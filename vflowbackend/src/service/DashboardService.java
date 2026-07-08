package com.vaultflow.service;

import com.vaultflow.dto.response.DashboardResponse;

import java.util.UUID;

public interface DashboardService {
    DashboardResponse getDashboard(UUID userId);
}