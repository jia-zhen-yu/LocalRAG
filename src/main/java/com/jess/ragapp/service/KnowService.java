package com.jess.ragapp.service;

import com.jess.ragapp.chroma.ChromaEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class KnowService {

    @Autowired
    EmbeddingModel embeddingModel;

    @Autowired
    EmbeddingStore<TextSegment> embeddingStore;

    public Integer addKnowledge(String knowledgeName, String path){
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl("http://localhost:8000")
                .collectionName(knowledgeName)
                .timeout(Duration.ofSeconds(10))
                .build();
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(path);
        DocumentSplitter documentSplitter = new DocumentByParagraphSplitter(50, 0);
        List<TextSegment> textSegments = documentSplitter.splitAll(documents);
        List<Embedding> content = embeddingModel.embedAll(textSegments).content();
        List<String> ids = embeddingStore.addAll(content, textSegments);
        return ids.size();
    }
    public List<EmbeddingMatch<TextSegment>> queryKnowledge(String knowledgeName,Embedding referenceEmbedding){
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl("http://localhost:8001")
                .collectionName(knowledgeName)
                .timeout(Duration.ofSeconds(10))
                .build();
        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(referenceEmbedding, 4, 0.5);
        return relevant;
    }

}
