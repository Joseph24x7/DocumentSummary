package com.docqa.service;

import com.docqa.model.DocumentEntity;
import com.docqa.repository.DocumentRepository;
import com.docqa.util.FileHashUtil;
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

    public String uploadAndProcessDocument(MultipartFile file, String query) {
        log.info("Starting document upload and processing for file: {}, file size: {}",
                file.getOriginalFilename(), file.getSize());

        String fileHash = FileHashUtil.calculateFileHash(file);
        log.info("Calculated file hash: {}", fileHash);

        DocumentEntity document = documentRepository.findByFileHash(fileHash)
                .orElseGet(() -> createNewDocument(file, fileHash));

        String extractedText = document.getExtractedText();
        String prompt = buildPrompt(extractedText, query);
        String response = ollamaService.generateText(prompt);

        log.info("Generated summary/answer using Ollama");

        document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(document);

        return response;
    }

    /**
     * Upload a document and return its ID (for chatbot integration)
     */
    public String uploadDocument(MultipartFile file) {
        log.info("Uploading document: {}, file size: {}", file.getOriginalFilename(), file.getSize());

        String fileHash = FileHashUtil.calculateFileHash(file);
        log.info("Calculated file hash: {}", fileHash);

        DocumentEntity document = documentRepository.findByFileHash(fileHash)
                .orElseGet(() -> createNewDocument(file, fileHash));

        document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(document);

        log.info("Document saved with ID: {}", document.getId());
        return document.getId();
    }

    private DocumentEntity createNewDocument(MultipartFile file, String fileHash) {
        log.info("Duplicate PDF detected! File hash {} already exists. Reusing cached content from file: {}",
                fileHash, file.getOriginalFilename());

        String extractedText = PDFExtractor.extractTextFromPDF(file);
        log.info("Successfully extracted text from PDF, length: {} characters", extractedText.length());

        LocalDateTime now = LocalDateTime.now();

        return DocumentEntity.builder()
                .fileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .fileHash(fileHash)
                .extractedText(extractedText)
                .uploadedAt(now)
                .updatedAt(now)
                .build();
    }

    private String buildPrompt(String extractedText, String query) {
        return StringUtils.isNotBlank(query) ?
                PromptTemplates.getQuestionAnsweringPrompt(extractedText, query) :
                PromptTemplates.getSummarizationPrompt(extractedText);
    }

}

