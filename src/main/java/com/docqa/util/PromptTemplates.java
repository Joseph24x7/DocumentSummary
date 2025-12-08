package com.docqa.util;

public class PromptTemplates {

    public static String getSummarizationPrompt(String documentText) {
        return """
                Analyze the following document and provide a comprehensive summary in 3-4 sentences.
                Focus on the main topics, key points, and important information.
                
                Document:
                %s
                
                Summary:
                """.formatted(documentText);
    }

    public static String getQuestionAnsweringPrompt(String documentText, String question) {
        return """
                Use ONLY the document text below to answer the question.
                Do not use any external knowledge.
                If the answer cannot be found in the document, say "I cannot find the answer in the provided document."
                
                Document:
                %s
                
                Question:
                %s
                
                Answer:
                """.formatted(documentText, question);
    }

}

