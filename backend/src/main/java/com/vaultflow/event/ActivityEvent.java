package com.vaultflow.event;

import com.vaultflow.entity.ActivityType;
import com.vaultflow.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ActivityEvent extends ApplicationEvent {

    private final User user;
    private final ActivityType activityType;
    private final String entityType;
    private final UUID entityId;
    private final String entityName;
    private final String details;

    public ActivityEvent(Object source, User user, ActivityType activityType, String entityType, UUID entityId, String entityName, String details) {
        super(source);
        this.user = user;
        this.activityType = activityType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.details = details;
    }
}
