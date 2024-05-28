package com.jess.ragapp.service;

import com.jess.ragapp.chroma.ChromaEmbeddingStore;
import com.jess.ragapp.shared.Assistant;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.HuggingFaceTokenizer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
@Slf4j
@Service
public class KnowService {

    @Autowired
    EmbeddingModel embeddingModel;

    @Autowired
    EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    ChatLanguageModel chatLanguageModel;
    private final String BASE_URL = "http://localhost:8001";

    public Integer addKnowledge(String knowledgeName, String path) {


        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(BASE_URL)
                .collectionName(knowledgeName)
                .timeout(Duration.ofSeconds(10))
                .build();


        Assistant assistant = AiServices.builder(Assistant.class).chatLanguageModel(chatLanguageModel).build();


        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()

                // adding userId metadata entry to each Document to be able to filter by it later
                .documentTransformer(document -> {
                    Metadata metadata = document.metadata();
                    log.info("原数据{}",document.metadata());
//                    document.metadata().put("userId", "12345");
                    return document;
                })

                // splitting each Document into TextSegments of 1000 tokens each, with a 200-token overlap
                .documentSplitter(DocumentSplitters.recursive(250, 50, new HuggingFaceTokenizer()))

                // adding a name of the Document to each TextSegment to improve the quality of search
                .textSegmentTransformer(textSegment -> {
                    String chat = assistant.chat(textSegment.text());
                    textSegment.metadata().add("summary", chat);
                    log.info("分割数据summary:{}",chat);
                    return TextSegment.from(
                            textSegment.metadata("file_name") + "\n" + chat + "\n" + textSegment.text(),
                            textSegment.metadata()
                    );
                })


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

}
