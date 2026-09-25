package org.haz.audit;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AuditLogger {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final String instanceId = Integer.toHexString(System.identityHashCode(this));

    @PostConstruct
    public void created() {
        System.out.println("[LIFECYCLE] new AuditLogger instance created (id=" + instanceId + ")");
    }

    public void log(String action) {
        System.out.println("   [AUDIT " + instanceId + "] "
                + LocalDateTime.now().format(STAMP) + " - " + action);
    }

    public String getInstanceId() {
        return instanceId;
    }
}
