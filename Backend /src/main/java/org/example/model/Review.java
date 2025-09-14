package org.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Review entity for course reviews from multiple sources
 */
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Indexed
    private String courseId;

    @Indexed
    private String userId;

    private String reviewerName;
    private String courseName;
    private String platform; // Platform where review was posted
    private String externalReviewId; // ID from external platform

    private int overallRating; // 1-5 scale
    private String reviewText;
    private String title;
    private boolean verified;
    private int helpfulCount;
    private String language;

    // Sentiment analysis results
    private SentimentAnalysis sentimentAnalysis;

    // Aspect ratings
    private List<AspectRating> aspectRatings;

    // Review insights
    private ReviewInsights reviewInsights;

    // Helpful votes
    private List<HelpfulVote> helpfulVotes;

    private LocalDateTime reviewDate;

    @CreatedDate
    private LocalDateTime createdAt;

    // Constructors
    public Review() {
    }

    public Review(String courseId, String userId, int rating, String reviewText) {
        this.courseId = courseId;
        this.userId = userId;
        this.overallRating = rating;
        this.reviewText = reviewText;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getExternalReviewId() {
        return externalReviewId;
    }

    public void setExternalReviewId(String externalReviewId) {
        this.externalReviewId = externalReviewId;
    }

    public int getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public SentimentAnalysis getSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public void setSentimentAnalysis(SentimentAnalysis sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }

    public List<AspectRating> getAspectRatings() {
        return aspectRatings;
    }

    public void setAspectRatings(List<AspectRating> aspectRatings) {
        this.aspectRatings = aspectRatings;
    }

    public ReviewInsights getReviewInsights() {
        return reviewInsights;
    }

    public void setReviewInsights(ReviewInsights reviewInsights) {
        this.reviewInsights = reviewInsights;
    }

    public List<HelpfulVote> getHelpfulVotes() {
        return helpfulVotes;
    }

    public void setHelpfulVotes(List<HelpfulVote> helpfulVotes) {
        this.helpfulVotes = helpfulVotes;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Nested classes
    public static class SentimentAnalysis {
        private double sentimentScore; // -1 (negative) to 1 (positive)
        private SentimentType overallSentiment;
        private double confidence; // 0 to 1
        private Map<String, Double> aspectSentiments; // instructor, content, value, etc.
        private Map<String, Integer> keywordFrequency;

        public SentimentAnalysis() {
        }

        public double getSentimentScore() {
            return sentimentScore;
        }

        public void setSentimentScore(double sentimentScore) {
            this.sentimentScore = sentimentScore;
        }

        public SentimentType getOverallSentiment() {
            return overallSentiment;
        }

        public void setOverallSentiment(SentimentType overallSentiment) {
            this.overallSentiment = overallSentiment;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public Map<String, Double> getAspectSentiments() {
            return aspectSentiments;
        }

        public void setAspectSentiments(Map<String, Double> aspectSentiments) {
            this.aspectSentiments = aspectSentiments;
        }

        public Map<String, Integer> getKeywordFrequency() {
            return keywordFrequency;
        }

        public void setKeywordFrequency(Map<String, Integer> keywordFrequency) {
            this.keywordFrequency = keywordFrequency;
        }
    }

    public static class AspectRating {
        private String aspectName;
        private int rating;

        public AspectRating() {
        }

        public String getAspectName() {
            return aspectName;
        }

        public void setAspectName(String aspectName) {
            this.aspectName = aspectName;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }
    }

    public static class ReviewInsights {
        private List<String> keyTopics;
        private double helpfulnessPrediction;
        private String reviewCategory;

        public ReviewInsights() {
        }

        public List<String> getKeyTopics() {
            return keyTopics;
        }

        public void setKeyTopics(List<String> keyTopics) {
            this.keyTopics = keyTopics;
        }

        public double getHelpfulnessPrediction() {
            return helpfulnessPrediction;
        }

        public void setHelpfulnessPrediction(double helpfulnessPrediction) {
            this.helpfulnessPrediction = helpfulnessPrediction;
        }

        public String getReviewCategory() {
            return reviewCategory;
        }

        public void setReviewCategory(String reviewCategory) {
            this.reviewCategory = reviewCategory;
        }
    }

    public static class HelpfulVote {
        private String userId;
        private LocalDateTime voteDate;

        public HelpfulVote() {
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public LocalDateTime getVoteDate() {
            return voteDate;
        }

        public void setVoteDate(LocalDateTime voteDate) {
            this.voteDate = voteDate;
        }
    }
}
