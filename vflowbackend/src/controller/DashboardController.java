package com.vaultflow.controller;

import com.vaultflow.dto.response.DashboardResponse;
import com.vaultflow.security.CurrentUser;
import com.vaultflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@CurrentUser UUID userId) {
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }
}