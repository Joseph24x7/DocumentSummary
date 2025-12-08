package com.docqa.service;

import com.docqa.model.DocumentEntity;
import com.docqa.repository.DocumentRepository;
import com.docqa.util.PDFExtractor;
import com.docqa.util.PromptTemplates;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OllamaService ollamaService;


    public DocumentService(DocumentRepository documentRepository, OllamaService ollamaService) {
        this.documentRepository = documentRepository;
        this.ollamaService = ollamaService;
    }

    public DocumentEntity uploadAndProcessDocument(MultipartFile file, String query) {

        log.info("Starting document upload and processing for file: {}, file size: {}", file.getOriginalFilename(), file.getSize());

        // Extract text from PDF
        String extractedText = PDFExtractor.extractTextFromPDF(file);
        log.info("Successfully extracted text from PDF, length: {} characters", extractedText.length());

        // Generate summary using Ollama
        String summaryPrompt = StringUtils.isNotBlank(query) ?
                PromptTemplates.getQuestionAnsweringPrompt(extractedText, query) :
                PromptTemplates.getSummarizationPrompt(extractedText);

        String summary = ollamaService.generateText(summaryPrompt);
        log.info("Generated summary/answer using Ollama");

        // Create and save document entity
        DocumentEntity document = DocumentEntity.builder()
                .fileName(file.getOriginalFilename()).mimeType(file.getContentType())
                .fileSize(file.getSize()).extractedText(extractedText).summary(summary)
                .uploadedAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        DocumentEntity savedDocument = documentRepository.save(document);
        log.info("Document saved with ID: {}", savedDocument.getId());

        return savedDocument;
    }

}

