package com.togethershop.backend.repository;

import java.util.List;

import com.togethershop.backend.domain.BusinessNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessNotificationRepository extends JpaRepository<BusinessNotification, Long> {

    @Query("SELECT n FROM BusinessNotification n WHERE n.businessId = :businessId AND n.status NOT IN ('READ', 'CLICKED')")
    List<BusinessNotification> findUnreadNotifications(@Param("businessId") Long businessId);
}
