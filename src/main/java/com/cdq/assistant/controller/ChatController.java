package com.cdq.assistant.controller;

import com.cdq.assistant.dto.ChatRequest;
import com.cdq.assistant.dto.ChatResponse;
import com.cdq.assistant.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse getChatResponse(@Valid @RequestBody ChatRequest request) {
        return chatService.ask(request.message());
    }
}
