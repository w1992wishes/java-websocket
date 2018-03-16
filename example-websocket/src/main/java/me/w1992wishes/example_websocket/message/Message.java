package me.w1992wishes.example_websocket.message;

/**
 * Created by w1992wishes
 * on 2018/3/16.
 */
public class Message {

    // 消息实体
    private Object data;
    // 单播时使用.如果希望消息只发送到某一个客户端,则在消息中配置此值,这个值可以需要从前端请求中携带过来
    private String jconnectID;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getJconnectID() {
        return jconnectID;
    }

    public void setJconnectID(String jconnectID) {
        this.jconnectID = jconnectID;
    }

}
