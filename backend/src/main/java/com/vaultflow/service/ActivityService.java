package com.vaultflow.service;

import com.vaultflow.dto.response.ActivityLogResponse;

import java.util.List;

public interface ActivityService {
    List<ActivityLogResponse> getUserActivityFeed(String userEmail, int limit);
}
