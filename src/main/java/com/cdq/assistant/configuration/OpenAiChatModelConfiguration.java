package com.cdq.assistant.configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("openai")
public class OpenAiChatModelConfiguration {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model-name}")
    private String modelName;

    @Value("${openai.temperature}")
    private double temperature;

    @Bean
    public ChatModel chatModel() {
        log.info("Initializing OpenAI ChatModel: model={}", modelName);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
