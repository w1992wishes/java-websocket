package me.w1992wishes.example_websocket.timer;

import me.w1992wishes.example_websocket.message.Message;
import me.w1992wishes.example_websocket.message.MessagePusher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by w1992wishes
 * on 2018/3/16.
 */
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
