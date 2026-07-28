package com.vaultflow.event;

import com.vaultflow.entity.ActivityLog;
import com.vaultflow.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityEventListener {

    private final ActivityLogRepository activityLogRepository;

    @Async
    @EventListener
    public void handleActivityEvent(ActivityEvent event) {
        ActivityLog log = ActivityLog.builder()
                .user(event.getUser())
                .activityType(event.getActivityType())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .entityName(event.getEntityName())
                .details(event.getDetails())
                .build();

        activityLogRepository.save(log);
    }
}
