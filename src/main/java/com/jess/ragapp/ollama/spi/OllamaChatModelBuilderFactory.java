package com.jess.ragapp.ollama.spi;


import com.jess.ragapp.ollama.OllamaChatModel;

import java.util.function.Supplier;

/**
 * A factory for building {@link OllamaChatModel.OllamaChatModelBuilder} instances.
 */
public interface OllamaChatModelBuilderFactory extends Supplier<OllamaChatModel.OllamaChatModelBuilder> {
}
