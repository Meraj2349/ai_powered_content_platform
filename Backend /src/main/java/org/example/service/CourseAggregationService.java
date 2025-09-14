package org.example.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.model.Course;
import org.example.model.CourseStatus;
import org.example.model.DifficultyLevel;
import org.example.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for aggregating courses from multiple platforms
 */
@Service
public class CourseAggregationService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ReviewAnalysisService reviewAnalysisService;

    @Autowired
    private AIContentService aiContentService;

    /**
     * Search courses across all platforms with intelligent filtering
     */
    public List<Course> searchCourses(String searchTerm, String category,
            DifficultyLevel difficulty, String language,
            boolean includeFreeCourses, double maxPrice) {

        List<Course> courses = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            courses = courseRepository.searchCourses(searchTerm);
        } else {
            courses = courseRepository.findAll();
        }

        // Apply filters
        return courses.stream()
                .filter(course -> category == null || course.getCategory().equals(category))
                .filter(course -> difficulty == null || course.getDifficulty().equals(difficulty))
                .filter(course -> language == null || course.getLanguage().equals(language))
                .filter(course -> course.getStatus() == CourseStatus.ACTIVE)
                .filter(course -> filterByPrice(course, includeFreeCourses, maxPrice))
                .sorted(this::compareCoursesByQuality)
                .collect(Collectors.toList());
    }

    /**
     * Find best courses for a specific skill with AI-powered recommendations
     */
    public List<Course> findBestCoursesForSkill(String skill, DifficultyLevel level,
            String language, int maxResults) {

        // Generate skill-related keywords using AI
        List<String> skillKeywords = aiContentService.generateSkillKeywords(skill);

        List<Course> candidateCourses = courseRepository.findCoursesForLearningPath(
                skillKeywords, level, language);

        return candidateCourses.stream()
                .filter(course -> course.getStatus() == CourseStatus.ACTIVE)
                .sorted(this::compareCoursesByQuality)
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Get course recommendations based on user preferences and learning history
     */
    public List<Course> getPersonalizedRecommendations(String userId,
            List<String> completedCourseIds,
            List<String> interests,
            DifficultyLevel preferredLevel,
            String preferredLanguage) {

        // Analyze user's learning patterns
        Map<String, Double> topicAffinities = analyzeUserTopicAffinities(completedCourseIds);

        // Find courses similar to user's interests
        List<Course> recommendations = new ArrayList<>();

        for (String interest : interests) {
            List<Course> interestCourses = courseRepository.findCoursesForLearningPath(
                    Arrays.asList(interest), preferredLevel, preferredLanguage);
            recommendations.addAll(interestCourses);
        }

        // Remove already completed courses
        recommendations = recommendations.stream()
                .filter(course -> !completedCourseIds.contains(course.getId()))
                .distinct()
                .sorted((c1, c2) -> Double.compare(
                        calculateRecommendationScore(c2, topicAffinities),
                        calculateRecommendationScore(c1, topicAffinities)))
                .limit(20)
                .collect(Collectors.toList());

        return recommendations;
    }

    /**
     * Compare platforms for a specific course topic
     */
    public Map<String, Object> comparePlatformsForTopic(String topic, DifficultyLevel level) {
        List<Course> courses = courseRepository.findCoursesForLearningPath(
                Arrays.asList(topic), level, "English");

        Map<String, List<Course>> platformCourses = courses.stream()
                .collect(Collectors.groupingBy(Course::getPlatform));

        Map<String, Object> comparison = new HashMap<>();

        for (Map.Entry<String, List<Course>> entry : platformCourses.entrySet()) {
            String platform = entry.getKey();
            List<Course> platformCourseList = entry.getValue();

            Map<String, Object> platformStats = new HashMap<>();
            platformStats.put("courseCount", platformCourseList.size());
            platformStats.put("averageRating", calculateAverageRating(platformCourseList));
            platformStats.put("averagePrice", calculateAveragePrice(platformCourseList));
            platformStats.put("freeCourseCount", countFreeCourses(platformCourseList));
            platformStats.put("totalDuration", calculateTotalDuration(platformCourseList));

            comparison.put(platform, platformStats);
        }

        return comparison;
    }

    /**
     * Get quality score for a course based on multiple factors
     */
    public double calculateCourseQualityScore(Course course) {
        double score = 0.0;

        // Rating score (40% weight)
        if (course.getTotalReviews() > 0) {
            score += (course.getAverageRating() / 5.0) * 0.4;
        }

        // Review analysis score (30% weight)
        if (course.getReviewAnalysis() != null) {
            score += Math.max(0, (course.getReviewAnalysis().getSentimentScore() + 1) / 2) * 0.3;
        }

        // Content quality score (20% weight)
        if (course.getModules() != null && !course.getModules().isEmpty()) {
            double avgLessonQuality = course.getModules().stream()
                    .flatMap(module -> module.getLessons().stream())
                    .mapToDouble(Course.Lesson::getQualityScore)
                    .average()
                    .orElse(0.0);
            score += avgLessonQuality * 0.2;
        }

        // Instructor credibility (10% weight)
        score += 0.1; // Placeholder - would be based on instructor analysis

        return score;
    }

    // Private helper methods
    private boolean filterByPrice(Course course, boolean includeFreeCourses, double maxPrice) {
        if (course.getPricing() == null)
            return true;

        if (course.getPricing().isFree()) {
            return includeFreeCourses;
        }

        return course.getPricing().getPrice() <= maxPrice;
    }

    private int compareCoursesByQuality(Course c1, Course c2) {
        double score1 = calculateCourseQualityScore(c1);
        double score2 = calculateCourseQualityScore(c2);
        return Double.compare(score2, score1);
    }

    private Map<String, Double> analyzeUserTopicAffinities(List<String> completedCourseIds) {
        Map<String, Double> affinities = new HashMap<>();

        for (String courseId : completedCourseIds) {
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                if (course.getTags() != null) {
                    for (String tag : course.getTags()) {
                        affinities.merge(tag, 1.0, Double::sum);
                    }
                }
            }
        }

        return affinities;
    }

    private double calculateRecommendationScore(Course course, Map<String, Double> topicAffinities) {
        double score = calculateCourseQualityScore(course);

        // Boost score based on topic affinity
        if (course.getTags() != null) {
            double affinityBoost = course.getTags().stream()
                    .mapToDouble(tag -> topicAffinities.getOrDefault(tag, 0.0))
                    .sum() / course.getTags().size();
            score += affinityBoost * 0.2;
        }

        return score;
    }

    private double calculateAverageRating(List<Course> courses) {
        return courses.stream()
                .filter(c -> c.getTotalReviews() > 0)
                .mapToDouble(Course::getAverageRating)
                .average()
                .orElse(0.0);
    }

    private double calculateAveragePrice(List<Course> courses) {
        return courses.stream()
                .filter(c -> c.getPricing() != null && !c.getPricing().isFree())
                .mapToDouble(c -> c.getPricing().getPrice())
                .average()
                .orElse(0.0);
    }

    private long countFreeCourses(List<Course> courses) {
        return courses.stream()
                .filter(c -> c.getPricing() != null && c.getPricing().isFree())
                .count();
    }

    private int calculateTotalDuration(List<Course> courses) {
        return courses.stream()
                .mapToInt(Course::getTotalDuration)
                .sum();
    }
}
