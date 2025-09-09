package com.togethershop.backend.repository;

import com.togethershop.backend.domain.CustomerNotification;
import com.togethershop.backend.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {

    @Query("SELECT n FROM CustomerNotification n WHERE n.customerId = :customerId AND n.status NOT IN ('READ', 'CLICKED')")
    List<CustomerNotification> findByCustomerIdAndStatus(Long customerId, NotificationStatus status);

    // 고객별 알림 목록 조회 (예: 최신순)
    List<CustomerNotification> findAllByCustomerIdOrderBySentAtDesc(Long customerId);

    // 상태별 필터링 조회 (필요시 추가)
    List<CustomerNotification> findAllByCustomerIdAndStatusOrderBySentAtDesc(Long customerId, String status);
}