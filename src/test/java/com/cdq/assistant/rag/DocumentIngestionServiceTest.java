package com.cdq.assistant.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

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
        setField("test-document.txt");
    }

    private void setField(Object value) throws Exception {
        Field field = DocumentIngestionService.class.getDeclaredField("documentPath");
        field.setAccessible(true);
        field.set(documentIngestionService, value);
    }

    @Test
    void shouldClearStoreAndIngestDocument() {
        // given
        when(embeddingModel.embedAll(anyList()))
                .thenReturn(Response.from(List.of(embedding)));

        // when
        documentIngestionService.ingest();

        // then
        InOrder inOrder = inOrder(embeddingStore);
        inOrder.verify(embeddingStore).removeAll();
        inOrder.verify(embeddingStore).addAll(anyList(), anyList());
    }

    @Test
    void shouldNotIngestWhenDocumentPathIsInvalid() throws Exception {
        // given
        setField("nonexistent-file.txt");

        // when
        documentIngestionService.ingest();

        // then
        verify(embeddingStore, never()).removeAll();
        verify(embeddingStore, never()).addAll(anyList(), anyList());
    }
}
