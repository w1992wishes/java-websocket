package me.w1992wishes.jetty_websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by w1992wishes
 * on 2018/3/15.
 */
public class JettyWebSocket implements WebSocketListener {

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<JettyWebSocket> webSocketSet = new CopyOnWriteArraySet<JettyWebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {

    }

    @Override
    public void onWebSocketClose(int i, String s) {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    @Override
    public void onWebSocketConnect(Session session) {
        System.out.println("jetty");
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }

    @Override
    public void onWebSocketError(Throwable throwable) {

    }

    @Override
    public void onWebSocketText(String message) {
        System.out.println("来自客户端的消息:" + message);
        //群发消息
        for(JettyWebSocket item: webSocketSet){
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void sendMessage(String message) throws IOException {
        this.session.getRemote().sendString(message);
        //this.session.getAsyncRemote().sendText(message);
    }

    private static synchronized int getOnlineCount() {
        return onlineCount;
    }

    private static synchronized void addOnlineCount() {
        JettyWebSocket.onlineCount++;
    }

    private static synchronized void subOnlineCount() {
        JettyWebSocket.onlineCount--;
    }
}
