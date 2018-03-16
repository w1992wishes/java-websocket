package me.w1992wishes.example_websocket.socketserver.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by w1992wishes
 * on 2018/3/16.
 */
public class SocketRequest {
    private String requestExecBeanName;
    private final Map<String, String> params;
    private SocketResponse response;
    private String requestHost;


    public SocketRequest(){
        params = new HashMap<>();
    }

    /**
     * 添加一个请求参数
     *
     * @param key
     * @param value
     */
    public void addRequestParameter(String key, String value) {
        params.put(key, value);
        response.addRequestParameter(key, value);
    }

    public int getInt(String paramName) {
        return Integer.parseInt(params.get(paramName));
    }

    public String getString(String paramName) {
        return params.get(paramName);
    }

    public String getRequestExecBeanName() {
        return requestExecBeanName;
    }

    public void setRequestExecBeanName(String requestExecBeanName) {
        this.requestExecBeanName = requestExecBeanName;
    }

    public SocketResponse getResponse() {
        return response;
    }

    public void setResponse(SocketResponse response) {
        this.response = response;
    }

    public String getRequestHost() {
        return requestHost;
    }

    public void setRequestHost(String requestHost) {
        this.requestHost = requestHost;
    }

}
