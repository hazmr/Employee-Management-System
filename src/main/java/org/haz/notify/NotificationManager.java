package org.haz.notify;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class NotificationManager {
    private final List<Notifier> notifiers;

    @Value("${notification.retry-count}")
    private int retryCount;

    private Notifier urgentNotifier;

    public NotificationManager(List<Notifier> notifiers) {
        this.notifiers = notifiers;
    }

    @Autowired
    public void setUrgentNotifier(@Qualifier("smsNotifier") Notifier urgentNotifier) {
        this.urgentNotifier = urgentNotifier;
    }

    @PostConstruct
    public void init() {
        System.out.println("[LIFECYCLE] NotificationManager initialised with "
                + notifiers.size() + " notifier(s), retryCount=" + retryCount);
    }

    public void notifyAll(String message) {
        for (Notifier notifier : notifiers) {
            notifier.send(message);
        }
    }

    public void notifyUrgent(String message) {
        urgentNotifier.send("URGENT: " + message);
    }

    public int getRetryCount() {
        return retryCount;
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("[LIFECYCLE] NotificationManager destroyed");
    }
}
