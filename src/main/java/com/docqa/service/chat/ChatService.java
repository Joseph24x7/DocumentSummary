package com.docqa.service.chat;

import com.docqa.exception.ResourceNotFoundException;
import com.docqa.model.ChatMessage;
import com.docqa.model.ChatSession;
import com.docqa.model.DocumentEntity;
import com.docqa.repository.ChatSessionRepository;
import com.docqa.repository.DocumentRepository;
import com.docqa.service.elasticsearch.ElasticsearchSyncService;
import com.docqa.service.llm.OllamaService;
import com.docqa.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final DocumentRepository documentRepository;
    private final OllamaService ollamaService;
    private final ElasticsearchSyncService elasticsearchSyncService;

    public ChatService(ChatSessionRepository chatSessionRepository,
                      DocumentRepository documentRepository,
                      OllamaService ollamaService,
                      ElasticsearchSyncService elasticsearchSyncService) {
        this.chatSessionRepository = chatSessionRepository;
        this.documentRepository = documentRepository;
        this.ollamaService = ollamaService;
        this.elasticsearchSyncService = elasticsearchSyncService;
    }

    public ChatSession startChatSession(String documentId) {
        log.info("Starting new chat session for document: {}", documentId);

        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        ChatSession session = new ChatSession(
                documentId,
                document.getFileName(),
                document.getExtractedText()
        );

        ChatSession savedSession = chatSessionRepository.save(session);

        // Sync to Elasticsearch for fast search
        elasticsearchSyncService.syncSession(savedSession);

        return savedSession;
    }

    public ChatSession getChatSession(String sessionId) {
        log.info("Retrieving chat session: {}", sessionId);
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
    }

    public String chat(String sessionId, String userMessage) {
        log.info("Processing chat message for session: {}", sessionId);

        ChatSession session = getChatSession(sessionId);

        ChatMessage userMsg = new ChatMessage("user", userMessage);
        session.addMessage(userMsg);

        String prompt = PromptBuilder.buildContextualPrompt(session, userMessage);

        String assistantResponse = ollamaService.generateText(prompt);
        log.info("Generated response from Ollama, length: {} characters", assistantResponse.length());

        ChatMessage assistantMsg = new ChatMessage("assistant", assistantResponse);
        session.addMessage(assistantMsg);

        chatSessionRepository.save(session);

        // Sync to Elasticsearch for fast search (if available)
        if (elasticsearchSyncService != null) {
            elasticsearchSyncService.syncSession(session);
        }

        return assistantResponse;
    }

    public List<ChatSession> getAllSessions() {
        log.info("Retrieving all chat sessions");
        return chatSessionRepository.findAll();
    }
}
