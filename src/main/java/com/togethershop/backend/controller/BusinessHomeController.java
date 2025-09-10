package com.togethershop.backend.controller;

import com.togethershop.backend.dto.BusinessHomeResponseDTO;
import com.togethershop.backend.service.BusinessHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business")

public class BusinessHomeController {
    private final BusinessHomeService businessHomeService;

    @GetMapping("/home-summary")
    public ResponseEntity<BusinessHomeResponseDTO> getHomeSummary(@RequestParam Long memberId) {
        BusinessHomeResponseDTO body = businessHomeService.getHomeSummary(memberId);
        return ResponseEntity.ok(body);
    }
}
