package com.togethershop.backend.service;

import com.togethershop.backend.domain.Coupon;
import com.togethershop.backend.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepo;

    @Transactional
    public void issueMutualCoupons(String roomId, Long proposerId, Long accepterId, Map<String, Object> proposalPayload) {
        Map<String, Object> proposerCouponMap = (Map<String, Object>) proposalPayload.get("proposerCoupon");
        Map<String, Object> recipientCouponMap = (Map<String, Object>) proposalPayload.get("recipientCoupon");

        Coupon proposerCoupon = Coupon.builder()
                .couponCode("CPN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .discountPercent((Long) proposerCouponMap.get("discountPercent"))
                .totalQuantity((Long) proposerCouponMap.get("totalQuantity"))
                .issueDate(LocalDate.parse((String) proposerCouponMap.get("startDate")))
                .expireDate(LocalDate.parse((String) proposerCouponMap.get("endDate")))
                .itemName((String) proposerCouponMap.get("itemName"))
                .issuedAt(LocalDateTime.now())
                .roomId(Long.parseLong(roomId))  // 필요하면 Long 변환
                .businessId(proposerId)
                .build();

        Coupon accepterCoupon = Coupon.builder()
                .couponCode("CPN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .discountPercent((Long) recipientCouponMap.get("discountPercent"))
                .totalQuantity((Long) recipientCouponMap.get("totalQuantity"))
                .issueDate(LocalDate.parse((String) recipientCouponMap.get("startDate")))
                .expireDate(LocalDate.parse((String) recipientCouponMap.get("endDate")))
                .itemName((String) recipientCouponMap.get("itemName"))
                .issuedAt(LocalDateTime.now())
                .roomId(Long.parseLong(roomId))
                .businessId(accepterId)
                .build();

        couponRepo.save(proposerCoupon);
        couponRepo.save(accepterCoupon);
    }


}

