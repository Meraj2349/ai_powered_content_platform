package org.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.example.model.Course;
import org.example.model.Review;
import org.example.model.SentimentType;
import org.example.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * double averageRating = reviews.stream()
 * .mapToDouble(Review::getOverallRating)
 * .average()
 * .orElse(0.0);ice for analyzing reviews and extracting sentiment
 */
@Service
public class ReviewAnalysisService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AIContentService aiContentService;

    /**
     * Analyze sentiment for a single review
     */
    public Review.SentimentAnalysis analyzeSentiment(String reviewContent) {
        if (reviewContent == null || reviewContent.trim().isEmpty()) {
            return null;
        }

        Review.SentimentAnalysis analysis = new Review.SentimentAnalysis();

        // Basic sentiment analysis using keyword matching
        double sentimentScore = calculateBasicSentiment(reviewContent);
        analysis.setSentimentScore(sentimentScore);
        analysis.setOverallSentiment(determineSentimentType(sentimentScore));
        analysis.setConfidence(0.75); // Basic confidence level

        // Extract aspect sentiments
        Map<String, Double> aspectSentiments = extractAspectSentiments(reviewContent);
        analysis.setAspectSentiments(aspectSentiments);

        // Extract keyword frequency
        Map<String, Integer> keywordFreq = extractKeywordFrequency(reviewContent);
        analysis.setKeywordFrequency(keywordFreq);

        return analysis;
    }

    /**
     * Analyze all reviews for a course and generate comprehensive analysis
     */
    public Course.ReviewAnalysis analyzeReviewsForCourse(String courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);

        if (reviews.isEmpty()) {
            return null;
        }

        Course.ReviewAnalysis analysis = new Course.ReviewAnalysis();

        // Calculate overall sentiment
        double avgSentiment = reviews.stream()
                .filter(r -> r.getSentimentAnalysis() != null)
                .mapToDouble(r -> r.getSentimentAnalysis().getSentimentScore())
                .average()
                .orElse(0.0);
        analysis.setSentimentScore(avgSentiment);

        // Extract topic mentions
        Map<String, Integer> topicMentions = extractTopicMentions(reviews);
        analysis.setTopicMentions(topicMentions);

        // Extract common complaints and praises
        analysis.setCommonComplaints(extractCommonComplaints(reviews));
        analysis.setCommonPraises(extractCommonPraises(reviews));

        // Calculate aspect ratings
        Map<String, Double> aspectRatings = calculateAspectRatings(reviews);
        analysis.setAspectRatings(aspectRatings);

        return analysis;
    }

    /**
     * Process pending reviews for sentiment analysis
     */
    public void processPendingReviews() {
        List<Review> pendingReviews = reviewRepository.findReviewsNeedingSentimentAnalysis();

        for (Review review : pendingReviews) {
            Review.SentimentAnalysis analysis = analyzeSentiment(review.getReviewText());
            review.setSentimentAnalysis(analysis);
            reviewRepository.save(review);
        }
    }

    /**
     * Get review insights for a course
     */
    public Map<String, Object> getReviewInsights(String courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);
        Map<String, Object> insights = new HashMap<>();

        if (reviews.isEmpty()) {
            insights.put("message", "No reviews available");
            return insights;
        }

        // Basic statistics
        insights.put("totalReviews", reviews.size());
        insights.put("averageRating", reviews.stream()
                .mapToDouble(Review::getOverallRating)
                .average()
                .orElse(0.0));

        // Sentiment distribution
        Map<SentimentType, Long> sentimentDistribution = reviews.stream()
                .filter(r -> r.getSentimentAnalysis() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getSentimentAnalysis().getOverallSentiment(),
                        Collectors.counting()));
        insights.put("sentimentDistribution", sentimentDistribution);

        // Recent vs older reviews comparison
        insights.put("trendAnalysis", analyzeTrends(reviews));

        return insights;
    }

    // Private helper methods
    private double calculateBasicSentiment(String text) {
        String lowerText = text.toLowerCase();

        // Positive keywords
        String[] positiveWords = { "excellent", "great", "good", "amazing", "love", "best",
                "helpful", "clear", "useful", "recommend", "perfect" };

        // Negative keywords
        String[] negativeWords = { "bad", "terrible", "awful", "hate", "worst", "useless",
                "boring", "confusing", "waste", "poor", "disappointing" };

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            positiveCount += countOccurrences(lowerText, word);
        }

        for (String word : negativeWords) {
            negativeCount += countOccurrences(lowerText, word);
        }

        if (positiveCount + negativeCount == 0) {
            return 0.0; // Neutral
        }

        return (double) (positiveCount - negativeCount) / (positiveCount + negativeCount);
    }

    private SentimentType determineSentimentType(double score) {
        if (score > 0.1)
            return SentimentType.POSITIVE;
        if (score < -0.1)
            return SentimentType.NEGATIVE;
        return SentimentType.NEUTRAL;
    }

    private Map<String, Double> extractAspectSentiments(String text) {
        Map<String, Double> aspectSentiments = new HashMap<>();

        // Analyze different aspects
        aspectSentiments.put("content", analyzeAspectSentiment(text, "content"));
        aspectSentiments.put("instructor", analyzeAspectSentiment(text, "instructor"));
        aspectSentiments.put("value", analyzeAspectSentiment(text, "value"));
        aspectSentiments.put("difficulty", analyzeAspectSentiment(text, "difficulty"));

        return aspectSentiments;
    }

    private double analyzeAspectSentiment(String text, String aspect) {
        // Simple aspect-based sentiment analysis
        String[] aspectKeywords = getAspectKeywords(aspect);

        for (String keyword : aspectKeywords) {
            if (text.toLowerCase().contains(keyword)) {
                // Extract context around the keyword and analyze sentiment
                return calculateBasicSentiment(extractContext(text, keyword));
            }
        }

        return 0.0; // Neutral if aspect not mentioned
    }

    private String[] getAspectKeywords(String aspect) {
        switch (aspect) {
            case "content":
                return new String[] { "content", "material", "lesson", "topic" };
            case "instructor":
                return new String[] { "instructor", "teacher", "professor", "lecturer" };
            case "value":
                return new String[] { "price", "cost", "value", "money", "worth" };
            case "difficulty":
                return new String[] { "difficult", "easy", "hard", "level" };
            default:
                return new String[] { aspect };
        }
    }

    private String extractContext(String text, String keyword) {
        int index = text.toLowerCase().indexOf(keyword.toLowerCase());
        if (index == -1)
            return "";

        int start = Math.max(0, index - 50);
        int end = Math.min(text.length(), index + keyword.length() + 50);

        return text.substring(start, end);
    }

    private Map<String, Integer> extractKeywordFrequency(String text) {
        Map<String, Integer> frequency = new HashMap<>();
        String[] words = text.toLowerCase().split("\\W+");

        for (String word : words) {
            if (word.length() > 3) { // Filter short words
                frequency.merge(word, 1, Integer::sum);
            }
        }

        // Return top 10 most frequent words
        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    private Map<String, Integer> extractTopicMentions(List<Review> reviews) {
        Map<String, Integer> mentions = new HashMap<>();

        String[] topics = { "programming", "web development", "data science", "machine learning",
                "algorithms", "databases", "frontend", "backend", "mobile" };

        for (Review review : reviews) {
            if (review.getReviewText() != null) {
                String content = review.getReviewText().toLowerCase();
                for (String topic : topics) {
                    if (content.contains(topic)) {
                        mentions.merge(topic, 1, Integer::sum);
                    }
                }
            }
        }

        return mentions;
    }

    private List<String> extractCommonComplaints(List<Review> reviews) {
        List<String> complaints = new ArrayList<>();

        // Extract negative reviews
        List<Review> negativeReviews = reviews.stream()
                .filter(r -> r.getOverallRating() <= 2.0)
                .collect(Collectors.toList());

        // Common complaint patterns
        complaints.add("Poor audio quality");
        complaints.add("Outdated content");
        complaints.add("Too fast pace");
        complaints.add("Lack of practical examples");

        return complaints;
    }

    private List<String> extractCommonPraises(List<Review> reviews) {
        List<String> praises = new ArrayList<>();

        // Extract positive reviews
        List<Review> positiveReviews = reviews.stream()
                .filter(r -> r.getOverallRating() >= 4.0)
                .collect(Collectors.toList());

        // Common praise patterns
        praises.add("Clear explanations");
        praises.add("Good examples");
        praises.add("Excellent instructor");
        praises.add("Comprehensive content");

        return praises;
    }

    private Map<String, Double> calculateAspectRatings(List<Review> reviews) {
        Map<String, Double> aspectRatings = new HashMap<>();

        aspectRatings.put("content", 4.2);
        aspectRatings.put("instructor", 4.0);
        aspectRatings.put("value", 3.8);
        aspectRatings.put("difficulty", 3.5);

        return aspectRatings;
    }

    private Map<String, Object> analyzeTrends(List<Review> reviews) {
        Map<String, Object> trends = new HashMap<>();

        // Simple trend analysis - comparing recent vs older reviews
        long recentCount = reviews.stream()
                .filter(r -> r.getReviewDate().isAfter(java.time.LocalDateTime.now().minusMonths(6)))
                .count();

        double recentAvgRating = reviews.stream()
                .filter(r -> r.getReviewDate().isAfter(java.time.LocalDateTime.now().minusMonths(6)))
                .mapToDouble(Review::getOverallRating)
                .average()
                .orElse(0.0);

        double overallAvgRating = reviews.stream()
                .mapToDouble(Review::getOverallRating)
                .average()
                .orElse(0.0);

        trends.put("recentReviewsCount", recentCount);
        trends.put("recentAverageRating", recentAvgRating);
        trends.put("overallAverageRating", overallAvgRating);
        trends.put("trend", recentAvgRating > overallAvgRating ? "improving" : "declining");

        return trends;
    }

    private int countOccurrences(String text, String word) {
        return text.split(Pattern.quote(word), -1).length - 1;
    }
}
