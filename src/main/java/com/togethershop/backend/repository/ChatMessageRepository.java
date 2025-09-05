package com.togethershop.backend.repository;

import com.togethershop.backend.domain.ChatMessage;
import com.togethershop.backend.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByRoomRoomIdOrderByCreatedAtAsc(String roomId, Pageable pageable);

    Page<ChatMessage> findByRoom(ChatRoom room, Pageable pageable);

}

