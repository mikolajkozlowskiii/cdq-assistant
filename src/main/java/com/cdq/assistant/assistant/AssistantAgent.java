package com.cdq.assistant.assistant;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;

public interface AssistantAgent {

    @SystemMessage("""
            You are a helpful AI assistant with access to tools:
            1. getCountryData — ALWAYS use this for ANY question about countries (e.g. capitals). NEVER answer country questions from your own knowledge — you MUST call the tool first, even if you know capital of country, check with that tool.
            NEVER answer from your own knowledge when a tool can provide the answer. Always call the appropriate tool first, then provide a concise answer based on the retrieved information. Keep responses short and focused — only include details directly relevant to the user's question. Do not dump all retrieved content.
            """)
    Result<String> chat(String message);
}
