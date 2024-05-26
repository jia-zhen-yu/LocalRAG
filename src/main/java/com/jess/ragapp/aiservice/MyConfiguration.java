package com.jess.ragapp.aiservice;

import com.jess.ragapp.chroma.ChromaClient;
import com.jess.ragapp.chroma.ChromaEmbeddingStore;
import com.jess.ragapp.ollama.OllamaChatModel;
import com.jess.ragapp.ollama.OllamaClient;
import com.jess.ragapp.ollama.OllamaEmbeddingModel;
import com.jess.ragapp.ollama.OllamaStreamingChatModel;
import com.jess.ragapp.shared.Assistant;
import com.jess.ragapp.shared.PersistentChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.cohere.CohereScoringModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MyConfiguration {

    private static final String CHROMA_BASE_URL = "http://localhost:8001";
    private static final String OLLAMA_BASE_URL = "http://localhost:11434";

    /**
     * This chat memory will be used by an {@link Assistant}
     */
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
    @Bean
    Assistant assistant(ChatMemory chatMemory) {
        return AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }


    @Bean
    EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(OLLAMA_BASE_URL)
                .modelName("mxbai-embed-large:latest")
                .timeout(Duration.ofMinutes(1))
                .maxRetries(3)
                .build();
    }

    @Bean
    ChatLanguageModel chatLanguageModel(){
        return OllamaChatModel.builder().baseUrl(OLLAMA_BASE_URL).modelName("gemma:2b").build();
    }
    @Bean
    StreamingChatLanguageModel streamingChatLanguageModel(){
        return OllamaStreamingChatModel.builder().
                baseUrl(OLLAMA_BASE_URL)
                .modelName("gemma:2b")
                .seed(111)
                .temperature(0.0)
                .build();
    }




    @Bean
    ChromaClient chromaClient() {
        return new ChromaClient(CHROMA_BASE_URL, Duration.ofMinutes(1));
    }
    @Bean
    OllamaClient ollamaClient(){
        return new OllamaClient(OLLAMA_BASE_URL, Duration.ofMinutes(1), null);
    }
    @Bean
    EmbeddingStore embeddingStore(){
        return new ChromaEmbeddingStore(CHROMA_BASE_URL, "ragapp", Duration.ofMinutes(1));
    }
    @Bean
    ScoringModel cohereScoringModel(){
        return CohereScoringModel.builder().baseUrl("http://localhost:8000/v1/").modelName("ms-marco-MiniLM-L-6-v2").apiKey("xxx").maxRetries(2).build();
    }

    @Bean
    PersistentChatMemoryStore persistentChatMemoryStore (){
        return new PersistentChatMemoryStore();
    }
    public static int count=0;

}
