package com.togethershop.backend.domain;

import com.togethershop.backend.dto.GroupPurchaseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grouppurchase_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupPurchaseParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupPurchaseStatus status = GroupPurchaseStatus.APPLIED;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

}
