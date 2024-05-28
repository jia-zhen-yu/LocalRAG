package com.jess.ragapp.shared;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface StreamAssistant {
    @SystemMessage("用中文回答")
    TokenStream chat(@MemoryId String memoryId, @UserMessage String message);

    TokenStream chatModel(@MemoryId String memoryId, @UserMessage String message);
}
