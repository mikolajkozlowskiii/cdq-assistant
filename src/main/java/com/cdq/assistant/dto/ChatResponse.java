package com.cdq.assistant.dto;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        String answer,
        Instant timestamp,
        List<SourceInfo> usedSources
) {
    public static ChatResponse of(String answer, List<SourceInfo> sources) {
        return new ChatResponse(answer, Instant.now(), sources);
    }

    public static ChatResponse error(String message) {
        return new ChatResponse(message, Instant.now(), List.of());
    }
}
