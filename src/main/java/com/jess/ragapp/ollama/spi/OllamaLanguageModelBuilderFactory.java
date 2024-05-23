package com.jess.ragapp.ollama.spi;


import com.jess.ragapp.ollama.OllamaLanguageModel;

import java.util.function.Supplier;

/**
 * A factory for building {@link OllamaLanguageModel.OllamaLanguageModelBuilder} instances.
 */
public interface OllamaLanguageModelBuilderFactory extends Supplier<OllamaLanguageModel.OllamaLanguageModelBuilder> {
}
