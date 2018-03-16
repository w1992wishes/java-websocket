package me.w1992wishes.example_websocket.socketserver.websocket;

import me.w1992wishes.example_websocket.util.ApplicationContextUtil;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.springframework.context.ApplicationContext;

/**
 * Created by w1992wishes
 * on 2018/3/15.
 */
public class MyWebsocket implements WebSocketListener {
    //当一个新的websocket连接时，不会把这个覆盖，进去看源代码会找到原因
    //因为每次请求过来，会调用MyWebsocketServlet的service方法
    //而service方法会调用到factory（也就是MyWebsocket注册进去的那个类）的createWebSocket方法
    //该方法每次通过反射初始化一个MyWebsocket
    private Session session;

    @Override
    public void onWebSocketBinary(byte payload[], int offset, int len) {

    }

    //连接关闭触发
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        MyWebsocketDispatcher websocketDispatcher = getWebsocketDispatcher();
        websocketDispatcher.dispatcherOnWebSocketClose(session);
    }

    //连接开启触发
    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        MyWebsocketDispatcher websocketDispatcher = getWebsocketDispatcher();
        websocketDispatcher.dispatcherOnWebSocketConnect(session);
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
    }

    //接收到消息触发
    @Override
    public void onWebSocketText(String message) {
        MyWebsocketDispatcher websocketDispatcher = getWebsocketDispatcher();
        websocketDispatcher.dispatcherOnWebSocketText(session, message);
    }

    private MyWebsocketDispatcher getWebsocketDispatcher() {
        ApplicationContext context = ApplicationContextUtil.getContext();
        MyWebsocketDispatcher webSocketDispatcher = (MyWebsocketDispatcher) context.getBean("myWebsocketDispatcher");
        return webSocketDispatcher;
    }
}
