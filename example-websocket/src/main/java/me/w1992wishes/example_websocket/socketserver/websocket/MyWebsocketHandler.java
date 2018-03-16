package me.w1992wishes.example_websocket.socketserver.websocket;

import me.w1992wishes.example_websocket.socketserver.domain.SocketRequest;

/**
 * Created by w1992wishes
 * on 2018/3/16.
 */
public interface MyWebsocketHandler {

    void handleOnWebSocketText(SocketRequest socketRequest);

    void handleOnWebSocketConnect(SocketRequest socketRequest);

    void handleOnWebSocketClose(SocketRequest socketRequest);

}
