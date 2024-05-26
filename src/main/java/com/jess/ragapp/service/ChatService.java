package com.jess.ragapp.service;

import com.jess.ragapp.chroma.ChromaEmbeddingStore;
import com.jess.ragapp.ollama.OllamaChatModel;
import com.jess.ragapp.ollama.OllamaEmbeddingModel;
import com.jess.ragapp.shared.Assistant;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.bge.small.en.v15.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jess.ragapp.shared.Utils.toPath;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Service
public class ChatService {

    private static Assistant createAssistant() {

        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

        // Let's create a separate embedding store specifically for biographies.
        EmbeddingStore<TextSegment> biographyEmbeddingStore =
                embed(toPath("documents/biography-of-john-doe.txt"), embeddingModel);
        ContentRetriever biographyContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(biographyEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.6)
                .build();

        // Additionally, let's create a separate embedding store dedicated to terms of use.
//        EmbeddingStore<TextSegment> termsOfUseEmbeddingStore =
//                embed(toPath("documents/miles-of-smiles-terms-of-use.txt"), embeddingModel);
//        ContentRetriever termsOfUseContentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(termsOfUseEmbeddingStore)
//                .embeddingModel(embeddingModel)
//                .maxResults(2)
//                .minScore(0.6)
//                .build();

        String modelName = "gemma:2b";

        // Build the ChatLanguageModel
        ChatLanguageModel chatLanguageModel =
                OllamaChatModel.builder().baseUrl("http://localhost:11434").modelName(modelName).build();


        // Let's create a query router.
        Map<ContentRetriever, String> retrieverToDescription = new HashMap<>();
        retrieverToDescription.put(biographyContentRetriever, "biography of John Doe");
//        retrieverToDescription.put(termsOfUseContentRetriever, "terms of use of car rental company");
        QueryRouter queryRouter = new LanguageModelQueryRouter(chatLanguageModel, retrieverToDescription);

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    private static EmbeddingStore<TextSegment> embed(Path documentPath, EmbeddingModel embeddingModel) {
        DocumentParser documentParser = new TextDocumentParser();
        Document document = loadDocument(documentPath, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }
    public void testchromadb(){

            EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                    .baseUrl("http:localhost:8001")
                    .collectionName("jiazhenyu")
                    .build();

//            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
            EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder().baseUrl("http://localhost:11434").
                    modelName("mxbai-embed-large:latest").timeout(Duration.ofDays(10000)).maxRetries(3).build();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
            List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
            EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

            System.out.println(embeddingMatch.score()); // 0.8144288493114709
            System.out.println(embeddingMatch.embedded().text()); // I like football.
        }


}
