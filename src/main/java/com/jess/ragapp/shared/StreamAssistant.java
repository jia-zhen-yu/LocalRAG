package com.jess.ragapp.shared;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface StreamAssistant {
    TokenStream chat(@MemoryId String memoryId, @UserMessage String message);
}
