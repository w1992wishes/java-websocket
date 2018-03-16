package me.w1992wishes.example_websocket.socketserver.domain;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by w1992wishes
 * on 2018/3/16.
 */
public class SocketResponse {
    private Session session;
    private boolean closed;
    private Map<String, String> params = new HashMap<String, String>();

    public void write(String message) throws IOException {
        session.getRemote().sendString(message);
    }

    public void flush() throws IOException {
        session.getRemote().flush();
    }

    /**
     * 添加一个请求参数
     *
     * @param key
     * @param value
     */
    public void addRequestParameter(String key, String value) {
        params.put(key, value);
    }

    public int getInt(String paramName) {
        return Integer.parseInt(params.get(paramName));
    }

    public long getLong(String paramName) {
        return Long.parseLong(params.get(paramName));
    }

    public String getString(String paramName) {
        return params.get(paramName);
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public boolean isClosed() {
        if (!closed) {
            return !session.isOpen();
        }
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
