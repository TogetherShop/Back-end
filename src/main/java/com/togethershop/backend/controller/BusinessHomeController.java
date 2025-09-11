package com.togethershop.backend.controller;

import com.togethershop.backend.dto.BusinessHomeResponseDTO;
import com.togethershop.backend.service.BusinessHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business")
public class BusinessHomeController {

    private final BusinessHomeService businessHomeService;

    @GetMapping("/home-summary")
    public ResponseEntity<BusinessHomeResponseDTO> getHomeSummary(
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        BusinessHomeResponseDTO body = businessHomeService.getHomeSummaryByUsername(username);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/home-summary-open")
    public ResponseEntity<BusinessHomeResponseDTO> getHomeSummaryOpen(
            @RequestParam("username") String username
    ) {
        BusinessHomeResponseDTO body = businessHomeService.getHomeSummaryByUsername(username);
        return ResponseEntity.ok(body);
    }
}
