package org.example.service;

import org.example.model.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for AI-powered note generation and management
 */
@Service
public class NoteGenerationService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AIContentService aiContentService;

    @Autowired
    private ProgressTrackingService progressTrackingService;

    /**
     * Generate comprehensive study notes for a course
     */
    public String generateCourseNotes(String userId, String courseId, String focusArea) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            throw new RuntimeException("Course not found");
        }

        Course course = courseOpt.get();

        // Build context for AI generation
        String courseContext = buildCourseContext(course);
        String userContext = buildUserContext(userId, courseId);

        // Generate AI-powered notes
        String generatedNotes = aiContentService.generateStudyNotes(
                course.getTitle(),
                courseContext + "\n" + userContext,
                focusArea != null ? focusArea : "comprehensive overview");

        // Save the generated notes
        progressTrackingService.addNote(
                userId,
                courseId,
                null,
                "AI Study Notes: " + course.getTitle() +
                        (focusArea != null ? " - " + focusArea : ""),
                generatedNotes,
                Arrays.asList("ai-generated", "course-notes",
                        focusArea != null ? focusArea.toLowerCase().replace(" ", "-") : "overview"));

        return generatedNotes;
    }

    /**
     * Generate topic-specific notes
     */
    public String generateTopicNotes(String userId, String courseId, String topic,
            List<String> keyPoints) {

        // Build topic context
        StringBuilder topicContext = new StringBuilder();
        topicContext.append("Topic: ").append(topic).append("\n");

        if (keyPoints != null && !keyPoints.isEmpty()) {
            topicContext.append("Key Points to Cover:\n");
            for (String point : keyPoints) {
                topicContext.append("- ").append(point).append("\n");
            }
        }

        // Get course context
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        String courseTitle = courseOpt.map(Course::getTitle).orElse("Unknown Course");

        // Generate focused notes
        String generatedNotes = aiContentService.generateStudyNotes(
                courseTitle,
                topicContext.toString(),
                topic);

        // Save the generated notes
        progressTrackingService.addNote(
                userId,
                courseId,
                null,
                "Topic Notes: " + topic,
                generatedNotes,
                Arrays.asList("ai-generated", "topic-notes", topic.toLowerCase().replace(" ", "-")));

        return generatedNotes;
    }

    /**
     * Generate summary notes from user's bookmarks
     */
    public String generateBookmarkSummary(String userId, String courseId) {
        UserProgress progress = progressTrackingService.findOrCreateUserProgress(userId);

        // Get bookmarks for this course
        List<UserProgress.Bookmark> courseBookmarks = progress.getBookmarks().stream()
                .filter(bookmark -> bookmark.getCourseId().equals(courseId))
                .collect(Collectors.toList());

        if (courseBookmarks.isEmpty()) {
            return "No bookmarks found for this course. Start bookmarking important sections to generate summary notes.";
        }

        // Build bookmark context
        StringBuilder bookmarkContext = new StringBuilder();
        bookmarkContext.append("Important sections bookmarked by the user:\n");

        for (UserProgress.Bookmark bookmark : courseBookmarks) {
            bookmarkContext.append("• ").append(bookmark.getTitle());
            if (bookmark.getNotes() != null && !bookmark.getNotes().trim().isEmpty()) {
                bookmarkContext.append(" - ").append(bookmark.getNotes());
            }
            bookmarkContext.append("\n");
        }

        // Get course title
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        String courseTitle = courseOpt.map(Course::getTitle).orElse("Course");

        // Generate summary notes
        String summaryNotes = aiContentService.generateStudyNotes(
                courseTitle,
                bookmarkContext.toString(),
                "bookmark summary");

        // Save the summary
        progressTrackingService.addNote(
                userId,
                courseId,
                null,
                "Bookmark Summary: " + courseTitle,
                summaryNotes,
                Arrays.asList("ai-generated", "bookmark-summary", "summary"));

        return summaryNotes;
    }

    /**
     * Generate review notes before exam/assessment
     */
    public String generateReviewNotes(String userId, String courseId, String examFocus) {
        UserProgress progress = progressTrackingService.findOrCreateUserProgress(userId);

        // Get all user's notes for this course
        List<UserProgress.Note> courseNotes = progress.getNotes().stream()
                .filter(note -> note.getCourseId().equals(courseId))
                .collect(Collectors.toList());

        // Build review context from existing notes
        StringBuilder reviewContext = new StringBuilder();
        reviewContext.append("Previous study notes:\n");

        for (UserProgress.Note note : courseNotes) {
            reviewContext.append("Topic: ").append(note.getTitle()).append("\n");
            reviewContext.append(note.getContent()).append("\n\n");
        }

        // Get course information
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        String courseTitle = courseOpt.map(Course::getTitle).orElse("Course");

        // Generate review notes
        String reviewNotes = aiContentService.generateStudyNotes(
                courseTitle,
                reviewContext.toString(),
                "exam review - " + (examFocus != null ? examFocus : "comprehensive review"));

        // Save review notes
        progressTrackingService.addNote(
                userId,
                courseId,
                null,
                "Review Notes: " + courseTitle +
                        (examFocus != null ? " - " + examFocus : ""),
                reviewNotes,
                Arrays.asList("ai-generated", "review-notes", "exam-prep",
                        examFocus != null ? examFocus.toLowerCase().replace(" ", "-") : "comprehensive"));

        return reviewNotes;
    }

    /**
     * Generate quick reference notes
     */
    public String generateQuickReference(String userId, String courseId, List<String> concepts) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        String courseTitle = courseOpt.map(Course::getTitle).orElse("Course");

        // Build concepts context
        StringBuilder conceptsContext = new StringBuilder();
        conceptsContext.append("Create a quick reference guide for these concepts:\n");

        if (concepts != null && !concepts.isEmpty()) {
            for (String concept : concepts) {
                conceptsContext.append("- ").append(concept).append("\n");
            }
        } else {
            // Get course tags as concepts
            if (courseOpt.isPresent() && courseOpt.get().getTags() != null) {
                for (String tag : courseOpt.get().getTags()) {
                    conceptsContext.append("- ").append(tag).append("\n");
                }
            }
        }

        // Generate quick reference
        String quickRef = aiContentService.generateStudyNotes(
                courseTitle,
                conceptsContext.toString(),
                "quick reference guide");

        // Save quick reference
        progressTrackingService.addNote(
                userId,
                courseId,
                null,
                "Quick Reference: " + courseTitle,
                quickRef,
                Arrays.asList("ai-generated", "quick-reference", "cheat-sheet"));

        return quickRef;
    }

    /**
     * Generate personalized flashcards
     */
    public List<Flashcard> generateFlashcards(String userId, String courseId,
            String topic, int numberOfCards) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        String courseTitle = courseOpt.map(Course::getTitle).orElse("Course");

        // Get user's notes for context
        UserProgress progress = progressTrackingService.findOrCreateUserProgress(userId);
        List<UserProgress.Note> relatedNotes = progress.getNotes().stream()
                .filter(note -> note.getCourseId().equals(courseId))
                .filter(note -> topic == null ||
                        note.getTitle().toLowerCase().contains(topic.toLowerCase()) ||
                        note.getContent().toLowerCase().contains(topic.toLowerCase()))
                .collect(Collectors.toList());

        // Build context for flashcard generation
        StringBuilder context = new StringBuilder();
        context.append("Course: ").append(courseTitle).append("\n");
        if (topic != null) {
            context.append("Topic: ").append(topic).append("\n");
        }

        context.append("Relevant study material:\n");
        for (UserProgress.Note note : relatedNotes) {
            context.append(note.getContent()).append("\n");
        }

        // Generate flashcards using AI
        String flashcardContent = aiContentService.generateStudyNotes(
                courseTitle,
                context.toString(),
                "flashcards for " + (topic != null ? topic : "course content"));

        // Parse the generated content into flashcards
        List<Flashcard> flashcards = parseFlashcardsFromContent(
                flashcardContent, courseId, topic, numberOfCards);

        // Save flashcards as notes
        for (int i = 0; i < flashcards.size(); i++) {
            Flashcard card = flashcards.get(i);
            progressTrackingService.addNote(
                    userId,
                    courseId,
                    null,
                    "Flashcard " + (i + 1) + ": " +
                            (topic != null ? topic : courseTitle),
                    "Q: " + card.getQuestion() + "\nA: " + card.getAnswer(),
                    Arrays.asList("ai-generated", "flashcard",
                            topic != null ? topic.toLowerCase().replace(" ", "-") : "general"));
        }

        return flashcards;
    }

    /**
     * Get user's note statistics
     */
    public NoteStatistics getNoteStatistics(String userId, String courseId) {
        UserProgress progress = progressTrackingService.findOrCreateUserProgress(userId);

        List<UserProgress.Note> courseNotes;
        if (courseId != null) {
            courseNotes = progress.getNotes().stream()
                    .filter(note -> note.getCourseId().equals(courseId))
                    .collect(Collectors.toList());
        } else {
            courseNotes = progress.getNotes();
        }

        NoteStatistics stats = new NoteStatistics();
        stats.setTotalNotes(courseNotes.size());

        // Count AI-generated vs manual notes
        long aiGeneratedCount = courseNotes.stream()
                .filter(note -> note.getTags() != null &&
                        note.getTags().contains("ai-generated"))
                .count();
        stats.setAiGeneratedNotes((int) aiGeneratedCount);
        stats.setManualNotes((int) (courseNotes.size() - aiGeneratedCount));

        // Get note creation trend (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentNotes = courseNotes.stream()
                .filter(note -> note.getCreatedAt().isAfter(weekAgo))
                .count();
        stats.setNotesThisWeek((int) recentNotes);

        // Most used tags
        Map<String, Integer> tagFrequency = new HashMap<>();
        for (UserProgress.Note note : courseNotes) {
            if (note.getTags() != null) {
                for (String tag : note.getTags()) {
                    tagFrequency.merge(tag, 1, Integer::sum);
                }
            }
        }

        List<String> popularTags = tagFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        stats.setPopularTags(popularTags);

        return stats;
    }

    // Private helper methods
    private String buildCourseContext(Course course) {
        StringBuilder context = new StringBuilder();
        context.append("Course: ").append(course.getTitle()).append("\n");

        if (course.getDescription() != null) {
            context.append("Description: ").append(course.getDescription()).append("\n");
        }

        if (course.getTags() != null && !course.getTags().isEmpty()) {
            context.append("Topics: ").append(String.join(", ", course.getTags())).append("\n");
        }

        if (course.getDifficulty() != null) {
            context.append("Level: ").append(course.getDifficulty().toString()).append("\n");
        }

        return context.toString();
    }

    private String buildUserContext(String userId, String courseId) {
        try {
            UserProgress progress = progressTrackingService.findOrCreateUserProgress(userId);

            // Get user's course progress
            Optional<UserProgress.CourseProgress> courseProgressOpt = progress.getCourses().stream()
                    .filter(cp -> cp.getCourseId().equals(courseId))
                    .findFirst();

            StringBuilder context = new StringBuilder();
            if (courseProgressOpt.isPresent()) {
                UserProgress.CourseProgress courseProgress = courseProgressOpt.get();
                context.append("User Progress: ")
                        .append(courseProgress.getCompletionPercentage())
                        .append("% complete\n");

                context.append("Time Spent: ")
                        .append(courseProgress.getTimeSpent())
                        .append(" minutes\n");
            }

            // Get existing bookmarks for context
            List<UserProgress.Bookmark> bookmarks = progress.getBookmarks().stream()
                    .filter(b -> b.getCourseId().equals(courseId))
                    .collect(Collectors.toList());

            if (!bookmarks.isEmpty()) {
                context.append("Bookmarked sections: ");
                context.append(bookmarks.stream()
                        .map(UserProgress.Bookmark::getTitle)
                        .collect(Collectors.joining(", ")));
                context.append("\n");
            }

            return context.toString();
        } catch (Exception e) {
            return ""; // Return empty context if user progress not available
        }
    }

    private List<Flashcard> parseFlashcardsFromContent(String content, String courseId,
            String topic, int maxCards) {
        List<Flashcard> flashcards = new ArrayList<>();

        // Simple parsing logic - can be enhanced
        String[] lines = content.split("\n");
        String currentQuestion = null;
        String currentAnswer = null;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Q:") || line.startsWith("Question:")) {
                if (currentQuestion != null && currentAnswer != null) {
                    // Save previous flashcard
                    flashcards.add(new Flashcard(currentQuestion, currentAnswer, courseId, topic));
                    if (flashcards.size() >= maxCards)
                        break;
                }
                currentQuestion = line.substring(line.indexOf(":") + 1).trim();
                currentAnswer = null;
            } else if (line.startsWith("A:") || line.startsWith("Answer:")) {
                currentAnswer = line.substring(line.indexOf(":") + 1).trim();
            }
        }

        // Add the last flashcard
        if (currentQuestion != null && currentAnswer != null && flashcards.size() < maxCards) {
            flashcards.add(new Flashcard(currentQuestion, currentAnswer, courseId, topic));
        }

        // If parsing failed, create some default flashcards
        if (flashcards.isEmpty()) {
            flashcards.add(new Flashcard(
                    "What is the main topic of this course section?",
                    topic != null ? topic : "Review the course material",
                    courseId,
                    topic));
        }

        return flashcards;
    }

    // Helper classes
    public static class Flashcard {
        private String question;
        private String answer;
        private String courseId;
        private String topic;
        private LocalDateTime createdAt;

        public Flashcard(String question, String answer, String courseId, String topic) {
            this.question = question;
            this.answer = answer;
            this.courseId = courseId;
            this.topic = topic;
            this.createdAt = LocalDateTime.now();
        }

        // Getters and setters
        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class NoteStatistics {
        private int totalNotes;
        private int aiGeneratedNotes;
        private int manualNotes;
        private int notesThisWeek;
        private List<String> popularTags;

        public NoteStatistics() {
        }

        public int getTotalNotes() {
            return totalNotes;
        }

        public void setTotalNotes(int totalNotes) {
            this.totalNotes = totalNotes;
        }

        public int getAiGeneratedNotes() {
            return aiGeneratedNotes;
        }

        public void setAiGeneratedNotes(int aiGeneratedNotes) {
            this.aiGeneratedNotes = aiGeneratedNotes;
        }

        public int getManualNotes() {
            return manualNotes;
        }

        public void setManualNotes(int manualNotes) {
            this.manualNotes = manualNotes;
        }

        public int getNotesThisWeek() {
            return notesThisWeek;
        }

        public void setNotesThisWeek(int notesThisWeek) {
            this.notesThisWeek = notesThisWeek;
        }

        public List<String> getPopularTags() {
            return popularTags;
        }

        public void setPopularTags(List<String> popularTags) {
            this.popularTags = popularTags;
        }
    }
}
