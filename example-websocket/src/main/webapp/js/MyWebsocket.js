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