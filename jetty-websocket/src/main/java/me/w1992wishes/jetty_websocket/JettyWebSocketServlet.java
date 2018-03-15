package me.w1992wishes.jetty_websocket;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Created by w1992wishes
 * on 2018/3/15.
 */
public class JettyWebSocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory factory) {
        // set a 10 second timeout
        //factory.getPolicy().setIdleTimeout(10000);
        factory.register(JettyWebSocket.class);
    }
}
