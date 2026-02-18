package com.cdq.assistant.configuration;

import com.cdq.assistant.assistant.AssistantAgent;
import com.cdq.assistant.tool.CdqKnowledgeTool;
import com.cdq.assistant.tool.CountryTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AgentConfiguration {

    @Bean
    public AssistantAgent assistantAgent(ChatModel chatModel,
                                         CountryTool countryTool,
                                         CdqKnowledgeTool cdqKnowledgeTool,
                                         McpToolProvider mcpToolProvider)
    {
        log.info("Building AssistantAgent with ChatModel, CountryTool, CdqKnowledgeTool, and McpToolProvider");
        AssistantAgent agent = AiServices.builder(AssistantAgent.class)
                .chatModel(chatModel)
                .tools(countryTool, cdqKnowledgeTool)
                .toolProvider(mcpToolProvider)
                .build();
        log.info("AssistantAgent initialized successfully");
        return agent;
    }
}
