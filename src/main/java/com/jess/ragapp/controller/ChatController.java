package com.jess.ragapp.controller;

import com.jess.ragapp.chroma.ChromaEmbeddingStore;
import com.jess.ragapp.service.KnowService;
import com.jess.ragapp.shared.Assistant;
import com.jess.ragapp.shared.PersistentChatMemoryStore;
import com.jess.ragapp.shared.StreamAssistant;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private static final String BASE_CHROMA_URL = "http://localhost:8001";

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    @Autowired
    EmbeddingModel embeddingModel;


    @Autowired
    ChatLanguageModel chatLanguageModel;
    @Autowired
    KnowService knowService;
    @Autowired
    ScoringModel cohereScoringModel;
    @Autowired
    StreamingChatLanguageModel streamingChatModel;
    @Autowired
    PersistentChatMemoryStore store;


    private static ConcurrentHashMap<String, StreamAssistant> assistantMap = new ConcurrentHashMap<>();

    @GetMapping("/know")
    public String ragApp(@RequestParam(value = "knowledgeName", defaultValue = "jessyyds2") String knowledgeName,
                         @RequestParam(value = "userMessage", defaultValue = "刘晓丹是谁？") String userMessage,
                         @RequestParam(value = "chatUUID", defaultValue = "uuid") String chatUUID) {

        StreamAssistant assistant = null;
       if(assistantMap.containsKey(chatUUID)) {
           assistant = assistantMap.get(chatUUID);
       }else {
           assistant=createStreamAssistant(knowledgeName,userMessage);
           assistantMap.put(chatUUID,assistant);
       }
        TokenStream chat = assistant.chat(chatUUID,userMessage);

        chat.onNext(System.out::println).onComplete(System.out::println).ignoreErrors().start();

        return "success";


    }
    public StreamAssistant createStreamAssistant(String knowledgeName,String userMessage) {

        // 创建了一个 EmbeddingStore，用于存储文本段的嵌入（Embeddings）。ChromaEmbeddingStore 使用 BASE_CHROMA_URL 作为基础 URL，并指定了知识库的名称 knowledgeName。超时时间设置为10秒。
        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(BASE_CHROMA_URL)
                .collectionName(knowledgeName)
                .timeout(Duration.ofSeconds(10))
                .build();

//        Response<Embedding> embed = embeddingModel.embed(userMessage);
//        List<EmbeddingMatch<TextSegment>> embeddingMatches = knowService.queryKnowledge(knowledgeName, embed.content());
//        log.info(embeddingMatches.toString());



        // 创建了一个 QueryTransformer，用于压缩和优化用户的查询消息 userMessage。CompressingQueryTransformer
        // 使用了一个语言模型 chatLanguageModel。然后将用户的消息转换为查询对象 query 并进行转换。
        QueryTransformer queryTransformer = new CompressingQueryTransformer(chatLanguageModel);
//        Query query = new Query(userMessage);
//        Collection<Query> transform = queryTransformer.transform(query);
//        log.info(transform.toString());
//        Function<Query, Filter> filterByUserId =
//                (query2) -> metadataKey("userId").isEqualTo(query2.metadata().chatMemoryId().toString());

//        Function<Query, Double> dynamicMinScore = (query) -> {
//            // 根据用户的查询消息 userMessage 和 embeddingMatches，计算一个动态的最小得分。
//            // 如果用户消息包含"降维"，则最小得分为0.6；否则为0.3。
//
//            query.metadata().chatMemory().
//            if (userMessage)) {
//                return 0.6;
//            } else {
//                return 0.3;
//            }
//        };




        // 创建了一个 ContentRetriever，用于根据嵌入模型 embeddingModel 从 embeddingStore 中检索内容。
        // 最多返回5个结果，并且只返回得分超过0.6的结果。
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
//                .dynamicFilter(filterByUserId)
//                .dynamicMinScore()
                .maxResults(1)
                .minScore(0.8)
                .build();

//        ContentAggregator contentAggregator = ReRankingContentAggregator.builder()
//                .scoringModel(cohereScoringModel)
//                .minScore(0.6) // we want to present the LLM with only the truly relevant segments for the user's query
//                .build();



        // 这里创建了一个 ContentInjector，用于将元数据（如文件名和索引）注入到返回的内容中。

        ContentInjector contentInjector = DefaultContentInjector.builder()
                // .promptTemplate(...) // Formatting can also be changed
                .metadataKeysToInclude(asList("file_name", "index"))
                .build();





        // 创建了一个 RetrievalAugmentor，将查询转换器、内容检索器和内容注入器组合在一起，用于增强内容检索和处理。

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .contentRetriever(contentRetriever)
//                .contentAggregator(contentAggregator)
                .contentInjector(contentInjector)
                .build();


        // 创建了一个 ChatMemoryProvider，用于管理聊天记忆。每个聊天记忆有一个最大消息数限制（10条），并存储在 store 中。

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();



        StreamAssistant assistant = AiServices.builder(StreamAssistant.class)
                .streamingChatLanguageModel(streamingChatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
        return assistant;

    }


//    @GetMapping("/model")
//    public void chatModel2() {
//        List<Document> documents = FileSystemDocumentLoader.loadDocuments("/home/jess/ragLLM/testdoc");
//        DocumentSplitter documentSplitter = new DocumentByParagraphSplitter(300, 0);
//        List<TextSegment> textSegments = documentSplitter.splitAll(documents);
//
//
//        EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore.builder()
//                .baseUrl("http:localhost:8001")
//                .collectionName("jiazhenyu")
//                .build();
//
//        EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder().baseUrl("http://localhost:11434").
//                modelName("mxbai-embed-large:latest").timeout(Duration.ofDays(10)).maxRetries(3).build();
//
//        List<Embedding> content = embeddingModel.embedAll(textSegments).content();
//        embeddingStore.addAll(content, textSegments);
//
//
//
//        String modelName = "gemma:2b";
//
//// Build the ChatLanguageModel
//        ChatLanguageModel chatLanguageModel =
//                OllamaChatModel.builder().baseUrl("http://localhost:11434").modelName(modelName).build();
//
//
//        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(embeddingStore)
//                .embeddingModel(embeddingModel)
//                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
//                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
//                .build();
//
//        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
//
//
//        Assistant assistant = AiServices.builder(Assistant.class)
//                .chatLanguageModel(chatLanguageModel)
//                .contentRetriever(contentRetriever)
//                .chatMemory(chatMemory)
//                .build();
//
//
//
//        List<Content> retrieve = contentRetriever.retrieve(new Query("降维是什么？"));
//        retrieve.forEach(contentTmp -> System.out.println(contentTmp.textSegment().text()));
//
//        String chat = assistant.chat("降维是什么？");
//
//
////        String answer = model.generate("Provide 3 short bullet points explaining why Java is awesome");
//        System.out.println(chat);
//    }


    @GetMapping("/chatModel")
    public String  chatModel(@RequestParam(value = "userMessage", defaultValue = "降维是什么？") String userMessage) {

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String chat = assistant.chat(userMessage);
        log.info(chat);
        return chat;


    }

    @GetMapping("/streamChatModel2")
    public String  streamChatModel(@RequestParam(value = "userMessage", defaultValue = "降维是什么？") String userMessage,@RequestParam(value = "chatUUID", defaultValue = "uuidChat") String chatUUID) throws InterruptedException {
        StreamAssistant streamAssistant=null;

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();

        if(assistantMap.containsKey(chatUUID)) {
            streamAssistant = assistantMap.get(chatUUID);
        }else {
//            assistant=AiServices.builder(StreamAssistant.class)
//                    .streamingChatLanguageModel(streamingChatModel)
//                    .chatMemoryProvider(chatMemoryProvider)
//                    .build();
            streamAssistant = AiServices.create(StreamAssistant.class, streamingChatModel);
            assistantMap.put(chatUUID,streamAssistant);
        }

        TokenStream chat = streamAssistant.chatModel(chatUUID, userMessage);
//        chat.onNext(System.out::println).onComplete((response) -> log.info("Chat completed"+response.toString())).onError(t -> log.error("Chat failed", t));
        chat.onNext(System.out::println);

//        Thread.sleep(50000);
        return "success";


    }
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
