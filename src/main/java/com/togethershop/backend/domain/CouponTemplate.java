package com.togethershop.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partnership_id", nullable = false)
    private Partnership partnership;

    @Column(name = "applicable_business_id", nullable = false)
    private Long applicableBusinessId; // 쿠폰을 사용할 수 있는 가게

    @Column(name = "discount_value", nullable = false)
    private Long discountValue; // 할인율(%)

    @Column(name = "total_quantity")
    private Integer totalQuantity;   // 발급 수량

    @Column(name = "current_quantity")
    private Integer currentQuantity; // 현재 남은 수량

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "item", columnDefinition = "TEXT")
    private String item; // 쿠폰 적용 품목/설명

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
