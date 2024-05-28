package com.jess.ragapp.shared;

import dev.langchain4j.service.SystemMessage;

public interface Assistant {

    @SystemMessage("Please summarize the following content, which is in Chinese. The summary should be provided in Chinese and must be no longer than 50 characters. The final output should only contain the summary.")
    String chat(String userMessage);
}