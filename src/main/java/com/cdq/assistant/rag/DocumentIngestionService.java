package com.cdq.assistant.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    static final String SOURCE_METADATA_KEY = "source";

    private final PgVectorEmbeddingStore embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Value("${rag.knowledge-base-dir}")
    private String knowledgeBaseDir;

    @Value("${rag.file-glob}")
    private String fileGlob;

    @Value("${rag.chunk-size}")
    private int chunkSize;

    @Value("${rag.chunk-overlap}")
    private int chunkOverlap;

    @EventListener(ApplicationReadyEvent.class)
    public void ingest() {
        log.info("Starting document ingestion from directory: {}", knowledgeBaseDir);
        try {
            final Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:" + knowledgeBaseDir + "/" + fileGlob);

            if (resources.length == 0) {
                log.info("No documents found in directory: {}", knowledgeBaseDir);
                return;
            }

            final Embedding probe = embeddingModel.embed("probe").content();
            final EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            for (Resource resource : resources) {
                final String filename = resource.getFilename();
                if (isAlreadyIngested(filename, probe)) {
                    log.info("Document '{}' already ingested, skipping", filename);
                    continue;
                }
                ingestDocument(resource, filename, ingestor);
            }

            log.info("Document ingestion completed");
        } catch (IOException e) {
            log.error("Failed to scan knowledge base directory: {}", knowledgeBaseDir, e);
        }
    }

    private boolean isAlreadyIngested(String filename, Embedding probe) {
        final EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(probe)
                .filter(MetadataFilterBuilder.metadataKey(SOURCE_METADATA_KEY).isEqualTo(filename))
                .maxResults(1)
                .minScore(0.0)
                .build();
        return !embeddingStore.search(request).matches().isEmpty();
    }

    private void ingestDocument(Resource resource, String filename, EmbeddingStoreIngestor ingestor) throws IOException {
        final String text = resource.getContentAsString(StandardCharsets.UTF_8);
        log.debug("Loaded document '{}', size: {} characters", filename, text.length());

        ingestor.ingest(Document.from(text, Metadata.from(SOURCE_METADATA_KEY, filename)));
        log.info("Document '{}' ingested successfully", filename);
    }
}
