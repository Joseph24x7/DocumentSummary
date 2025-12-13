package com.docqa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {
    private String query;
    private String response;
    private String sessionId;      // Chat session ID for continuing conversation
    private String documentId;     // Document ID for reference
}



