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
				Sei un assistente disponibile. Usa SOLO il contesto fornito per rispondere e sempre la lingua italiana.
				Se la risposta non è nel contesto, rispondi che non lo sai.
				Quando possibile, cita il nome del file di origine e la pagina, se disponibili.
				""";

		var prompt = new Prompt(new UserMessage(system + "\n\nCONTEXT:\n" + context + "\n\nQUESTION: " + question));
		
	    return chatModel
	            .stream(prompt)                       
	            .map(r -> r.getResult().getOutput().getText())
	            .onErrorResume(ex -> Flux.just("\n[error] " + ex.getMessage() + "\n"));
	}
}
