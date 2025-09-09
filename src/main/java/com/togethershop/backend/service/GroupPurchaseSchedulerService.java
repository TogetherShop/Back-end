package com.togethershop.backend.service;

import com.togethershop.backend.domain.GroupPurchaseParticipant;
import com.togethershop.backend.domain.GroupPurchaseProject;
import com.togethershop.backend.dto.GroupPurchaseStatus;
import com.togethershop.backend.repository.GroupPurchaseParticipantRepository;
import com.togethershop.backend.repository.GroupPurchaseProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupPurchaseSchedulerService {

    private final GroupPurchaseProjectRepository projectRepository;
    private final GroupPurchaseParticipantRepository participantRepository;

    /**
     * 매분마다 만료된 프로젝트를 CLOSED로 변경
     */
    @Scheduled(fixedRate = 60000) // 10분마다 실행
    @Transactional
    public void closeExpiredProjects() {
        LocalDateTime now = LocalDateTime.now();
        
        // 종료일이 지났지만 아직 OPEN 상태인 프로젝트들 조회
        List<GroupPurchaseProject> expiredProjects = projectRepository.findExpiredOpenProjects(now);
        
        for (GroupPurchaseProject project : expiredProjects) {
            // 목표 인원 달성 여부 확인
            Long participantCount = participantRepository.countParticipantsByProjectId(project.getId());
            
            if (participantCount >= project.getTargetNumber()) {
                // 목표 인원 달성 - FULFILLED로 변경
                project.setStatus(GroupPurchaseProject.ProjectStatus.FULFILLED);
                projectRepository.save(project);
                
                log.info("Project {} automatically fulfilled with {} participants", 
                        project.getId(), participantCount);
            } else {
                // 목표 인원 미달성 - CLOSED로 변경
                project.setStatus(GroupPurchaseProject.ProjectStatus.CLOSED);
                projectRepository.save(project);
                
                // 참여자들을 모두 CANCELLED로 변경
                cancelAllParticipants(project.getId());
                
                log.info("Project {} automatically closed due to expiration. Participants: {}/{}",
                        project.getId(), participantCount, project.getTargetNumber());
            }
        }
    }

    /**
     * 프로젝트의 모든 참여자를 CANCELLED 상태로 변경
     */
    private void cancelAllParticipants(Long projectId) {
        List<GroupPurchaseParticipant> participants = participantRepository.findByProjectIdOrderByJoinedAtDesc(projectId);
        
        for (GroupPurchaseParticipant participant : participants) {
            if (participant.getStatus() != GroupPurchaseStatus.CANCELLED) {
                participant.setStatus(GroupPurchaseStatus.CANCELLED);
                participantRepository.save(participant);
                
                log.debug("Participant {} cancelled due to project closure", participant.getId());
            }
        }
    }
}
