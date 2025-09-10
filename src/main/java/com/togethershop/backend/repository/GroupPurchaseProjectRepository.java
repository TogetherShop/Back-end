package com.togethershop.backend.repository;

import com.togethershop.backend.domain.GroupPurchaseProject;
import com.togethershop.backend.dto.GroupPurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupPurchaseProjectRepository extends JpaRepository<GroupPurchaseProject, Long> {
    
    // 사업자별 프로젝트 조회
    List<GroupPurchaseProject> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
    
    // 상태별 프로젝트 조회
    Page<GroupPurchaseProject> findByStatus(GroupPurchaseProject.ProjectStatus status, Pageable pageable);
    
    // 활성화된 OPEN 프로젝트만 조회 (마감일이 지나지 않은 것)
    @Query("SELECT gp FROM GroupPurchaseProject gp " +
           "WHERE gp.status = 'OPEN' " +
           "AND (gp.endDate IS NULL OR gp.endDate > CURRENT_TIMESTAMP) " +
           "ORDER BY gp.createdAt DESC")
    Page<GroupPurchaseProject> findActiveOpenProjects(Pageable pageable);
    
    // 키워드 검색 (description에서 검색)
    @Query("SELECT gp FROM GroupPurchaseProject gp WHERE " +
           "gp.description LIKE %:keyword% " +
           "AND gp.status = :status " +
           "ORDER BY gp.createdAt DESC")
    Page<GroupPurchaseProject> findByKeywordAndStatus(@Param("keyword") String keyword, 
                                                       @Param("status") GroupPurchaseProject.ProjectStatus status, 
                                                       Pageable pageable);
    
    // 사업체 카테고리별 검색
    @Query("SELECT gp FROM GroupPurchaseProject gp " +
           "JOIN Business b ON gp.businessId = b.id " +
           "WHERE b.businessCategory = :category " +
           "AND gp.status = :status " +
           "ORDER BY gp.createdAt DESC")
    Page<GroupPurchaseProject> findByCategoryAndStatus(@Param("category") String category, 
                                                        @Param("status") GroupPurchaseProject.ProjectStatus status, 
                                                        Pageable pageable);
    
    // 지역별 검색 (위도/경도 기반)
    @Query("SELECT gp FROM GroupPurchaseProject gp " +
           "JOIN Business b ON gp.businessId = b.id " +
           "WHERE gp.status = :status " +
           "AND (6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(b.latitude)) * " +
           "COS(RADIANS(b.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(b.latitude)))) <= :radius " +
           "ORDER BY gp.createdAt DESC")
    Page<GroupPurchaseProject> findByLocationAndStatus(@Param("latitude") Double latitude,
                                                        @Param("longitude") Double longitude,
                                                        @Param("radius") Double radius,
                                                        @Param("status") GroupPurchaseProject.ProjectStatus status,
                                                        Pageable pageable);
    
    // 복합 검색 (키워드 + 카테고리 + 지역)
    @Query("SELECT gp FROM GroupPurchaseProject gp " +
           "JOIN Business b ON gp.businessId = b.id " +
           "WHERE (:keyword IS NULL OR gp.description LIKE %:keyword%) " +
           "AND (:category IS NULL OR b.businessCategory = :category) " +
           "AND gp.status = :status " +
           "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
           "(6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(b.latitude)) * " +
           "COS(RADIANS(b.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(b.latitude)))) <= :radius) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'endDate' THEN gp.endDate END ASC, " +
           "CASE WHEN :sortBy = 'createdAt' THEN gp.createdAt END DESC, " +
           "gp.createdAt DESC")
    Page<GroupPurchaseProject> findByComplexSearch(@Param("keyword") String keyword,
                                                    @Param("category") String category,
                                                    @Param("latitude") Double latitude,
                                                    @Param("longitude") Double longitude,
                                                    @Param("radius") Double radius,
                                                    @Param("status") GroupPurchaseProject.ProjectStatus status,
                                                    @Param("sortBy") String sortBy,
                                                    Pageable pageable);
    
    // 종료일이 임박한 프로젝트 조회
    @Query("SELECT gp FROM GroupPurchaseProject gp " +
           "WHERE gp.status = 'OPEN' " +
           "AND gp.endDate BETWEEN :now AND :deadline " +
           "ORDER BY gp.endDate ASC")
    List<GroupPurchaseProject> findProjectsEndingSoon(@Param("now") LocalDateTime now, 
                                                       @Param("deadline") LocalDateTime deadline);
    
    // 인기 프로젝트 조회 (참여자 수 기준)
    @Query("SELECT gp FROM GroupPurchaseProject gp " +
           "WHERE gp.status = 'OPEN' " +
           "ORDER BY (SELECT COUNT(gpp) FROM GroupPurchaseParticipant gpp " +
           "          WHERE gpp.projectId = gp.id AND gpp.status IN ('APPLIED', 'CONFIRMED')) DESC, " +
           "gp.createdAt DESC")
    Page<GroupPurchaseProject> findPopularProjects(Pageable pageable);
    
    // 목표 달성 임박 프로젝트 (참여자 수 기준으로 계산)
    @Query("SELECT gp FROM GroupPurchaseProject gp " +
           "WHERE gp.status = 'OPEN' " +
           "AND (SELECT COUNT(gpp) FROM GroupPurchaseParticipant gpp " +
           "     WHERE gpp.projectId = gp.id AND gpp.status IN ('APPLIED', 'CONFIRMED')) * 100.0 / gp.targetNumber >= :threshold " +
           "ORDER BY (SELECT COUNT(gpp) FROM GroupPurchaseParticipant gpp " +
           "          WHERE gpp.projectId = gp.id AND gpp.status IN ('APPLIED', 'CONFIRMED')) * 100.0 / gp.targetNumber DESC")
    Page<GroupPurchaseProject> findNearCompletionProjects(@Param("threshold") Double threshold, 
                                                           Pageable pageable);
    
    // 만료된 OPEN 상태 프로젝트 조회 (스케줄러용)
    @Query("SELECT gp FROM GroupPurchaseProject gp " +
           "WHERE gp.status = 'OPEN' " +
           "AND gp.endDate <= :now")
    List<GroupPurchaseProject> findExpiredOpenProjects(@Param("now") LocalDateTime now);
    
    // 상태별 프로젝트 수 조회
    Long countByStatus(GroupPurchaseProject.ProjectStatus status);
}
