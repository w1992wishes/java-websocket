package me.w1992wishes.example_websocket.socketserver.websocket;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Created by w1992wishes
 * on 2018/3/15.
 */
public class MyWebsocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.register(MyWebsocket.class);
    }
}
