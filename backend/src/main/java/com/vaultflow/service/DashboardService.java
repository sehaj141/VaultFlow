package com.vaultflow.service;

import com.vaultflow.dto.response.DashboardAnalyticsResponse;

public interface DashboardService {
    DashboardAnalyticsResponse getDashboardAnalytics(String userEmail);
}
