package me.w1992wishes.example_websocket.socketserver.websocket;

import com.alibaba.fastjson.JSONObject;
import me.w1992wishes.example_websocket.socketserver.domain.SocketRequest;
import me.w1992wishes.example_websocket.socketserver.domain.SocketResponse;
import me.w1992wishes.example_websocket.util.ApplicationContextUtil;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by w1992wishes
 * on 2018/3/15.
 */
@Service("myWebsocketDispatcher")
public class MyWebsocketDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyWebsocketDispatcher.class);

    //这个map是维持所有的websocket连接，因为消息要推送到所有的客户端
    private Map<Session, SocketRequest> socketRequestMap = new ConcurrentHashMap<Session, SocketRequest>();

    public void dispatcherOnWebSocketConnect(Session session){
        SocketRequest socketRequest = initWebSocketRequest(session);
        socketRequestMap.put(session, socketRequest);

        // 找到对应的业务service
        MyWebsocketHandler handler = getWebsocketHandler(socketRequest.getRequestExecBeanName());
        handler.handleOnWebSocketConnect(socketRequest);
    }

    public void dispatcherOnWebSocketText(Session session, String message){
        SocketRequest socketRequest = socketRequestMap.get(session);

        if ("~H#B~".equals(message)) {
            // 心跳报文，忽略
            return;
        }

        JSONObject json = JSONObject.parseObject(message);
        for (String key : json.keySet()) {
            socketRequest.addRequestParameter(key, json.getString(key));
        }

        try {
            // 找到对应的业务service
            MyWebsocketHandler handler = getWebsocketHandler(socketRequest.getRequestExecBeanName());
            handler.handleOnWebSocketText(socketRequest);
        } catch (Exception e) {
            LOGGER.error("", e);
        }

    }

    public void dispatcherOnWebSocketClose(Session session) {
        SocketRequest socketRequest = socketRequestMap.get(session);
        try {
            // 找到对应的业务service
            MyWebsocketHandler handler = getWebsocketHandler(socketRequest.getRequestExecBeanName());
            handler.handleOnWebSocketClose(socketRequest);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        socketRequestMap.remove(session);
    }

    private SocketRequest initWebSocketRequest(Session session) {
        SocketRequest socketRequest = new SocketRequest();
        SocketResponse response = new SocketResponse();
        response.setSession(session);
        socketRequest.setResponse(response);

        URI uri = session.getUpgradeRequest().getRequestURI();
        socketRequest.setRequestHost(uri.getHost());

        String path = session.getUpgradeRequest().getRequestURI().getPath();
        if (path.contains("/websocket/")) {
            path = path.substring(path.indexOf("/websocket/") + "/websocket/".length());
            String beanName = path.split("/")[0];
            socketRequest.setRequestExecBeanName(beanName);

            String queryStr = session.getUpgradeRequest().getRequestURI().getQuery();
            if (queryStr != null && queryStr != "") {
                String[] params = queryStr.split("&");
                for (String param : params) {
                    if (!param.contains("=")) {
                        continue;
                    }
                    int c = param.indexOf("=");
                    socketRequest.addRequestParameter(param.substring(0, c), param.substring(c + 1));
                }
            }
        } else {
            return null;
        }
        return socketRequest;

    }

    private MyWebsocketHandler getWebsocketHandler(String beanName){
        return ApplicationContextUtil.getContext().getBean(beanName, MyWebsocketHandler.class);
    }
}
