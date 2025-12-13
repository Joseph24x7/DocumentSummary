package com.docqa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    private String id;

    private String documentId;           // Reference to the uploaded document
    private String documentName;         // Document file name for display
    private String extractedText;        // Full extracted text from PDF (cached)

    private List<ChatMessage> messages;  // Conversation history

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChatSession(String documentId, String documentName, String extractedText) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.extractedText = extractedText;
        this.messages = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addMessage(ChatMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        this.updatedAt = LocalDateTime.now();
    }
}

