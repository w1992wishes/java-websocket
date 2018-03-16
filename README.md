# Websocket消息推送

本篇结构：

 - 背景
 - HTTP协议特点
 - 消息推送方案
 - Websocket简介
 - Websocket实例

## 一、背景

HTTP协议的无状态和被动性，使得B/S架构的服务器主动推送消息给浏览器比较困难，而通用的一些解决方案又有各种各样的问题，比如：ajax轮询会有很多无用的请求，浪费宽带；基于Flash的消息推送又有Flash支持不好，无法自动穿越防火墙等问题......

Websocket就是在这种情况下出现的一个协议。

## 二、HTTP协议特点

B/S架构的系统多使用HTTP协议，HTTP协议的特点：

**1、简单快速**

客户向服务器请求服务时，只需传送请求方法和路径。请求方法常用的有GET、HEAD、POST。每种方法规定了客户与服务器联系的类型不同。由于HTTP协议简单，使得HTTP服务器的程序规模小，因而通信速度很快。

**2、灵活**

HTTP允许传输任意类型的数据对象。正在传输的类型由Content-Type加以标记。

**3、无状态**

即无状态协议。这指的是，HTTP协议不对请求和响应之间的通信状态进行保存。所以使用HTTP协议，每当有新的请求发送，就会有对应的新响应产生。这样做的好处是更快地处理大量事务，确保协议的可伸缩性。

然而，随着时间的推移，人们发现静态的HTML着实无聊而乏味，增加动态生成的内容才会令Web应用程序变得更加有用。于是乎，HTML的语法在不断膨胀，其中最重要的是增加了表单（Form）；客户端也增加了诸如脚本处理、DOM处理等功能；对于服务器，则相应的出现了CGI（Common Gateway Interface）以处理包含表单提交在内的动态请求。

在这种客户端与服务器进行动态交互的Web应用程序出现之后，HTTP无状态的特性严重阻碍了这些交互式应用程序的实现，毕竟交互是需要承前启后的，简单的购物车程序也要知道用户到底在之前选择了什么商品。于是，两种用于保持HTTP状态的技术就应运而生了，一个是Cookie，而另一个则是Session。

**4、持久连接**

HTTP协议初试版本中，每进行一次HTTP通信就要断开一次TCP连接。

早期这么做的原因是HTTP协议产生于互联网，因此服务器需要处理同时面向全世界数十万、上百万客户端的网页访问，但每个客户端（即浏览器）与服务器之间交换数据的间歇性较大（即传输具有突发性、瞬时性），并且网页浏览的联想性、发散性导致两次传送的数据关联性很低，如果按照上面的方式则需要在服务器端开的进程和句柄数目都是不可接受的，大部分通道实际上会很空闲、无端占用资源。因此HTTP的设计者有意利用这种特点将协议设计为请求时建连接、请求完释放连接，以尽快将资源释放出来服务其他客户端。

但是当浏览器请求一个包含多张图片的HTML页面时，会增加通信量的开销。为了解决这个问题，HTTP/1.1相处了持久连接（HTTP keep-alive）方法。其特点是，只要任意一端没有明确提出断开连接，则保持TCP连接状态，在请求首部字段中的Connection: keep-alive即为表明使用了持久连接。

这样一来，客户端和服务器之间的HTTP连接就会被保持，不会断开（超过Keep-Alive规定的时间，意外断电等情况除外），当客户端发送另外一个请求时，就使用这条已经建立的连接。

**5、支持B/S及C/S模式**

> PS:这节内容多来自博文：[http协议特点](http://blog.csdn.net/u014005836/article/details/51129655)

### 三、消息推送方案

### 3.1、HTTP使得服务器无法主动推送消息

HTTP的生命周期通过Request来界定，也就是一个Request，一个Response，在HTTP1.0中，这次HTTP请求就结束了。

在HTTP1.1中进行了改进，添加了一个keep-alive，也就是说，在一个HTTP连接中，可以发送多个Request，接收多个Response。但是在HTTP中永远是Request=Response，也就是说一个request只能有一个response。而且这个response也是被动的，不能主动发起。

反映在日常生活中就是：

客户端通过浏览器发出一个请求，服务器端接收请求后进行处理并返回结果给客户端，客户端浏览器将信息呈现。

这种机制对于信息变化不是特别频繁的应用可以良好支撑，但对于实时要求高、海量并发的应用来说显得捉襟见肘，在当前业界移动互联网蓬勃发展的趋势下，高并发与用户实时响应是Web应用经常面临的问题，比如金融证券的实时信息、Web导航应用中的地理位置获取、社交网络的实时消息推送等。

### 3.2、实现服务器主动推送消息

有问题出现，就会有解决方案：

**1.Ajax轮询**

其原理简单易懂，就是客户端定时向服务器发送Ajax请求，询问服务器是否有新信息。

但它的问题也很明显：当客户端以固定频率向服务器端发送请求时，服务器端的数据可能并没有更新，带来很多无谓请求，浪费带宽，效率低下。

适于小型应用。

**2.Flash Socket**

AdobeFlash通过自己的Socket实现完成数据交换，再利用Flash暴露出相应的接口给JavaScript调用，从而达到实时传输目的。此方式比轮询要高效，且因为Flash安装率高，应用场景广泛。

但是移动互联网终端上Flash的支持并不好：IOS系统中无法支持Flash，Android虽然支持Flash但实际的使用效果差强人意，且对移动设备的硬件配置要求较高。2012年Adobe官方宣布不再支持Android4.1+系统，宣告了Flash在移动终端上的死亡。

**3.长轮询(long poll)，长连接**

keep-alive connection是指在一次TCP连接中完成多个HTTP请求，但是对每个请求仍然要单独发HTTP header；长轮询是指从客户端（一般就是浏览器）不断主动的向服务器发 HTTP 请求查询是否有新数据。这两种模式有一个共同的缺点，就是除了真正的数据部分外，服务器和客户端还要大量交换 HTTP header，信息交换效率很低。它们建立的“长连接”都是伪.长连接，只不过好处是不需要对现有的HTTP server和浏览器架构做修改就能实现。

还有就是下面要说的Websocket。

> PS：关于Ajax轮询，长轮询等解释来自知乎：
https://www.zhihu.com/question/20215561

## 四、Websocket简介

### 4.1、Websocket是什么

WebSocket是HTML5下一种新的协议。

它是一个新的基于TCP的应用层协议，只需要一次连接，以后的数据不需要重新建立连接，可以直接发送，它是基于TCP的，属于和HTTP相同的地位。

![](http://p5maw5o6h.bkt.clouddn.com/20180315_websocket_01.png)

它的最大特点就是，服务器可以主动向客户端推送信息，客户端也可以主动向服务器发送信息，是真正的双向平等对话，属于服务器推送技术的一种。

### 4.2、Websocket的特点

 - 建立在 TCP 协议之上，服务器端的实现比较容易。
 - 与 HTTP 协议有着良好的兼容性。默认端口也是80和443，并且握手阶段采用 HTTP 协议，因此握手时不容易屏蔽，能通过各种 HTTP 代理服务器。
 - 数据格式比较轻量，性能开销小，通信高效。
 - 可以发送文本，也可以发送二进制数据。
 - 没有同源限制，客户端可以与任意服务器通信。
 - 协议标识符是ws（如果加密，则为wss），服务器网址就是 URL。

### 4.3、Websocket的优势

1. 是真正的全双工方式，建立连接后客户端与服务器端是完全平等的，可以互相主动请求。而HTTP长连接基于HTTP，是传统的客户端对服务器发起请求的模式。
2. HTTP长连接中，每次数据交换除了真正的数据部分外，服务器和客户端还要大量交换HTTP header，信息交换效率很低。Websocket协议通过第一个request建立了TCP连接之后，之后交换的数据都不需要发送 HTTP header就能交换数据，这显然和原有的HTTP协议有区别所以它需要对服务器和客户端都进行升级才能实现（主流浏览器都已支持HTML5）。

## 五、Websocket实例

后台消息推送是很多系统中重要的功能，我现在工作的项目是一个网管项目，当网管服务器收到设备发过来的告警时，需要将告警信息推送到客户端，这里面就用到了消息推送。

之前网管的推送是用flash实现的，但flash经常出现被禁用等问题，导致客户端收不到服务器推送来的消息，加上很多浏览器对flash已经不再更新，了解了Websocket的优势后，于是将后台的消息推送用Websocket实现。

网管这边是用Jetty做服务器，比较新的Jetty也已经兼容了Websocket，所以就采用了Jetty的api实现。

关于如何实现这些api就不介绍了，网上有不少例子，直接上代码（源码可以在https://github.com/w1992wishes/java-websocket的example-websocket模块找到）：

### 5.1、前台代码

将通用的Websocket连接放到MyWebsocket.js中：

```
function MyWebSocket(serviceName, config) {
    this.serviceName = serviceName;
    this.config = config;

    this.connect();
}

MyWebSocket.prototype.connect = function() {
    var _me = this;
    var serverIp = location.hostname;
    var _config = _me.config;

    // 需要判断是否支持websocket，如果不支持，使用flash版本的
    if (typeof WebSocket != 'undefined') {
        initWebSocket();
        _me.supportWebSocket = true;
    } else {
        console.log("not support Websocket");
        _me.supportWebSocket = false;
        return;
    }

    function initWebSocket() {
        var url = 'ws://localhost:8080/example/websocket/' + _me.serviceName;

        var firstParam = true;
        if(_config.params) {
            for(var key in _config.params) {
                if(firstParam) {
                    url += '?' + key + '=' + _config.params[key];
                    firstParam = false;
                } else {
                    url += '&' + key + '=' + _config.params[key];
                }
            }
        }
        var socket = new WebSocket(url);
        _me.socket = socket;

        socket.onopen = function() {
            _config.onopen();

            // 开启心跳检测，以免一段时间后收不到消息自动失联
            heartbeat_timer = setInterval(function () {
                keepalive(socket)
            }, 10000);

            function keepalive(socket) {
                socket.send('~H#B~');
            }
        };

        socket.onmessage = function (message) {
            _config.onmessage(message.data);
        };

        socket.onclose = function() {
            _config.onclose();
            clearInterval(heartbeat_timer);
        };

        socket.onerror = function(err) {
            _config.onerror(err);
        };
    }
}

MyWebSocket.prototype.send = function(message) {
    if(this.supportWebSocket) {
        this.socket.send(JSON.stringify(message));
    } else {
        this.socket.sendRequest(this.serviceName, message, true);
    }
}

MyWebSocket.prototype.close = function() {
    if(this.supportWebSocket) {
        this.socket.close();
    } else {
        this.socket.disconnect();
    }
}

MyWebSocket.prototype.reconnect = function() {
    this.close();
    this.connect();
}
```

然后在需要消息推送的页面引入该js，并做相应配置：

```
<%@ page language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>消息推送</title>
    <style type="text/css">
        .greenTxt{ color:#008200;}
        .redTxt{ color:#820803; }
    </style>
    <script src="https://apps.bdimg.com/libs/jquery/2.1.4/jquery.min.js"></script>
    <script src="js/MyWebsocket.js"></script>
    <script>
        var ws;
        $(document).ready(function(){
            var $content = $("#content");
            window.GLOBAL_SOCKET_CONNECT_ID = random( 25 );
            ws = new MyWebSocket("messagePusher",{
                params: {
                    JCONNECTID : window.GLOBAL_SOCKET_CONNECT_ID,
                },
                onmessage: function (messagestr) {
                    var content = JSON.parse(messagestr);
                    $content.append("<pre>"+content.data+"</pre>");
                },
                onopen: function() {
                    $content.append("<p class='greenTxt'>SOCKET已连接上...</p>");
                },
                onclose: function() {
                    $content.append("<p class='redTxt'>SOCKET连接已中断!</p>");
                },
                onerror: function(err) {
                    $content.append("<pre>"+err+"</pre>");
                }
            });
        });

        function random(len) {
            len = len || 32;
            /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
            var $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';
            var $maxPos = $chars.length;
            var $data = '';
            for (i = 0; i < len; i++) {
                $data += $chars.charAt(Math.floor(Math.random() * $maxPos));
            }
            return $data;
        }
    </script>
</head>
    <body>
    <div id="content"></div>
    </body>
</html>
```

这样前台代码就完成了。

### 5.2、后台代码

继承jetty的WebSocketServlet，实现其configure()方法，给WebSocketServletFactory提供一个WebSocketListener的实现类。这样在收到websocket请求后，会生成这个实现类的一个实例。通过这个实例，可以与前端进行交互。

```java
public class MyWebsocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.register(MyWebsocket.class);
    }
}
```

既然是一个Servlet，就需要在web.xml中配置（Servlet 3.0中可以用注解的方式）：

```
<servlet>
    <servlet-name>WebSocketServlet</servlet-name>
    <servlet-class>me.w1992wishes.example_websocket.socketserver.websocket.MyWebsocketServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>WebSocketServlet</servlet-name>
    <url-pattern>/websocket/*</url-pattern>
</servlet-mapping>
```

MyWebsocket（该类会在每个Websocket请求建立时都重新实例化一个）实现了WebSocketListener，需要实现几个事件监听方法，在对应事件发生时将被触发。：

```java
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
```

ApplicationContextUtil帮助不被spring管理的实例获取applicationContext，进而获取bean容器中的MyWebsocketDispatcher。

```
@Component
public class ApplicationContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;
    }
}
```

WebSocketDispatcher是被spring容器管理的分发器，到了这里，就可以与业务层打交道了。当一个Websocket请求到达，会解析该请求，获取前台传来的beanName，然后分发器将该请求放入一个Map，并分发到相应的业务类并触发相应的Websocket事件。

```
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
```

WebSocketDispatcher的重点在于SocketRequest和SocketResponse的创建，由它去与具体的业务类打交道，可以降低Jetty api对业务类的侵入，降低耦合。

```
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
```

最后就来到具体业务代码：

```
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
```

到这里，基本代码已经结束，但前后台建立Websocket连接后，MessagePusherImpl中的dataQueue并没有消息，是空队列，因此代码会一直阻塞。

为此做了一个简单的定时器，模拟向dataQueue中添加消息。

```
@Service
public class MessageTimer {

    private int i;

    @Autowired
    private MessagePusher messagePusher;

    @PostConstruct
    public void initialize(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                i++;
                Message message = new Message();
                message.setData("a test message, id is " + i);
                messagePusher.sendMessage(message);
            }
        }, new Date(), 10000);
    }

}
```


 














