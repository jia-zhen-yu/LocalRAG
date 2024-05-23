package com.jess.ragapp.ollama;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ChatRequest {

    private String model;
    private List<Message> messages;
    private Options options;
    private String format;
    private Boolean stream;
}
