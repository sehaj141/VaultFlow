package com.vaultflow.controller;

import com.vaultflow.dto.response.DashboardAnalyticsResponse;
import com.vaultflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/analytics")
    public ResponseEntity<DashboardAnalyticsResponse> getAnalytics(@AuthenticationPrincipal UserDetails userDetails) {
        DashboardAnalyticsResponse analytics = dashboardService.getDashboardAnalytics(userDetails.getUsername());
        return ResponseEntity.ok(analytics);
    }
}
