package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// CouponProposal.java
@Entity
@Table(name = "coupon_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    private Long proposerId; // 제안자

    // 제안 조건 (예시 필드)
    private Integer discountPercent; // 할인율(%) 예: 20
    private Integer totalQuantity;   // 발급 수량
    private LocalDate startDate;
    private LocalDate endDate;

    private boolean acceptedByRequester;
    private boolean acceptedByRecipient;

    private LocalDateTime createdAt;
}
