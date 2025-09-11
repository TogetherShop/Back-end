package com.togethershop.backend.repository;

import com.togethershop.backend.domain.ChatMessage;
import com.togethershop.backend.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByRoomRoomIdOrderBySentAtAsc(String roomId, Pageable pageable);

    Page<ChatMessage> findByRoom(ChatRoom room, Pageable pageable);

    Optional<ChatMessage> findTopByRoomRoomIdOrderBySentAtDesc(String roomId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.deliveryStatus = 'READ' " +
            "WHERE m.room.roomId = :roomId AND m.senderId <> :userId " +
            "AND m.deliveryStatus <> 'READ'")
    int markMessagesAsRead(@Param("roomId") String roomId, @Param("userId") Long userId);

}

