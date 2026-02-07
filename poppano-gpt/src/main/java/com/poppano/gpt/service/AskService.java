package com.poppano.gpt.service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AskService {

	private final VectorStore vectorStore;
	private final OllamaChatModel chatModel;

	public Flux<String> ask(String question, int k) {
		var hits = vectorStore.similaritySearch(question);

		var context = hits.stream().map(d -> d.getFormattedContent()).collect(Collectors.joining("\n---\n"));

		var system = """
				You are a helpful assistant. Use ONLY the provided context to answer and always use the English language.
				If the answer is not in the context, reply that you do not know.
				When possible, cite the source file name and the page, if available.
				""";

		var prompt = new Prompt(new UserMessage(system + "\n\nCONTEXT:\n" + context + "\n\nQUESTION: " + question));
		
	    return chatModel
	            .stream(prompt)                       
	            .map(r -> r.getResult().getOutput().getText())
	            .onErrorResume(ex -> Flux.just("\n[error] " + ex.getMessage() + "\n"));
	}
}
