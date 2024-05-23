package com.jess.ragapp.controller;

import com.jess.ragapp.service.KnowService;
import com.jess.ragapp.shared.Assistant;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/know")
public class KnowController {

    @Autowired
    EmbeddingModel embeddingModel;
    @Autowired
    KnowService knowService;

    @Autowired
    Assistant assistant;

    @PutMapping("/add")
    public String know(@RequestParam(value = "dirPath", defaultValue = "", required = false) String dirPath, @RequestParam(value = "knowledge") String knowledgeName) {
        Integer i = knowService.addKnowledge(knowledgeName, dirPath);
        log.info("Added {} embeddings", i);

        return "add";
    }
}

