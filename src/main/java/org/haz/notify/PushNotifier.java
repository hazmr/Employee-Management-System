package org.haz.notify;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class PushNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("   [PUSH] " + message);
    }
}
