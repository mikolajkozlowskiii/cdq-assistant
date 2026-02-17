package com.cdq.assistant.controller;

import com.cdq.assistant.dto.ChatResponse;
import com.cdq.assistant.dto.SourceInfo;
import com.cdq.assistant.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void shouldReturnResponseWhenMessageIsValid() throws Exception {
        // given
        ChatResponse response = ChatResponse.of("Paris is the capital of France", List.of(
                new SourceInfo("getCountryData", "{\"countryName\":\"France\"}")
        ));
        when(chatService.ask("What is the capital of France?")).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"What is the capital of France?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Paris is the capital of France"))
                .andExpect(jsonPath("$.usedSources[0].name").value("getCountryData"));
    }

    @Test
    void shouldReturnBadRequestWhenMessageIsBlank() throws Exception {
        // given
        String blankMessageRequest = "{\"message\":\"   \"}";

        // when & then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankMessageRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenMessageIsNull() throws Exception {
        // given
        String nullMessageRequest = "{}";

        // when & then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullMessageRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceThrows() throws Exception {
        // given
        when(chatService.ask("fail")).thenThrow(new RuntimeException("unexpected"));

        // when & then
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"fail\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.answer").value("An error occurred while processing your request"));
    }
}
