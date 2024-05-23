package com.jess.ragapp.ollama.spi;



import com.jess.ragapp.ollama.OllamaStreamingChatModel;

import java.util.function.Supplier;

/**
 * A factory for building {@link OllamaStreamingChatModel.OllamaStreamingChatModelBuilder} instances.
 */
public interface OllamaStreamingChatModelBuilderFactory extends Supplier<OllamaStreamingChatModel.OllamaStreamingChatModelBuilder> {
}
