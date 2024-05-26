package com.jess.ragapp.controller;


import com.jess.ragapp.ollama.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController("model")
public class ModelController {
    @GetMapping("list")
    public void index() {
        //  返回模型列表

        OllamaClient ollamaClient = new OllamaClient("http://localhost:11434", Duration.ofMinutes(1), null);
        ModelsListResponse modelsListResponse = ollamaClient.listModels();
        List<OllamaModel> models = modelsListResponse.getModels();
        models.forEach(model -> System.out.println(model.getName()));

        OllamaModelCard ollamaModelCard = ollamaClient.showInformation(new ShowModelInformationRequest("mxbai-embed-large:latest"));
    }
    //
}
