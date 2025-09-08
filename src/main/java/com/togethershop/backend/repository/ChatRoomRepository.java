package com.togethershop.backend.repository;

import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.dto.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);
    List<ChatRoom> findByPartnershipIdAndStatus(Long partnershipId, ChatStatus status);

    List<ChatRoom> findByPartnershipIdIn(List<Long> partnershipId);
    List<ChatRoom> findByRequesterIdOrRecipientIdOrderByCreatedAtDesc(Long requesterId, Long recipientId);
}
