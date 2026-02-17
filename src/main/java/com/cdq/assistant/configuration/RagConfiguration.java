package com.cdq.assistant.configuration;

import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RagConfiguration {

    @Value("${pgvector.host}")
    private String host;

    @Value("${pgvector.port}")
    private int port;

    @Value("${pgvector.database}")
    private String database;

    @Value("${pgvector.user}")
    private String user;

    @Value("${pgvector.password}")
    private String password;

    @Value("${pgvector.dimension}")
    private int dimension;

    @Value("${pgvector.table}")
    private String table;

    @Bean
    public PgVectorEmbeddingStore embeddingStore() {
        log.info("Initializing PgVector embedding store: host={}, port={}, database={}, table={}, dimension={}",
                host, port, database, table, dimension);
        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(user)
                .password(password)
                .dimension(dimension)
                .table(table)
                .createTable(true)
                .build();
    }
}
