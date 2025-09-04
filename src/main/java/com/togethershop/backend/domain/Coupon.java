package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Coupon.java (발급된 쿠폰 레코드)
@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String couponCode;
    private Integer discountPercent;
    private Integer totalQuantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private String itemName;
    private LocalDateTime issuedAt;
    private Long roomId; // 어떤 협의에서 만들어졌는지
    private Long ownerId;  // 누가 사용할 쿠폰인지
}

