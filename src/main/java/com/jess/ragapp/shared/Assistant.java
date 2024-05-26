package com.jess.ragapp.shared;

import dev.langchain4j.service.SystemMessage;

public interface Assistant {

    @SystemMessage("You are a polite assistant")
    String chat(String userMessage);
}