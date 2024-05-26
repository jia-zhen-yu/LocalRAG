package com.jess.ragapp.service;

import com.jess.ragapp.chroma.ChromaEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.HuggingFaceTokenizer;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
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
    private final String BASE_URL = "http://localhost:8001";

    public Integer addKnowledge(String knowledgeName, String path){


        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(BASE_URL)
                .collectionName(knowledgeName)
                .timeout(Duration.ofSeconds(10))
                .build();


        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()

                // adding userId metadata entry to each Document to be able to filter by it later
                .documentTransformer(document -> {
                    Metadata metadata = document.metadata();
                    System.out.println(metadata);
                    document.metadata().put("userId", "12345");
                    return document;
                })

                // splitting each Document into TextSegments of 1000 tokens each, with a 200-token overlap
                .documentSplitter(DocumentSplitters.recursive(1000, 200, new HuggingFaceTokenizer()))

                // adding a name of the Document to each TextSegment to improve the quality of search
                .textSegmentTransformer(textSegment -> TextSegment.from(
                        textSegment.metadata("file_name") + "\n" + textSegment.text(),
                        textSegment.metadata()
                ))

                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();







        List<Document> documents = FileSystemDocumentLoader.loadDocuments(path);
        ingestor.ingest(documents);
//        DocumentSplitter documentSplitter = new DocumentByParagraphSplitter(50, 0);
//        List<TextSegment> textSegments = documentSplitter.splitAll(documents);
//        List<Embedding> content = embeddingModel.embedAll(textSegments).content();
//        List<String> ids = embeddingStore.addAll(content, textSegments);
        return 0;
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
