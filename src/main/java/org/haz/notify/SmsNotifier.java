package org.haz.notify;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class SmsNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("   [SMS] " + message);
    }
}
