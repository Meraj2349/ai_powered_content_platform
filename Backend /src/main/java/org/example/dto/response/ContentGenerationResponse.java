package org.example.dto.response;

import org.example.model.ContentType;

/**
 * Content generation response DTO
 */
public class ContentGenerationResponse {
    private String contentId;
    private String generatedContent;
    private ContentType contentType;
    private String prompt;

    // Constructors
    public ContentGenerationResponse() {
    }

    public ContentGenerationResponse(String contentId, String generatedContent, ContentType contentType,
            String prompt) {
        this.contentId = contentId;
        this.generatedContent = generatedContent;
        this.contentType = contentType;
        this.prompt = prompt;
    }

    // Getters and Setters
    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getGeneratedContent() {
        return generatedContent;
    }

    public void setGeneratedContent(String generatedContent) {
        this.generatedContent = generatedContent;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
