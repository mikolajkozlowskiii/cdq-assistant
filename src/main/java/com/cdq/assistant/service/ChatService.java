package com.cdq.assistant.service;

import com.cdq.assistant.assistant.AssistantAgent;
import com.cdq.assistant.dto.ChatResponse;
import com.cdq.assistant.dto.SourceInfo;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final AssistantAgent agent;

    public ChatResponse ask(String message) {
        log.info("Processing message: {}", message);

        final Result<String> result = agent.chat(message);
        final List<SourceInfo> usedSources = extractSources(result);

        log.info("Response generated, {} source(s) used", usedSources.size());
        usedSources.forEach(s -> log.info("{} - {}", s.name(), s.detail()));

        return ChatResponse.of(result.content(), usedSources);
    }

    private List<SourceInfo> extractSources(Result<String> result) {
        return result.toolExecutions().stream()
                .map(this::classify)
                .toList();
    }

    private SourceInfo classify(ToolExecution execution) {
        final String toolName = execution.request().name();
        final String args = execution.request().arguments();
        log.debug("Classified tool execution '{}' with args {}", toolName, args);
        return new SourceInfo(toolName, args);
    }
}
