package com.poppano.gpt.service;

import com.poppano.gpt.tool.Tools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;

@Service
public class ChatRoutingService {

    private final ChatClient chatClient;

    public ChatRoutingService(ChatClient.Builder chatClientBuilder, RestClient.Builder restClientBuilder, VectorStore vectorStore, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultTools(new Tools(chatClientBuilder.clone(), restClientBuilder, vectorStore))
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public Flux<String> ask(String question, int k, String conversationId) {
        String resolvedConversationId = (conversationId == null || conversationId.isBlank())
                ? ChatMemory.DEFAULT_CONVERSATION_ID
                : conversationId;
        return chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, resolvedConversationId))
                .user(question)
                .stream()
                .content();
    }
}
