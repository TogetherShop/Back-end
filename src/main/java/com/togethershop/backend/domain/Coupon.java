package com.togethershop.backend.domain;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.CouponStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String couponCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Business business;

    private LocalDateTime issuedAt;
    private LocalDateTime expiredAt;
    private LocalDateTime usedAt;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private BigDecimal minimumOrderAmount;

    @Column(unique = true, nullable = false)
    private String jwtJti;
}
