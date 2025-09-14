package org.example.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.model.Course;
import org.example.model.Review;
import org.example.model.SentimentType;
import org.example.repository.CourseRepository;
import org.example.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for managing ratings and reviews across the SkillMate AI platform
 */
@Service
public class RatingReviewManagementService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ReviewAnalysisService reviewAnalysisService;

    @Autowired
    private AIContentService aiContentService;

    /**
     * Submit a new review for a course
     */
    public Review submitReview(String userId, String courseId, int rating,
            String reviewText, List<String> aspectRatings) {

        // Validate course exists
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            throw new RuntimeException("Course not found");
        }

        Course course = courseOpt.get();

        // Create new review
        Review review = new Review();
        review.setUserId(userId);
        review.setCourseId(courseId);
        review.setCourseName(course.getTitle());
        review.setPlatform(course.getPlatform());
        review.setOverallRating(rating);
        review.setReviewText(reviewText);
        review.setReviewDate(LocalDateTime.now());
        review.setVerified(false); // Will be verified later
        review.setHelpfulCount(0);
        review.setLanguage("en"); // Default to English

        // Process aspect ratings
        if (aspectRatings != null && !aspectRatings.isEmpty()) {
            List<Review.AspectRating> aspects = parseAspectRatings(aspectRatings);
            review.setAspectRatings(aspects);
        }

        // Perform sentiment analysis
        Review.SentimentAnalysis sentiment = reviewAnalysisService.analyzeSentiment(reviewText);
        review.setSentimentAnalysis(sentiment);

        // Generate AI insights for the review
        Review.ReviewInsights insights = generateReviewInsights(review, course);
        review.setReviewInsights(insights);

        // Save review
        Review savedReview = reviewRepository.save(review);

        // Update course aggregate ratings
        updateCourseAggregateRatings(courseId);

        return savedReview;
    }

    /**
     * Get reviews for a specific course with filtering options
     */
    public List<Review> getCourseReviews(String courseId, String sortBy,
            String filterBy, int page, int size) {

        List<Review> reviews = reviewRepository.findByCourseId(courseId);

        // Apply filters
        if (filterBy != null) {
            reviews = applyFilters(reviews, filterBy);
        }

        // Apply sorting
        reviews = applySorting(reviews, sortBy);

        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, reviews.size());

        return reviews.subList(start, end);
    }

    /**
     * Get review analytics for a course
     */
    public ReviewAnalytics getCourseReviewAnalytics(String courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);

        if (reviews.isEmpty()) {
            return new ReviewAnalytics(); // Return empty analytics
        }

        ReviewAnalytics analytics = new ReviewAnalytics();
        analytics.setCourseId(courseId);
        analytics.setTotalReviews(reviews.size());

        // Calculate average rating
        double averageRating = reviews.stream()
                .mapToInt(Review::getOverallRating)
                .average()
                .orElse(0.0);
        analytics.setAverageRating(averageRating);

        // Calculate rating distribution
        Map<Integer, Long> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(
                        Review::getOverallRating,
                        Collectors.counting()));
        analytics.setRatingDistribution(ratingDistribution);

        // Calculate sentiment distribution
        Map<SentimentType, Long> sentimentDistribution = reviews.stream()
                .filter(r -> r.getSentimentAnalysis() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getSentimentAnalysis().getOverallSentiment(),
                        Collectors.counting()));
        analytics.setSentimentDistribution(sentimentDistribution);

        // Get most common positive/negative themes
        analytics.setTopPositiveThemes(extractTopThemes(reviews, SentimentType.POSITIVE));
        analytics.setTopNegativeThemes(extractTopThemes(reviews, SentimentType.NEGATIVE));

        // Calculate aspect ratings
        Map<String, Double> aspectAverages = calculateAspectAverages(reviews);
        analytics.setAspectAverages(aspectAverages);

        // Recent trends
        analytics.setRecentTrend(calculateRecentTrend(reviews));

        return analytics;
    }

    /**
     * Mark a review as helpful
     */
    public Review markReviewHelpful(String reviewId, String userId) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            throw new RuntimeException("Review not found");
        }

        Review review = reviewOpt.get();

        // Initialize helpful votes if not exists
        if (review.getHelpfulVotes() == null) {
            review.setHelpfulVotes(new ArrayList<>());
        }

        // Check if user already voted
        boolean alreadyVoted = review.getHelpfulVotes().stream()
                .anyMatch(vote -> vote.getUserId().equals(userId));

        if (!alreadyVoted) {
            Review.HelpfulVote vote = new Review.HelpfulVote();
            vote.setUserId(userId);
            vote.setVoteDate(LocalDateTime.now());
            review.getHelpfulVotes().add(vote);

            review.setHelpfulCount(review.getHelpfulVotes().size());
        }

        return reviewRepository.save(review);
    }

    /**
     * Generate AI-powered review summary for a course
     */
    public String generateReviewSummary(String courseId, int maxLength) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);

        if (reviews.isEmpty()) {
            return "No reviews available for this course yet.";
        }

        // Get recent and helpful reviews
        List<Review> significantReviews = reviews.stream()
                .filter(r -> r.getOverallRating() >= 1 && r.getReviewText() != null)
                .sorted((r1, r2) -> {
                    // Sort by helpfulness and recency
                    int helpfulCompare = Integer.compare(r2.getHelpfulCount(), r1.getHelpfulCount());
                    if (helpfulCompare != 0)
                        return helpfulCompare;
                    return r2.getReviewDate().compareTo(r1.getReviewDate());
                })
                .limit(20)
                .collect(Collectors.toList());

        // Build context for AI summarization
        StringBuilder reviewContext = new StringBuilder();
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        String courseTitle = courseOpt.map(Course::getTitle).orElse("Course");

        reviewContext.append("Course: ").append(courseTitle).append("\n");
        reviewContext.append("Total Reviews: ").append(reviews.size()).append("\n");
        reviewContext.append("Average Rating: ").append(
                String.format("%.1f", reviews.stream()
                        .mapToInt(Review::getOverallRating)
                        .average().orElse(0.0)))
                .append("\n\n");

        reviewContext.append("Key Reviews:\n");
        for (Review review : significantReviews) {
            reviewContext.append("Rating: ").append(review.getOverallRating())
                    .append("/5 - ").append(review.getReviewText()).append("\n");
        }

        // Generate AI summary
        String summary = aiContentService.generateStudyNotes(
                courseTitle,
                reviewContext.toString(),
                "review summary highlighting key strengths and areas for improvement");

        // Truncate if needed
        if (maxLength > 0 && summary.length() > maxLength) {
            summary = summary.substring(0, maxLength) + "...";
        }

        return summary;
    }

    /**
     * Detect and flag potentially fake reviews
     */
    public List<Review> detectSuspiciousReviews(String courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);
        List<Review> suspicious = new ArrayList<>();

        // Group reviews by user to detect patterns
        Map<String, List<Review>> reviewsByUser = reviews.stream()
                .collect(Collectors.groupingBy(Review::getUserId));

        for (Review review : reviews) {
            int suspicionScore = 0;

            // Check for common patterns of fake reviews

            // 1. Very short or generic text
            if (review.getReviewText() != null) {
                String text = review.getReviewText().trim();
                if (text.length() < 20 || isGenericText(text)) {
                    suspicionScore += 2;
                }
            }

            // 2. Extreme ratings with short text
            if ((review.getOverallRating() == 5 || review.getOverallRating() == 1) &&
                    (review.getReviewText() == null || review.getReviewText().length() < 30)) {
                suspicionScore += 3;
            }

            // 3. User has multiple reviews on same day
            List<Review> userReviews = reviewsByUser.get(review.getUserId());
            if (userReviews != null && userReviews.size() > 1) {
                long sameDay = userReviews.stream()
                        .filter(r -> r.getReviewDate().toLocalDate()
                                .equals(review.getReviewDate().toLocalDate()))
                        .count();
                if (sameDay > 1) {
                    suspicionScore += 2;
                }
            }

            // 4. Review date very close to course creation
            // (would need course creation date for this check)

            if (suspicionScore >= 3) {
                suspicious.add(review);
            }
        }

        return suspicious;
    }

    /**
     * Get trending courses based on recent positive reviews
     */
    public List<String> getTrendingCourses(String platform, int limit) {
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);

        // Get recent reviews
        List<Review> recentReviews = reviewRepository.findRecentReviews(lastMonth);

        if (platform != null && !platform.isEmpty()) {
            recentReviews = recentReviews.stream()
                    .filter(r -> platform.equals(r.getPlatform()))
                    .collect(Collectors.toList());
        }

        // Calculate trending score for each course
        Map<String, Double> courseScores = new HashMap<>();

        for (Review review : recentReviews) {
            String courseId = review.getCourseId();
            double score = calculateTrendingScore(review);
            courseScores.merge(courseId, score, Double::sum);
        }

        // Sort by score and return top courses
        return courseScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private List<Review.AspectRating> parseAspectRatings(List<String> aspectRatings) {
        List<Review.AspectRating> aspects = new ArrayList<>();

        for (String aspectString : aspectRatings) {
            // Expected format: "Content Quality:4" or "Instructor:5"
            String[] parts = aspectString.split(":");
            if (parts.length == 2) {
                try {
                    Review.AspectRating aspect = new Review.AspectRating();
                    aspect.setAspectName(parts[0].trim());
                    aspect.setRating(Integer.parseInt(parts[1].trim()));
                    aspects.add(aspect);
                } catch (NumberFormatException e) {
                    // Skip invalid format
                }
            }
        }

        return aspects;
    }

    private Review.ReviewInsights generateReviewInsights(Review review, Course course) {
        Review.ReviewInsights insights = new Review.ReviewInsights();

        // Extract key topics mentioned
        List<String> topics = extractTopicsFromReview(review.getReviewText(), course.getTags());
        insights.setKeyTopics(topics);

        // Calculate helpfulness prediction
        double helpfulnessPrediction = predictHelpfulness(review);
        insights.setHelpfulnessPrediction(helpfulnessPrediction);

        // Identify review category
        String category = categorizeReview(review);
        insights.setReviewCategory(category);

        return insights;
    }

    private void updateCourseAggregateRatings(String courseId) {
        List<Review> reviews = reviewRepository.findByCourseId(courseId);

        if (!reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getOverallRating)
                    .average()
                    .orElse(0.0);

            // Update course rating (assuming Course has setAverageRating method)
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                // course.setAverageRating(averageRating);
                // course.setTotalReviews(reviews.size());
                // courseRepository.save(course);
            }
        }
    }

    private List<Review> applyFilters(List<Review> reviews, String filterBy) {
        switch (filterBy.toLowerCase()) {
            case "positive":
                return reviews.stream()
                        .filter(r -> r.getOverallRating() >= 4)
                        .collect(Collectors.toList());
            case "negative":
                return reviews.stream()
                        .filter(r -> r.getOverallRating() <= 2)
                        .collect(Collectors.toList());
            case "verified":
                return reviews.stream()
                        .filter(Review::isVerified)
                        .collect(Collectors.toList());
            case "recent":
                LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
                return reviews.stream()
                        .filter(r -> r.getReviewDate().isAfter(lastWeek))
                        .collect(Collectors.toList());
            default:
                return reviews;
        }
    }

    private List<Review> applySorting(List<Review> reviews, String sortBy) {
        if (sortBy == null)
            sortBy = "recent";

        switch (sortBy.toLowerCase()) {
            case "helpful":
                return reviews.stream()
                        .sorted((r1, r2) -> Integer.compare(r2.getHelpfulCount(), r1.getHelpfulCount()))
                        .collect(Collectors.toList());
            case "rating_high":
                return reviews.stream()
                        .sorted((r1, r2) -> Integer.compare(r2.getOverallRating(), r1.getOverallRating()))
                        .collect(Collectors.toList());
            case "rating_low":
                return reviews.stream()
                        .sorted((r1, r2) -> Integer.compare(r1.getOverallRating(), r2.getOverallRating()))
                        .collect(Collectors.toList());
            case "recent":
            default:
                return reviews.stream()
                        .sorted((r1, r2) -> r2.getReviewDate().compareTo(r1.getReviewDate()))
                        .collect(Collectors.toList());
        }
    }

    private List<String> extractTopThemes(List<Review> reviews, SentimentType sentimentType) {
        Map<String, Integer> themeCount = new HashMap<>();

        for (Review review : reviews) {
            if (review.getSentimentAnalysis() != null &&
                    review.getSentimentAnalysis().getOverallSentiment() == sentimentType) {

                // Extract themes from review text (simplified)
                String[] words = review.getReviewText().toLowerCase().split("\\s+");
                for (String word : words) {
                    if (word.length() > 4 && !isStopWord(word)) {
                        themeCount.merge(word, 1, Integer::sum);
                    }
                }
            }
        }

        return themeCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Double> calculateAspectAverages(List<Review> reviews) {
        Map<String, List<Integer>> aspectRatings = new HashMap<>();

        for (Review review : reviews) {
            if (review.getAspectRatings() != null) {
                for (Review.AspectRating aspect : review.getAspectRatings()) {
                    aspectRatings.computeIfAbsent(aspect.getAspectName(), k -> new ArrayList<>())
                            .add(aspect.getRating());
                }
            }
        }

        Map<String, Double> averages = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : aspectRatings.entrySet()) {
            double average = entry.getValue().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            averages.put(entry.getKey(), average);
        }

        return averages;
    }

    private String calculateRecentTrend(List<Review> reviews) {
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);

        List<Review> recentReviews = reviews.stream()
                .filter(r -> r.getReviewDate().isAfter(lastMonth))
                .collect(Collectors.toList());

        if (recentReviews.size() < 5) {
            return "insufficient_data";
        }

        double recentAverage = recentReviews.stream()
                .mapToInt(Review::getOverallRating)
                .average()
                .orElse(0.0);

        double overallAverage = reviews.stream()
                .mapToInt(Review::getOverallRating)
                .average()
                .orElse(0.0);

        if (recentAverage > overallAverage + 0.2) {
            return "improving";
        } else if (recentAverage < overallAverage - 0.2) {
            return "declining";
        } else {
            return "stable";
        }
    }

    private boolean isGenericText(String text) {
        String[] genericPhrases = {
                "good course", "great course", "excellent", "recommended",
                "waste of time", "bad course", "terrible", "not recommended"
        };

        String lowerText = text.toLowerCase();
        for (String phrase : genericPhrases) {
            if (lowerText.contains(phrase) && text.length() < 50) {
                return true;
            }
        }
        return false;
    }

    private double calculateTrendingScore(Review review) {
        double score = 0.0;

        // Higher rating = higher score
        score += review.getOverallRating() * 2;

        // Recent reviews get bonus
        long daysAgo = java.time.Duration.between(review.getReviewDate(), LocalDateTime.now()).toDays();
        if (daysAgo <= 7) {
            score += 10;
        } else if (daysAgo <= 30) {
            score += 5;
        }

        // Helpful reviews get bonus
        score += review.getHelpfulCount() * 0.5;

        // Verified reviews get bonus
        if (review.isVerified()) {
            score += 3;
        }

        return score;
    }

    private List<String> extractTopicsFromReview(String reviewText, List<String> courseTags) {
        List<String> topics = new ArrayList<>();

        if (reviewText == null || courseTags == null) {
            return topics;
        }

        String lowerReviewText = reviewText.toLowerCase();
        for (String tag : courseTags) {
            if (lowerReviewText.contains(tag.toLowerCase())) {
                topics.add(tag);
            }
        }

        return topics;
    }

    private double predictHelpfulness(Review review) {
        double score = 0.0;

        // Longer reviews tend to be more helpful
        if (review.getReviewText() != null) {
            int length = review.getReviewText().length();
            if (length > 100)
                score += 0.3;
            if (length > 300)
                score += 0.2;
        }

        // Moderate ratings often more helpful than extremes
        int rating = review.getOverallRating();
        if (rating == 3 || rating == 4) {
            score += 0.2;
        }

        // Aspect ratings indicate thoughtful review
        if (review.getAspectRatings() != null && !review.getAspectRatings().isEmpty()) {
            score += 0.3;
        }

        return Math.min(score, 1.0);
    }

    private String categorizeReview(Review review) {
        if (review.getOverallRating() >= 4) {
            return "positive";
        } else if (review.getOverallRating() <= 2) {
            return "negative";
        } else {
            return "neutral";
        }
    }

    private boolean isStopWord(String word) {
        String[] stopWords = { "the", "and", "but", "for", "with", "this", "that", "very", "good", "bad" };
        return Arrays.asList(stopWords).contains(word.toLowerCase());
    }

    // Helper class for review analytics
    public static class ReviewAnalytics {
        private String courseId;
        private int totalReviews;
        private double averageRating;
        private Map<Integer, Long> ratingDistribution;
        private Map<SentimentType, Long> sentimentDistribution;
        private List<String> topPositiveThemes;
        private List<String> topNegativeThemes;
        private Map<String, Double> aspectAverages;
        private String recentTrend;

        public ReviewAnalytics() {
        }

        // Getters and setters
        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public int getTotalReviews() {
            return totalReviews;
        }

        public void setTotalReviews(int totalReviews) {
            this.totalReviews = totalReviews;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(double averageRating) {
            this.averageRating = averageRating;
        }

        public Map<Integer, Long> getRatingDistribution() {
            return ratingDistribution;
        }

        public void setRatingDistribution(Map<Integer, Long> ratingDistribution) {
            this.ratingDistribution = ratingDistribution;
        }

        public Map<SentimentType, Long> getSentimentDistribution() {
            return sentimentDistribution;
        }

        public void setSentimentDistribution(Map<SentimentType, Long> sentimentDistribution) {
            this.sentimentDistribution = sentimentDistribution;
        }

        public List<String> getTopPositiveThemes() {
            return topPositiveThemes;
        }

        public void setTopPositiveThemes(List<String> topPositiveThemes) {
            this.topPositiveThemes = topPositiveThemes;
        }

        public List<String> getTopNegativeThemes() {
            return topNegativeThemes;
        }

        public void setTopNegativeThemes(List<String> topNegativeThemes) {
            this.topNegativeThemes = topNegativeThemes;
        }

        public Map<String, Double> getAspectAverages() {
            return aspectAverages;
        }

        public void setAspectAverages(Map<String, Double> aspectAverages) {
            this.aspectAverages = aspectAverages;
        }

        public String getRecentTrend() {
            return recentTrend;
        }

        public void setRecentTrend(String recentTrend) {
            this.recentTrend = recentTrend;
        }
    }
}
