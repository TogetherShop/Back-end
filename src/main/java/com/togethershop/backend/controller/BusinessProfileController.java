package com.togethershop.backend.controller;

import com.togethershop.backend.dto.BusinessProfileDTO;
import com.togethershop.backend.service.BusinessProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    @GetMapping("/profile-summary")
    public ResponseEntity<BusinessProfileDTO> getProfileSummary(
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        BusinessProfileDTO body = businessProfileService.getProfileSummaryByUsername(username);
        return ResponseEntity.ok(body);
    }
}
