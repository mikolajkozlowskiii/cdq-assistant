package com.cdq.assistant.service;

import com.cdq.assistant.assistant.AssistantAgent;
import com.cdq.assistant.dto.ChatResponse;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private AssistantAgent agent;

    @InjectMocks
    private ChatService chatService;

    @Test
    void shouldReturnResponseWithSources() {
        // given
        ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                .name("getCountryData")
                .arguments("{\"countryName\":\"France\"}")
                .build();
        ToolExecution toolExecution = ToolExecution.builder()
                .request(toolRequest)
                .result(ToolExecutionResult.builder().resultText("country data json").build())
                .build();
        Result<String> agentResult = new Result<>("Paris is the capital", null, null,
                null, List.of(toolExecution));
        when(agent.chat("What is the capital of France?")).thenReturn(agentResult);

        // when
        ChatResponse response = chatService.ask("What is the capital of France?");

        // then
        assertThat(response.answer()).isEqualTo("Paris is the capital");
        assertThat(response.usedSources()).hasSize(1);
        assertThat(response.usedSources().getFirst().name()).isEqualTo("getCountryData");
        assertThat(response.usedSources().getFirst().detail()).isEqualTo("{\"countryName\":\"France\"}");
    }

    @Test
    void shouldReturnEmptySourcesWhenNoToolExecutions() {
        // given
        Result<String> agentResult = new Result<>("Hello!", null, null, null, null);
        when(agent.chat("Hello")).thenReturn(agentResult);

        // when
        ChatResponse response = chatService.ask("Hello");

        // then
        assertThat(response.answer()).isEqualTo("Hello!");
        assertThat(response.usedSources()).isEmpty();
    }

    @Test
    void shouldReturnEmptySourcesWhenToolExecutionsListIsEmpty() {
        // given
        Result<String> agentResult = new Result<>("Hi!", null, null, null, List.of());
        when(agent.chat("Hi")).thenReturn(agentResult);

        // when
        ChatResponse response = chatService.ask("Hi");

        // then
        assertThat(response.answer()).isEqualTo("Hi!");
        assertThat(response.usedSources()).isEmpty();
    }
}
