package com.cdq.assistant.configuration;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Slf4j
@Configuration
@Profile("ollama")
public class OllamaChatModelConfiguration {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model-name}")
    private String chatModelName;

    @Value("${ollama.timeout}")
    private int timeout;

    @Value("${ollama.temperature}")
    private double temperature;

    @Bean
    public ChatModel chatModel() {
        log.info("Initializing Ollama ChatModel: model={}, baseUrl={}, timeout={}s", chatModelName, baseUrl, timeout);
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(chatModelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
