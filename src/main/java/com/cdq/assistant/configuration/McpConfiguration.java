package com.cdq.assistant.configuration;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class McpConfiguration {

    @Value("${mcp.weather.command}")
    private String command;

    @Value("${mcp.weather.working-directory}")
    private String workingDirectory;

    @Value("${mcp.weather.api-key}")
    private String weatherApiKey;

    @Value("${mcp.weather.api-url}")
    private String weatherApiUrl;

    private McpClient mcpClient;

    @Bean
    public McpTransport mcpTransport() {
        String absolutePath = Path.of(workingDirectory).toAbsolutePath().toString();
        String indexPath = Path.of(absolutePath, "src", "index.ts").toString();
        log.info("Configuring MCP stdio transport: command={}, workingDirectory={}", command, absolutePath);
        log.debug("MCP transport index path: {}", indexPath);
        Map<String, String> env = new HashMap<>(System.getenv());
        env.put("WEATHER_API_KEY", weatherApiKey);
        env.put("WEATHER_API_URL", weatherApiUrl);

        return new StdioMcpTransport.Builder()
                .command(List.of(command, "--prefix", absolutePath, "tsx", indexPath))
                .logEvents(true)
                .environment(env)
                .build();
    }

    @Bean
    public McpClient mcpClient(McpTransport mcpTransport) {
        log.info("Initializing MCP weather client...");
        this.mcpClient = new DefaultMcpClient.Builder()
                .transport(mcpTransport)
                .build();
        log.info("MCP weather client initialized successfully");
        return this.mcpClient;
    }

    @Bean
    public McpToolProvider mcpToolProvider(McpClient mcpClient) {
        log.info("Creating MCP tool provider");
        return McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();
    }

    @PreDestroy
    public void cleanup() {
        if (mcpClient != null) {
            log.info("Shutting down MCP client...");
            try {
                mcpClient.close();
                log.info("MCP client closed successfully");
            } catch (Exception e) {
                log.warn("Error closing MCP client during shutdown", e);
            }
        }
    }
}
