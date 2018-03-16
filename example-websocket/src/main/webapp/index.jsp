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
