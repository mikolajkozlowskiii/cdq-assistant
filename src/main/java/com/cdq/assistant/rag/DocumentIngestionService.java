package com.cdq.assistant.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final PgVectorEmbeddingStore embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Value("${rag.document-path}")
    private String documentPath;

    @EventListener(ApplicationReadyEvent.class)
    public void ingest() {
        log.info("Starting document ingestion from: {}", documentPath);
        try {
            final String text = new ClassPathResource(documentPath)
                    .getContentAsString(StandardCharsets.UTF_8);
            log.debug("Loaded document '{}', size: {} characters", documentPath, text.length());

            embeddingStore.removeAll();

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(1000, 100))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(Document.from(text));
            log.info("Document ingestion completed successfully for: {}", documentPath);
        } catch (IOException e) {
            log.error("Failed to read document from classpath: {}", documentPath, e);
        }
    }
}
