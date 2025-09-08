package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// CouponProposal.java
@Entity
@Table(name = "coupon_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @Column(name = "business_id", nullable = false)
    private Long businessId; // 제안자

    // 제안 조건 (예시 필드)
    @Column(name = "discount_value", nullable = false)
    private Long discountValue; // 할인율(%) 예: 20
    private Integer totalQuantity;   // 발급 수량
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    private boolean acceptedByRequester;
    private boolean acceptedByRecipient;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
