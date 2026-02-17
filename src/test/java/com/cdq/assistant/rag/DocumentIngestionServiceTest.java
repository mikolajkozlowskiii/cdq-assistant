package com.cdq.assistant.rag;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

    @Mock
    private PgVectorEmbeddingStore embeddingStore;

    @Mock
    private EmbeddingModel embeddingModel;

    private DocumentIngestionService documentIngestionService;

    private final Embedding embedding = Embedding.from(new float[]{0.1f, 0.2f});

    @BeforeEach
    void setUp() throws Exception {
        documentIngestionService = new DocumentIngestionService(embeddingStore, embeddingModel);
        setField("knowledgeBaseDir", "cdq-knowledge-base");
        setField("fileGlob", "*.*");
        setField("chunkSize", 1000);
        setField("chunkOverlap", 100);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = DocumentIngestionService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(documentIngestionService, value);
    }

    @Test
    void shouldIngestNewDocument() {
        // given
        when(embeddingModel.embed("probe")).thenReturn(Response.from(embedding));
        when(embeddingStore.search(any(EmbeddingSearchRequest.class)))
                .thenReturn(new EmbeddingSearchResult<>(List.of()));
        when(embeddingModel.embedAll(anyList()))
                .thenReturn(Response.from(List.of(embedding)));

        // when
        documentIngestionService.ingest();

        // then
        verify(embeddingStore).addAll(anyList(), anyList());
    }

    @Test
    void shouldSkipAlreadyIngestedDocument() {
        // given
        when(embeddingModel.embed("probe")).thenReturn(Response.from(embedding));
        EmbeddingMatch<TextSegment> existingMatch = new EmbeddingMatch<>(
                0.5, "id1", embedding, TextSegment.from("existing content"));
        when(embeddingStore.search(any(EmbeddingSearchRequest.class)))
                .thenReturn(new EmbeddingSearchResult<>(List.of(existingMatch)));

        // when
        documentIngestionService.ingest();

        // then
        verify(embeddingStore, never()).addAll(anyList(), anyList());
    }

    @Test
    void shouldDoNothingWhenDirectoryDoesNotExist() throws Exception {
        // given
        setField("knowledgeBaseDir", "nonexistent-directory");

        // when
        documentIngestionService.ingest();

        // then
        verify(embeddingStore, never()).addAll(anyList(), anyList());
        verify(embeddingModel, never()).embed("probe");
    }
}
