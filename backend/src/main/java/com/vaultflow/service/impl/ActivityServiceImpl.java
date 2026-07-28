package com.vaultflow.service.impl;

import com.vaultflow.dto.response.ActivityLogResponse;
import com.vaultflow.entity.ActivityLog;
import com.vaultflow.entity.User;
import com.vaultflow.exception.ResourceNotFoundException;
import com.vaultflow.repository.ActivityLogRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getUserActivityFeed(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<ActivityLog> logs = activityLogRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, safeLimit));

        return logs.stream()
                .map(this::mapToActivityLogResponse)
                .toList();
    }

    private ActivityLogResponse mapToActivityLogResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .activityType(log.getActivityType().name())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .entityName(log.getEntityName())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
