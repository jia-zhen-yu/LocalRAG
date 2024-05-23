package com.jess.ragapp.chroma;

import java.util.HashMap;
import java.util.Map;

public class CreateCollectionRequest {

    private final String name;
    private final Map<String, String> metadata;

    /**
     * Currently, cosine distance is always used as the distance method for chroma implementation
     */
    public CreateCollectionRequest(String name) {
        this.name = name;
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("hnsw:space", "cosine");
        this.metadata = metadata;
    }
}
