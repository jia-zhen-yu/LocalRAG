package com.jess.ragapp.ollama.spi;



import com.jess.ragapp.ollama.OllamaEmbeddingModel;

import java.util.function.Supplier;

/**
 * A factory for building {@link OllamaEmbeddingModel.OllamaEmbeddingModelBuilder} instances.
 */
public interface OllamaEmbeddingModelBuilderFactory extends Supplier<OllamaEmbeddingModel.OllamaEmbeddingModelBuilder> {
}
