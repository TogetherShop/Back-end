package com.togethershop.backend.controller;

import com.togethershop.backend.dto.ChatMessageDTO;
import com.togethershop.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

// ChatQueryController.java
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatQueryController {

    private final ChatService chatService;

    @GetMapping("/{roomId}/history")
    public Page<ChatMessageDTO> history(@PathVariable String roomId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "50") int size) {
        return chatService.history(roomId, page, size);
    }
}

