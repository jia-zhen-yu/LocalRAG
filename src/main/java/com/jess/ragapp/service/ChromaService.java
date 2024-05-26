package com.jess.ragapp.service;

import com.jess.ragapp.chroma.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class ChromaService {
    @Autowired
    private ChromaClient chromaClient;
    public void getCollection(String name)
    {
        Collection collection = chromaClient.collection("jess");
        System.out.println(collection);
    }
    public Boolean addEmbeddings(String collectionId, AddEmbeddingsRequest addEmbeddingsRequest){
         return chromaClient.addEmbeddings(collectionId, addEmbeddingsRequest);
    }
    public void createCollection(String collectionName){
        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest(collectionName);
        Collection collection = chromaClient.createCollection(createCollectionRequest);
    }
    public void queryCollection(String collectionId, QueryRequest queryRequest){
        QueryResponse queryResponse = chromaClient.queryCollection(collectionId, queryRequest);
    }
}
