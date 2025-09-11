package com.togethershop.backend.controller;

import com.togethershop.backend.dto.*;
import com.togethershop.backend.security.CustomUserDetails;
import com.togethershop.backend.service.GroupPurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-purchase")
@RequiredArgsConstructor
@Slf4j
@Validated
public class GroupPurchaseController {

    private final GroupPurchaseService groupPurchaseService;

    /**
     * 공동구매 프로젝트 생성
     */
    @PostMapping("/projects")
    public ResponseEntity<Map<String, Object>> createProject(
            @Valid @RequestBody GroupPurchaseProjectCreateDTO createDTO,
            Authentication authentication) {
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = userDetails.getUserId();
            
            GroupPurchaseProjectDTO project = groupPurchaseService.createProject(businessId, createDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "공동구매 프로젝트가 성공적으로 생성되었습니다.");
            response.put("data", project);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating project: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 공동구매 프로젝트 목록 조회
     */
    @GetMapping("/projects")
    public ResponseEntity<Map<String, Object>> searchProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<GroupPurchaseProjectDTO> projects = groupPurchaseService.searchProjects(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", projects.getContent());
            response.put("currentPage", projects.getNumber());
            response.put("totalPages", projects.getTotalPages());
            response.put("totalElements", projects.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching projects: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로젝트 검색 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 공동구매 프로젝트 상세 조회
     */
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<Map<String, Object>> getProjectDetail(@PathVariable Long projectId) {
        try {
            GroupPurchaseProjectDTO project = groupPurchaseService.getProjectDetail(projectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", project);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting project detail: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 공동구매 참여
     */
    @PostMapping("/participation")
    public ResponseEntity<Map<String, Object>> participateInProject(
            @Valid @RequestBody GroupPurchaseParticipationRequestDTO requestDTO,
            Authentication authentication) {
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = userDetails.getUserId();
            
            GroupPurchaseParticipantDTO participant = groupPurchaseService.participateInProject(businessId, requestDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "공동구매에 성공적으로 참여했습니다.");
            response.put("data", participant);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error participating in project: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 참여 취소
     */
    @DeleteMapping("/participation/{projectId}")
    public ResponseEntity<Map<String, Object>> cancelParticipation(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = userDetails.getUserId();
            
            groupPurchaseService.cancelParticipation(businessId, projectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "참여가 성공적으로 취소되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error canceling participation: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 프로젝트 취소 (생성자만)
     */
    @PutMapping("/projects/{projectId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = userDetails.getUserId();
            
            groupPurchaseService.cancelProject(businessId, projectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로젝트가 성공적으로 취소되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cancelling project: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 내가 생성한 프로젝트 목록 조회
     */
    @GetMapping("/my-projects")
    public ResponseEntity<Map<String, Object>> getMyProjects(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = userDetails.getUserId();
            
            List<GroupPurchaseProjectDTO> projects = groupPurchaseService.getMyProjects(businessId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", projects);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting my projects: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "프로젝트 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 내가 참여한 프로젝트 목록 조회
     */
    @GetMapping("/my-participations")
    public ResponseEntity<Map<String, Object>> getMyParticipations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = userDetails.getUserId();
            
            Pageable pageable = PageRequest.of(page, size);
            Page<GroupPurchaseParticipantDTO> participations = groupPurchaseService.getMyParticipations(businessId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", participations.getContent());
            response.put("currentPage", participations.getNumber());
            response.put("totalPages", participations.getTotalPages());
            response.put("totalElements", participations.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting my participations: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "참여 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 프로젝트 참여자 목록 조회
     */
    @GetMapping("/projects/{projectId}/participants")
    public ResponseEntity<Map<String, Object>> getProjectParticipants(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<GroupPurchaseParticipantDTO> participants = groupPurchaseService.getProjectParticipants(projectId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", participants.getContent());
            response.put("currentPage", participants.getNumber());
            response.put("totalPages", participants.getTotalPages());
            response.put("totalElements", participants.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting project participants: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "참여자 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 참여 승인 (프로젝트 생성자만, FULFILLED 상태에서만)
     */
    @PutMapping("/participants/{participantId}/approve")
    public ResponseEntity<Map<String, Object>> approveParticipation(
            @PathVariable Long participantId,
            Authentication authentication) {
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long businessId = userDetails.getUserId();
            
            groupPurchaseService.approveParticipation(businessId, participantId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "참여가 승인되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error approving participation: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
