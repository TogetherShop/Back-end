package com.togethershop.backend.repository;

import com.togethershop.backend.domain.Coupon;
import com.togethershop.backend.dto.CouponStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCouponCode(String couponCode);
    List<Coupon> findByCustomerIdAndStatus(Long customerId, CouponStatus status);
    @Query("SELECT c FROM Coupon c WHERE c.customerId = :customerId AND c.status = 'ISSUED' " +
            "AND c.expireDate >= CURRENT_TIMESTAMP ORDER BY c.expireDate ASC")
    List<Coupon> findExpiringCoupons(@Param("customerId") Long customerId, @Param("now") LocalDateTime now, org.springframework.data.domain.Pageable pageable);
    List<Coupon> findByExpireDateAndStatus(LocalDate expireDate, CouponStatus status);


}
