package org.example.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.dto.request.ContentGenerationRequest;
import org.example.dto.response.ContentGenerationResponse;
import org.example.model.Content;
import org.example.model.User;
import org.example.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * AI Content Generation Service
 * Handles AI-powered content creation using OpenAI API
 */
@Service
public class AIContentService {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Autowired
    private ContentRepository contentRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public ContentGenerationResponse generateContent(ContentGenerationRequest request, User author) {
        try {
            // Build the prompt based on content type and requirements
            String enhancedPrompt = buildPrompt(request);

            // Call OpenAI API
            String generatedContent = callOpenAI(enhancedPrompt, 0.7, 1500);

            // Create and save content entity
            Content content = new Content();
            content.setTitle(request.getTitle());
            content.setBody(generatedContent);
            content.setContentType(request.getContentType());
            content.setAuthor(author);
            content.setPrompt(enhancedPrompt);
            content.setAiModel("gpt-3.5-turbo");
            content.setTags(request.getTags());

            // Store AI parameters
            Map<String, Object> aiParameters = new HashMap<>();
            aiParameters.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);
            aiParameters.put("maxTokens", request.getMaxTokens() != null ? request.getMaxTokens() : 1000);
            aiParameters.put("model", "gpt-3.5-turbo");
            content.setAiParameters(aiParameters);

            Content savedContent = contentRepository.save(content);

            return new ContentGenerationResponse(
                    savedContent.getId(),
                    generatedContent,
                    request.getContentType(),
                    enhancedPrompt);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate content: " + e.getMessage(), e);
        }
    }

    /**
     * Generate skill-related keywords for course search
     */
    public List<String> generateSkillKeywords(String skill) {
        try {
            String prompt = "Generate a list of 10-15 relevant keywords and topics for learning: " + skill +
                    ". Include related technologies, concepts, and subtopics. Return as comma-separated values.";

            String response = callOpenAI(prompt, 0.3, 200);

            return Arrays.asList(response.split(","))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback to basic keywords
            return Arrays.asList(skill, skill + " basics", skill + " tutorial",
                    skill + " course", skill + " training");
        }
    }

    /**
     * Generate learning path suggestions
     */
    public String generateLearningPathSuggestion(String skill, String currentLevel, String goals) {
        String prompt = String.format(
                "Create a comprehensive learning path for someone who wants to learn %s. " +
                        "Current level: %s. Goals: %s. " +
                        "Provide a structured path with steps, estimated timeframes, and key topics to cover.",
                skill, currentLevel, goals);

        return callOpenAI(prompt, 0.7, 1500);
    }

    /**
     * Generate study notes from course content
     */
    public String generateStudyNotes(String courseTitle, String lessonContent, String keyTopics) {
        String prompt = String.format(
                "Create comprehensive study notes for the following lesson from course '%s':\n\n" +
                        "Lesson Content: %s\n\n" +
                        "Key Topics: %s\n\n" +
                        "Format the notes with clear headings, bullet points, and key takeaways.",
                courseTitle, lessonContent, keyTopics);

        return callOpenAI(prompt, 0.5, 1000);
    }

    /**
     * Analyze course combination effectiveness
     */
    public String analyzeCourseCombo(List<String> courseTitles, String learningGoal) {
        String coursesStr = String.join(", ", courseTitles);
        String prompt = String.format(
                "Analyze the effectiveness of this course combination for achieving the goal '%s':\n\n" +
                        "Courses: %s\n\n" +
                        "Provide insights on: 1) Course synergy 2) Potential gaps 3) Learning progression 4) Recommendations",
                learningGoal, coursesStr);

        return callOpenAI(prompt, 0.6, 800);
    }

    private String callOpenAI(String prompt, double temperature, int maxTokens) {
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + openAiApiKey);
            headers.set("Content-Type", "application/json");

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "user", "content", prompt)));
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make API call
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class);

            // Extract generated content
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            throw new RuntimeException("Invalid response from OpenAI API");

        } catch (Exception e) {
            // Fallback to mock content for testing
            return "Error generating content: " + e.getMessage();
        }
    }

    private String generateMockContent(ContentGenerationRequest request) {
        String mockPrefix = "[AI Generated Content] ";
        String contentTypeText = request.getContentType().name().toLowerCase().replace("_", " ");

        return mockPrefix + "This is a sample " + contentTypeText + " about " + request.getTopic() +
                ". This content would normally be generated by OpenAI, but this is a fallback response " +
                "for development purposes. Please configure your OpenAI API key to enable actual AI generation.";
    }

    private String buildPrompt(ContentGenerationRequest request) {
        StringBuilder promptBuilder = new StringBuilder();

        // Add content type specific instructions
        switch (request.getContentType()) {
            case BLOG_POST:
                promptBuilder.append("Write a comprehensive blog post about: ");
                break;
            case ARTICLE:
                promptBuilder.append("Write an informative article about: ");
                break;
            case SOCIAL_MEDIA_POST:
                promptBuilder.append("Create an engaging social media post about: ");
                break;
            case EMAIL_TEMPLATE:
                promptBuilder.append("Create a professional email template for: ");
                break;
            case PRODUCT_DESCRIPTION:
                promptBuilder.append("Write a compelling product description for: ");
                break;
            case MARKETING_COPY:
                promptBuilder.append("Create persuasive marketing copy for: ");
                break;
            default:
                promptBuilder.append("Write content about: ");
        }

        promptBuilder.append(request.getTopic());

        // Add additional requirements
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            promptBuilder.append("\n\nAdditional requirements: ").append(request.getDescription());
        }

        // Add target audience
        if (request.getTargetAudience() != null && !request.getTargetAudience().isEmpty()) {
            promptBuilder.append("\n\nTarget audience: ").append(request.getTargetAudience());
        }

        // Add tone specification
        if (request.getTone() != null && !request.getTone().isEmpty()) {
            promptBuilder.append("\n\nTone: ").append(request.getTone());
        }

        // Add word count requirement
        if (request.getWordCount() != null) {
            promptBuilder.append("\n\nTarget word count: approximately ").append(request.getWordCount())
                    .append(" words");
        }

        return promptBuilder.toString();
    }
}
