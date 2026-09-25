package org.haz.config;

import org.haz.notify.NotificationManager;
import org.haz.notify.Notifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@ComponentScan(basePackages = "org.haz")
@PropertySource("classpath:application.properties")
public class AppConfig {
    @Bean
    public NotificationManager notificationManager(List<Notifier> notifiers) {
        return new NotificationManager(notifiers);
    }
}
