package com.jess.ragapp.chroma;

import java.util.Map;

public class Collection {

    private String id;
    private String name;
    private Map<String, String> metadata;

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Map<String, String> metadata() {
        return metadata;
    }
}
