package com.jess.ragapp.ollama.spi;


import com.jess.ragapp.ollama.OllamaStreamingLanguageModel;

import java.util.function.Supplier;

/**
 * A factory for building {@link OllamaStreamingLanguageModel.OllamaStreamingLanguageModelBuilder} instances.
 */
public interface OllamaStreamingLanguageModelBuilderFactory extends Supplier<OllamaStreamingLanguageModel.OllamaStreamingLanguageModelBuilder> {
}
