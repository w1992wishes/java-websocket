package me.w1992wishes.example_websocket.message;

import com.alibaba.fastjson.JSONObject;
import me.w1992wishes.example_websocket.socketserver.domain.SocketRequest;
import me.w1992wishes.example_websocket.socketserver.domain.SocketResponse;
import me.w1992wishes.example_websocket.socketserver.websocket.MyWebsocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by w1992wishes
 * on 2018/3/16.
 */
@Service("messagePusher")
public class MessagePusherImpl implements MyWebsocketHandler, MessagePusher{

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePusherImpl.class);

    private final Map<String, SocketResponse> connections = new ConcurrentHashMap<>();
    private final Map<String, String> hosts = new ConcurrentHashMap<>();
    private BlockingQueue<Message> dataQueue = null;
    private ExecutorService executorService;

    @PostConstruct
    public void initialize() {
        dataQueue = new ArrayBlockingQueue<Message>(1000);
        executorService = Executors.newSingleThreadExecutor();
        // ------开启一个专用线程用于消息广播推送------//
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("MessagePusherThread");
                // ---------如果告警队列对象存在，线程就一直执行----//
                while (true) {
                    // 获取并移除此告警队列的头部，在元素变得可用之前一直等待。
                    try {
                        Message message = dataQueue.take();
                        if (!connections.isEmpty()) {
                            Collection<SocketResponse> responses = connections.values();
                            String messageStr = JSONObject.toJSONString(message);
                            LOGGER.info(messageStr);
                            String jconnectID = message.getJconnectID();
                            if (jconnectID != null) {//单播
                                SocketResponse response = connections.get(jconnectID);
                                if (response == null || response.isClosed()) {
                                    connections.remove(jconnectID);
                                    continue;
                                }
                                response.write(messageStr);
                            } else {//广播
                                for (Iterator<SocketResponse> it = responses.iterator(); it.hasNext();) {
                                    SocketResponse $response = it.next();
                                    if ($response.isClosed()) {
                                        it.remove();
                                        continue;
                                    }
                                    $response.write(messageStr);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                    } catch (Exception ex){
                        LOGGER.error("Send message by pusher.", ex);
                    }
                }
            }
        });
    }

    /**
     * 推送消息到前端页面
     *
     * @param msg
     */
    @Override
    public void sendMessage(Message msg) {
        try {
            dataQueue.add(msg);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("sendMessage dataQueue [" + dataQueue.size() + "].");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void handleOnWebSocketText(SocketRequest socketRequest) {

    }

    @Override
    public void handleOnWebSocketConnect(SocketRequest socketRequest) {
        String connectID = socketRequest.getString("JCONNECTID");
        connections.put(connectID, socketRequest.getResponse());
        hosts.put(connectID, socketRequest.getRequestHost());
    }

    @Override
    public void handleOnWebSocketClose(SocketRequest socketRequest) {

    }
}
