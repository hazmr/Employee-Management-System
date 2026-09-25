package org.haz.notify;

import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@Order(1)
public class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("   [EMAIL] " + message);
    }
}
