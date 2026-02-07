package com.poppano.gpt.tool;

import com.poppano.gpt.component.SearchEngineDocumentRetriever;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Component
public class Tools {

    private final ChatClient.Builder chatClientBuilder;
    private final RestClient.Builder restClientBuilder;
    private final VectorStore vectorStore;

    public Tools(ChatClient.Builder chatClientBuilder, RestClient.Builder restClientBuilder, VectorStore vectorStore) {
        this.chatClientBuilder = chatClientBuilder;
        this.restClientBuilder = restClientBuilder;
        this.vectorStore = vectorStore;
    }

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "Retrieve information by searching the web")
    @Nullable
    String webSearchRetriever(String query) {
        return chatClientBuilder.clone().build().prompt()
                .advisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(SearchEngineDocumentRetriever.builder()
                                .restClientBuilder(restClientBuilder)
                                .maxResults(5)
                                .build())
                        .build())
                .user(query)
                .call()
                .content();
    }

    @Tool(description = "Retrieve information about Sannio Valley Masterclass and Carmine Di Gregorio")
    @Nullable
    String dbRetriever(String query) {
        return chatClientBuilder.clone().build().prompt()
                .advisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(VectorStoreDocumentRetriever.builder()
                                //.filterExpression(new FilterExpressionBuilder().eq("topic", "Sannio Valley").build())
                                .vectorStore(vectorStore)
                                .similarityThreshold(0.5)
                                .topK(3)
                                .build())
                        .build())
                .user(query)
                .call()
                .content();
    }
}
