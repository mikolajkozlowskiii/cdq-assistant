package com.cdq.assistant.tool;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CdqKnowledgeToolTest {

    @Mock
    private PgVectorEmbeddingStore embeddingStore;

    @Mock
    private EmbeddingModel embeddingModel;

    private CdqKnowledgeTool cdqKnowledgeTool;

    @BeforeEach
    void setUp() throws Exception {
        cdqKnowledgeTool = new CdqKnowledgeTool(embeddingStore, embeddingModel);
        setField("maxResults", 3);
        setField("minScore", 0.7);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = CdqKnowledgeTool.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(cdqKnowledgeTool, value);
    }

    @Test
    void shouldReturnMatchingKnowledge() {
        // given
        Embedding queryEmbedding = Embedding.from(new float[]{0.1f, 0.2f});
        when(embeddingModel.embed("What is Fraud Guard?"))
                .thenReturn(Response.from(queryEmbedding));

        EmbeddingSearchRequest expectedRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .minScore(0.7)
                .build();
        EmbeddingMatch<TextSegment> match1 = new EmbeddingMatch<>(
                0.9, "id1", queryEmbedding, TextSegment.from("Fraud Guard detects fraud."));
        EmbeddingMatch<TextSegment> match2 = new EmbeddingMatch<>(
                0.8, "id2", queryEmbedding, TextSegment.from("CDQ provides data quality."));
        when(embeddingStore.search(expectedRequest))
                .thenReturn(new EmbeddingSearchResult<>(List.of(match1, match2)));

        // when
        String result = cdqKnowledgeTool.searchCdqKnowledge("What is Fraud Guard?");

        // then
        assertThat(result)
                .contains("Fraud Guard detects fraud.")
                .contains("CDQ provides data quality.");
    }

    @Test
    void shouldReturnNotFoundMessageWhenNoMatches() {
        // given
        Embedding queryEmbedding = Embedding.from(new float[]{0.1f, 0.2f});
        when(embeddingModel.embed("unknown topic"))
                .thenReturn(Response.from(queryEmbedding));
        EmbeddingSearchRequest expectedRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .minScore(0.7)
                .build();
        when(embeddingStore.search(expectedRequest))
                .thenReturn(new EmbeddingSearchResult<>(List.of()));

        // when
        String result = cdqKnowledgeTool.searchCdqKnowledge("unknown topic");

        // then
        assertThat(result).isEqualTo("No relevant information found about: unknown topic");
    }

    @Test
    void shouldJoinMultipleMatchesWithDoubleNewline() {
        // given
        Embedding queryEmbedding = Embedding.from(new float[]{0.1f, 0.2f});
        when(embeddingModel.embed("CDQ")).thenReturn(Response.from(queryEmbedding));

        EmbeddingSearchRequest expectedRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .minScore(0.7)
                .build();
        EmbeddingMatch<TextSegment> match1 = new EmbeddingMatch<>(
                0.9, "id1", queryEmbedding, TextSegment.from("First segment."));
        EmbeddingMatch<TextSegment> match2 = new EmbeddingMatch<>(
                0.8, "id2", queryEmbedding, TextSegment.from("Second segment."));
        when(embeddingStore.search(expectedRequest))
                .thenReturn(new EmbeddingSearchResult<>(List.of(match1, match2)));

        // when
        String result = cdqKnowledgeTool.searchCdqKnowledge("CDQ");

        // then
        assertThat(result).isEqualTo("First segment.\n\nSecond segment.");
    }
}
