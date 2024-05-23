package com.jess.ragapp.chroma;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class QueryRequest {

    private final List<List<Float>> queryEmbeddings;
    private final int nResults;
    private final List<String> include = asList("metadatas", "documents", "distances", "embeddings");

    public QueryRequest(List<Float> queryEmbedding, int nResults) {
        this.queryEmbeddings = singletonList(queryEmbedding);
        this.nResults = nResults;
    }
}
