package com.poppano.gpt.controller;

import java.nio.file.Files;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.poppano.gpt.service.AskService;
import com.poppano.gpt.service.IngestService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PoppanoController {

	private final IngestService ingestionService;
	private final AskService askService;

	@PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String ingest(@RequestPart("file") MultipartFile file, @RequestParam(defaultValue = "upload") String source) throws Exception {
		var tmp = Files.createTempFile("pdf-", ".pdf");
		var fileName = file.getOriginalFilename();
		file.transferTo(tmp);
		var chunks = ingestionService.ingestPdf(tmp, source,fileName);
		Files.deleteIfExists(tmp);
		return "Ingested " + chunks + " chunks from " + fileName;
	}

	 @GetMapping("/ask")
	 public Flux<String> ask(@RequestParam String q, @RequestParam(defaultValue = "5") int k) {
		return askService.ask(q, k);
	}
}
