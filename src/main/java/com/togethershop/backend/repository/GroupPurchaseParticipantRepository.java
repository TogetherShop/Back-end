package com.togethershop.backend.repository;

import com.togethershop.backend.domain.GroupPurchaseParticipant;
import com.togethershop.backend.dto.GroupPurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupPurchaseParticipantRepository extends JpaRepository<GroupPurchaseParticipant, Long> {
    
    // 프로젝트별 참여자 조회
    List<GroupPurchaseParticipant> findByProjectIdOrderByJoinedAtDesc(Long projectId);
    
    // 프로젝트별 상태별 참여자 조회
    List<GroupPurchaseParticipant> findByProjectIdAndStatus(Long projectId, GroupPurchaseStatus status);
    
    // 사업자별 참여 프로젝트 조회
    Page<GroupPurchaseParticipant> findByBusinessIdOrderByJoinedAtDesc(Long businessId, Pageable pageable);
    
    // 사업자별 상태별 참여 프로젝트 조회
    List<GroupPurchaseParticipant> findByBusinessIdAndStatus(Long businessId, GroupPurchaseStatus status);
    
    // 특정 프로젝트에 특정 사업자가 이미 참여했는지 확인
    Optional<GroupPurchaseParticipant> findByProjectIdAndBusinessId(Long projectId, Long businessId);

    // 특정 프로젝트에 특정 사업자 참여 삭제
    int deleteByProjectIdAndBusinessId(Long projectId, Long businessId);


    // 프로젝트별 참여자 수 조회
    @Query("SELECT COUNT(gpp) FROM GroupPurchaseParticipant gpp " +
           "WHERE gpp.projectId = :projectId " +
           "AND gpp.status IN ('APPLIED', 'CONFIRMED')")
    Long countParticipantsByProjectId(@Param("projectId") Long projectId);
    
    // 프로젝트별 확정된 참여자 수 조회
    @Query("SELECT COUNT(gpp) FROM GroupPurchaseParticipant gpp " +
           "WHERE gpp.projectId = :projectId " +
           "AND gpp.status = 'CONFIRMED'")
    Long countConfirmedParticipantsByProjectId(@Param("projectId") Long projectId);
    
    // 프로젝트별 총 신청 수량 조회 (각 참여자당 1개씩 계산)
    @Query("SELECT COUNT(gpp) FROM GroupPurchaseParticipant gpp " +
           "WHERE gpp.projectId = :projectId " +
           "AND gpp.status IN ('APPLIED', 'CONFIRMED')")
    Integer sumQuantityByProjectId(@Param("projectId") Long projectId);
    
    // 프로젝트별 확정된 총 수량 조회 (각 참여자당 1개씩 계산)
    @Query("SELECT COUNT(gpp) FROM GroupPurchaseParticipant gpp " +
           "WHERE gpp.projectId = :projectId " +
           "AND gpp.status = 'CONFIRMED'")
    Integer sumConfirmedQuantityByProjectId(@Param("projectId") Long projectId);
    
    // 사업자가 참여한 프로젝트의 상태별 통계
    @Query("SELECT gpp.status, COUNT(gpp) FROM GroupPurchaseParticipant gpp " +
           "WHERE gpp.businessId = :businessId " +
           "GROUP BY gpp.status")
    List<Object[]> getParticipationStatsByBusinessId(@Param("businessId") Long businessId);
    
    // 프로젝트 참여자 목록 (사업체 정보 포함)
    @Query("SELECT gpp FROM GroupPurchaseParticipant gpp " +
           "JOIN Business b ON gpp.businessId = b.id " +
           "WHERE gpp.projectId = :projectId " +
           "ORDER BY gpp.joinedAt DESC")
    Page<GroupPurchaseParticipant> findParticipantsWithBusinessInfo(@Param("projectId") Long projectId, 
                                                                     Pageable pageable);
}
