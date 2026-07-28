package com.vaultflow.controller;

import com.vaultflow.dto.response.ActivityLogResponse;
import com.vaultflow.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> getActivityFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        List<ActivityLogResponse> response = activityService.getUserActivityFeed(userDetails.getUsername(), limit);
        return ResponseEntity.ok(response);
    }
}
