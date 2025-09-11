package com.togethershop.backend.repository;

import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.dto.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);

    List<ChatRoom> findByPartnershipIdInAndStatus(List<Long> partnershipIds, ChatStatus status);

    List<ChatRoom> findByPartnershipIdIn(List<Long> partnershipId);

    List<ChatRoom> findByRequesterIdOrRecipientIdOrderByCreatedAtDesc(Long requesterId, Long recipientId);


    @Query("SELECT cr FROM ChatRoom cr WHERE " +
            "(cr.requester.id = :businessId OR cr.recipient.id = :businessId) " +
            "AND cr.status = :status")
    List<ChatRoom> findByBusinessIdAndStatus(@Param("businessId") Long businessId,
                                             @Param("status") ChatStatus status);

    List<ChatRoom> findByPartnershipId(Long partnershipId);
}
