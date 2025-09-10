package com.togethershop.backend.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.togethershop.backend.dto.PartnershipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "partnerships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partnership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partnership_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @JsonBackReference
    private Business requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_business_id", nullable = false)
    @JsonBackReference
    private Business partner;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "partnership", cascade = CascadeType.ALL)
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    private PartnershipStatus status = PartnershipStatus.REQUESTED;

}

