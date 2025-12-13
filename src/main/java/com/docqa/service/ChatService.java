package com.docqa.service;

import com.docqa.model.ChatMessage;
import com.docqa.model.ChatSession;
import com.docqa.model.DocumentEntity;
import com.docqa.repository.ChatSessionRepository;
import com.docqa.repository.DocumentRepository;
import com.docqa.util.PromptTemplates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final DocumentRepository documentRepository;
    private final OllamaService ollamaService;

    public ChatService(ChatSessionRepository chatSessionRepository,
                      DocumentRepository documentRepository,
                      OllamaService ollamaService) {
        this.chatSessionRepository = chatSessionRepository;
        this.documentRepository = documentRepository;
        this.ollamaService = ollamaService;
    }

    /**
     * Start a new chat session for a document
     */
    public ChatSession startChatSession(String documentId) {
        log.info("Starting new chat session for document: {}", documentId);

        // Get the document
        DocumentEntity document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        // Create new chat session
        ChatSession session = new ChatSession(
                documentId,
                document.getFileName(),
                document.getExtractedText()
        );

        return chatSessionRepository.save(session);
    }

    /**
     * Get existing chat session
     */
    public ChatSession getChatSession(String sessionId) {
        log.info("Retrieving chat session: {}", sessionId);
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Chat session not found: " + sessionId));
    }

    /**
     * Send a message to the chatbot and get a response
     */
    public String chat(String sessionId, String userMessage) {
        log.info("Processing chat message for session: {}", sessionId);

        ChatSession session = getChatSession(sessionId);

        // Add user message to history
        ChatMessage userMsg = new ChatMessage("user", userMessage);
        session.addMessage(userMsg);

        // Build prompt with conversation context
        String prompt = buildContextualPrompt(session, userMessage);

        // Get response from Ollama
        String assistantResponse = ollamaService.generateText(prompt);
        log.info("Generated response from Ollama, length: {} characters", assistantResponse.length());

        // Add assistant response to history
        ChatMessage assistantMsg = new ChatMessage("assistant", assistantResponse);
        session.addMessage(assistantMsg);

        // Save updated session
        chatSessionRepository.save(session);

        return assistantResponse;
    }

    /**
     * Build a prompt that includes conversation history and document context
     */
    private String buildContextualPrompt(ChatSession session, String currentQuestion) {
        StringBuilder promptBuilder = new StringBuilder();

        // Document context
        promptBuilder.append("You are a helpful assistant analyzing the following document:\n\n");
        promptBuilder.append("---DOCUMENT START---\n");
        promptBuilder.append(session.getExtractedText());
        promptBuilder.append("\n---DOCUMENT END---\n\n");

        // Conversation history
        if (!session.getMessages().isEmpty() && session.getMessages().size() > 1) {
            promptBuilder.append("Previous conversation:\n");
            // Include last 5 exchanges to keep context window manageable
            int startIndex = Math.max(0, session.getMessages().size() - 10);
            for (int i = startIndex; i < session.getMessages().size() - 1; i++) {
                ChatMessage msg = session.getMessages().get(i);
                promptBuilder.append(msg.getRole().equals("user") ? "User: " : "Assistant: ");
                promptBuilder.append(msg.getContent()).append("\n");
            }
            promptBuilder.append("\n");
        }

        // Current question
        promptBuilder.append("Current question:\n");
        promptBuilder.append(currentQuestion).append("\n\n");
        promptBuilder.append("Please provide a detailed answer based on the document and conversation history.");

        return promptBuilder.toString();
    }

    /**
     * Get all messages for a session
     */
    public ChatSession getSessionWithMessages(String sessionId) {
        return getChatSession(sessionId);
    }
}

