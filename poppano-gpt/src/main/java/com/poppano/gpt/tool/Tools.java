package com.poppano.gpt.tool;

import com.poppano.gpt.component.SearchEngineDocumentRetriever;
import com.poppano.gpt.component.WeatherEngine;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    String getWeather(double latitude, double longitude) {
        return WeatherEngine.builder().restClientBuilder(restClientBuilder).build().retrieve(latitude,longitude);
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

    @Tool(description = "Update poppanoGPT template based on a change request and return the updated HTML")
    String updateChatTemplate(String query) {
        Path templatePath = Path.of("src/main/resources/templates/chat.html");
        String template = readTemplate(templatePath);
        String updatedTemplate = chatClientBuilder.clone().build().prompt()
                //.system("You are a precise HTML editor. Apply only the change request to the template, no other modifications. Return the full updated HTML only, with no explanations or optimizations.")
                .system("You are a precise HTML editor.Applay the change request to the template,Return the full updated HTML only, with no explanations")
                .user("Change request: " + query + "\n\nCurrent template:\n" + template)
                .call()
                .content();
        IO.println(updatedTemplate);
        writeTemplate(templatePath, updatedTemplate);
        return "DONE";
    }

    private String readTemplate(Path templatePath) {
        try {
            return Files.readString(templatePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read template: " + templatePath, e);
        }
    }

    private void writeTemplate(Path templatePath, String updatedTemplate) {
        try {
            Files.writeString(templatePath, updatedTemplate, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write template: " + templatePath, e);
        }
    }
}
