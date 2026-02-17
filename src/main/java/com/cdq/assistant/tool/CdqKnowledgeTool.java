package com.cdq.assistant.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CdqKnowledgeTool {

    private final PgVectorEmbeddingStore embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Value("${rag.max-results}")
    private int maxResults;

    @Value("${rag.min-score}")
    private double minScore;

    @Tool("Searches the CDQ Fraud Guard knowledge base for product information. Use this for any question about CDQ, Fraud Guard, data quality, or business partner fraud detection.")
    public String searchCdqKnowledge(String query) {
        log.info("Searching CDQ knowledge base for: {}", query);

        final Embedding queryEmbedding = embeddingModel.embed(query).content();
        final EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(maxResults)
                        .minScore(minScore)
                        .build()
        );

        if (result.matches().isEmpty()) {
            log.info("No relevant CDQ knowledge found for query: {}", query);
            return "No relevant information found about: " + query;
        }

        final String response = result.matches().stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        log.info("Found {} CDQ knowledge matches for query: {}", result.matches().size(), query);
        return response;
    }
}
