package com.cdq.assistant.configuration;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class EmbeddingConfiguration {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.embedding-model-name}")
    private String embeddingModelName;

    @Value("${ollama.timeout}")
    private int timeout;

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Initializing Ollama EmbeddingModel: model={}, baseUrl={}", embeddingModelName, baseUrl);
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(embeddingModelName)
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }
}
