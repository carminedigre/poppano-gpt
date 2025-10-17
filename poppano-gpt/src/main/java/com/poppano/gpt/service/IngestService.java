package com.poppano.gpt.service;

import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IngestService {

	private final VectorStore vectorStore;

	public int ingestPdf(Path path, String sourceTag, String originalFileName) throws Exception {
		var res = new FileSystemResource(path);

		var chunks = readAndChunkPdf(res);
		if (chunks == null || chunks.isEmpty()) {
			return 0;
		}
		for (Document d : chunks) {
			d.getMetadata().put("source", sourceTag);
			d.getMetadata().put("filename", originalFileName);
		}
		if (vectorStore != null) {
			vectorStore.add(chunks); // embeds with Ollama, stores in PGVector
		}
		
		return chunks.size();
	}
	
	/** Read a PDF and chunk it for embeddings */
	private List<Document> readAndChunkPdf(Resource pdfPath) {
		var reader = new PagePdfDocumentReader(pdfPath);
		var docs = reader.get(); // one Document per page (includes page metadata)
		var splitter = TokenTextSplitter.builder().withMaxNumChunks(400).build();
		return splitter.split(docs);
	}
}