package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.GroupPurchaseParticipant;
import com.togethershop.backend.domain.GroupPurchaseProject;
import com.togethershop.backend.dto.*;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.GroupPurchaseParticipantRepository;
import com.togethershop.backend.repository.GroupPurchaseProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GroupPurchaseService {

    private final GroupPurchaseProjectRepository projectRepository;
    private final GroupPurchaseParticipantRepository participantRepository;
    private final BusinessRepository businessRepository;

    /**
     * 공동구매 프로젝트 생성 (DDL 기준)
     */
    public GroupPurchaseProjectDTO createProject(Long businessId, GroupPurchaseProjectCreateDTO createDTO) {
        log.info("Creating group purchase project for business: {}", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("사업자를 찾을 수 없습니다."));

        GroupPurchaseProject project = GroupPurchaseProject.builder()
                .businessId(businessId)
                .description(createDTO.getTitle()) // DDL: description에 제목 저장
                .totalQuantity(createDTO.getTargetQuantity()) // DDL: total_quantity
                .targetNumber(createDTO.getTargetQuantity()) // DDL: target_number
                .targetMoney(createDTO.getTargetMoney()) // DDL: target_money
                .accountNumber(createDTO.getAccountNumber()) // DDL: account_number
                .accountHost(createDTO.getAccountHost()) // DDL: account_host
                .status(GroupPurchaseProject.ProjectStatus.OPEN) // DDL: status
                .endDate(createDTO.getEndDate()) // DDL: end_date
                .createdAt(LocalDateTime.now()) // DDL: created_at
                .build();

        GroupPurchaseProject savedProject = projectRepository.save(project);
        return convertToDTO(savedProject, business);
    }

    /**
     * 공동구매 참여 - 참가 여부만 확인, 인원수만 관리
     */
    public GroupPurchaseParticipantDTO participateInProject(Long businessId, GroupPurchaseParticipationRequestDTO requestDTO) {
        log.info("Business {} participating in project {}", businessId, requestDTO.getProjectId());

        GroupPurchaseProject project = projectRepository.findById(requestDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        if (project.getStatus() != GroupPurchaseProject.ProjectStatus.OPEN) {
            throw new RuntimeException("참여할 수 없는 프로젝트입니다.");
        }

        if (project.getEndDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("종료된 프로젝트입니다.");
        }

        if (participantRepository.findByProjectIdAndBusinessId(requestDTO.getProjectId(), businessId).isPresent()) {
            throw new RuntimeException("이미 참여한 프로젝트입니다.");
        }

        if (project.getBusinessId().equals(businessId)) {
            throw new RuntimeException("자신이 생성한 프로젝트에는 참여할 수 없습니다.");
        }

        // 목표 인원수 초과 확인 (DDL의 target_number 기준)
        Long currentParticipantCount = participantRepository.countParticipantsByProjectId(requestDTO.getProjectId());
        if (currentParticipantCount >= project.getTargetNumber()) {
            throw new RuntimeException("목표 인원수를 초과할 수 없습니다.");
        }

        GroupPurchaseParticipant participant = GroupPurchaseParticipant.builder()
                .projectId(requestDTO.getProjectId())
                .businessId(businessId)
                .status(GroupPurchaseStatus.APPLIED)
                .joinedAt(LocalDateTime.now())
                .build();

        GroupPurchaseParticipant savedParticipant = participantRepository.save(participant);
        checkAndUpdateProjectCompletion(requestDTO.getProjectId());

        return convertToDTO(savedParticipant);
    }

    /**
     * 프로젝트 목록 검색 - 모든 프로젝트 조회
     */
    @Transactional(readOnly = true)
    public Page<GroupPurchaseProjectDTO> searchProjects(Pageable pageable) {
        Page<GroupPurchaseProject> projects = projectRepository.findAll(pageable);
        return projects.map(this::convertToDTO);
    }

    /**
     * 프로젝트 상세 조회
     */
    @Transactional(readOnly = true)
    public GroupPurchaseProjectDTO getProjectDetail(Long projectId) {
        GroupPurchaseProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        return convertToDTO(project);
    }

    /**
     * 내 프로젝트 목록
     */
    @Transactional(readOnly = true)
    public List<GroupPurchaseProjectDTO> getMyProjects(Long businessId) {
        List<GroupPurchaseProject> projects = projectRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        return projects.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 내 참여 목록
     */
    @Transactional(readOnly = true)
    public Page<GroupPurchaseParticipantDTO> getMyParticipations(Long businessId, Pageable pageable) {
        Page<GroupPurchaseParticipant> participations = participantRepository.findByBusinessIdOrderByJoinedAtDesc(businessId, pageable);
        return participations.map(this::convertToDTO);
    }

    /**
     * 프로젝트 참여자 목록
     */
    @Transactional(readOnly = true)
    public Page<GroupPurchaseParticipantDTO> getProjectParticipants(Long projectId, Pageable pageable) {
        Page<GroupPurchaseParticipant> participants = participantRepository.findParticipantsWithBusinessInfo(projectId, pageable);
        return participants.map(this::convertToDTO);
    }

    /**
     * 참여 승인 (프로젝트 생성자만, FULFILLED 상태에서만)
     */
    public void approveParticipation(Long businessId, Long participantId) {
        GroupPurchaseParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("참여자를 찾을 수 없습니다."));
        
        GroupPurchaseProject project = projectRepository.findById(participant.getProjectId())
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        if (!project.getBusinessId().equals(businessId)) {
            throw new RuntimeException("프로젝트 생성자만 승인할 수 있습니다.");
        }

        if (project.getStatus() != GroupPurchaseProject.ProjectStatus.FULFILLED) {
            throw new RuntimeException("목표 인원이 달성된 프로젝트에서만 승인할 수 있습니다.");
        }

        if (participant.getStatus() != GroupPurchaseStatus.APPLIED) {
            throw new RuntimeException("승인할 수 없는 상태입니다.");
        }

        participant.setStatus(GroupPurchaseStatus.CONFIRMED);
        participantRepository.save(participant);
    }

    /**
     * 참여 취소
     */
//    public void cancelParticipation(Long businessId, Long projectId) {
//        GroupPurchaseParticipant participant = participantRepository.findByProjectIdAndBusinessId(projectId, businessId)
//                .orElseThrow(() -> new RuntimeException("참여 내역을 찾을 수 없습니다."));
//
//        participant.setStatus(GroupPurchaseStatus.CANCELLED);
//        participantRepository.save(participant);
//    }
    /**
     * 참여 취소 (직접 삭제 방식)
     */
    public void cancelParticipation(Long businessId, Long projectId) {
        int deletedCount = participantRepository.deleteByProjectIdAndBusinessId(projectId, businessId);
        if (deletedCount == 0) {
            throw new RuntimeException("참여 내역을 찾을 수 없습니다.");
        }
    }


    /**
     * 프로젝트 취소
     */
    public void cancelProject(Long businessId, Long projectId) {
        GroupPurchaseProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        if (!project.getBusinessId().equals(businessId)) {
            throw new RuntimeException("프로젝트 생성자만 취소할 수 있습니다.");
        }

        project.setStatus(GroupPurchaseProject.ProjectStatus.CANCELLED);
        projectRepository.save(project);
    }

    // Helper methods
    private void checkAndUpdateProjectCompletion(Long projectId) {
        GroupPurchaseProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        if (project.getStatus() != GroupPurchaseProject.ProjectStatus.OPEN) {
            return;
        }

        Long participantCount = participantRepository.countParticipantsByProjectId(projectId);

        if (participantCount >= project.getTargetNumber()) {
            project.setStatus(GroupPurchaseProject.ProjectStatus.FULFILLED);
            projectRepository.save(project);
            log.info("Project {} has been fulfilled with {} participants", projectId, participantCount);
        }
    }

    private GroupPurchaseProjectDTO convertToDTO(GroupPurchaseProject project) {
        Business business = businessRepository.findById(project.getBusinessId()).orElse(null);
        return convertToDTO(project, business);
    }

    private GroupPurchaseProjectDTO convertToDTO(GroupPurchaseProject project, Business business) {
        Long participantCount = participantRepository.countParticipantsByProjectId(project.getId());
        Integer currentQuantity = participantCount.intValue();
        Integer targetQuantity = project.getTargetNumber();
        
        return GroupPurchaseProjectDTO.builder()
                .id(project.getId()) // DDL: project_id
                .businessId(project.getBusinessId()) // DDL: business_id
                .businessName(business != null ? business.getBusinessName() : null) // 조인 데이터
                .description(project.getDescription()) // DDL: description
                .totalQuantity(project.getTotalQuantity()) // DDL: total_quantity
                .targetNumber(project.getTargetNumber()) // DDL: target_number
                .targetMoney(project.getTargetMoney()) // DDL: target_money
                .accountNumber(project.getAccountNumber()) // DDL: account_number
                .accountHost(project.getAccountHost()) // DDL: account_host
                .status(project.getStatus().name()) // DDL: status (OPEN, CLOSED, FULFILLED, CANCELLED)
                .endDate(project.getEndDate()) // DDL: end_date
                .createdAt(project.getCreatedAt()) // DDL: created_at
                
                // 계산된 필드들
                .currentQuantity(currentQuantity)
                .participantCount(participantCount.intValue())
                .remainingQuantity(Math.max(0, targetQuantity - currentQuantity))
                .progressPercentage(targetQuantity > 0 ? (double) currentQuantity / targetQuantity * 100 : 0.0)
                .daysRemaining(project.getEndDate() != null ? 
                    Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), project.getEndDate())) : null)
                .build();
    }

    private GroupPurchaseParticipantDTO convertToDTO(GroupPurchaseParticipant participant) {
        Business business = businessRepository.findById(participant.getBusinessId()).orElse(null);

        return GroupPurchaseParticipantDTO.builder()
                .id(participant.getId()) // DDL: participant_id
                .projectId(participant.getProjectId()) // DDL: project_id
                .businessId(participant.getBusinessId()) // DDL: business_id
                .businessName(business != null ? business.getBusinessName() : null) // 조인 데이터
                .businessCategory(business != null ? business.getBusinessCategory() : null) // 조인 데이터
                .status(participant.getStatus()) // DDL: status (APPLIED, CONFIRMED, CANCELLED)
                .joinedAt(participant.getJoinedAt()) // DDL: joined_at
                .build();
    }
}
