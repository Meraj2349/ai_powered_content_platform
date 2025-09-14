package org.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Course entity representing courses from multiple platforms
 */
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    @Indexed
    private String title;

    private String description;
    private String instructor;
    private String platform; // YouTube, Coursera, Udemy, etc.
    private String platformCourseId;
    private String url;
    private String thumbnailUrl;

    private CourseType type;
    private String category;
    private List<String> tags;
    private String language;
    private DifficultyLevel difficulty;

    // Pricing information
    private CoursePrice pricing;

    // Course structure
    private List<CourseModule> modules;
    private int totalDuration; // in minutes
    private int totalLessons;

    // Ratings and reviews
    private double averageRating;
    private int totalReviews;
    private List<String> reviewIds; // References to Review documents

    // Aggregated review analysis
    private ReviewAnalysis reviewAnalysis;

    // Course status
    private CourseStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Course() {
    }

    public Course(String title, String platform, String url) {
        this.title = title;
        this.platform = platform;
        this.url = url;
        this.status = CourseStatus.ACTIVE;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformCourseId() {
        return platformCourseId;
    }

    public void setPlatformCourseId(String platformCourseId) {
        this.platformCourseId = platformCourseId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public CoursePrice getPricing() {
        return pricing;
    }

    public void setPricing(CoursePrice pricing) {
        this.pricing = pricing;
    }

    public List<CourseModule> getModules() {
        return modules;
    }

    public void setModules(List<CourseModule> modules) {
        this.modules = modules;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(int totalLessons) {
        this.totalLessons = totalLessons;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public List<String> getReviewIds() {
        return reviewIds;
    }

    public void setReviewIds(List<String> reviewIds) {
        this.reviewIds = reviewIds;
    }

    public ReviewAnalysis getReviewAnalysis() {
        return reviewAnalysis;
    }

    public void setReviewAnalysis(ReviewAnalysis reviewAnalysis) {
        this.reviewAnalysis = reviewAnalysis;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Nested classes
    public static class CoursePrice {
        private boolean isFree;
        private double price;
        private String currency;
        private boolean hasDiscount;
        private double discountPrice;
        private LocalDateTime discountExpiryDate;

        // Constructors, getters, setters
        public CoursePrice() {
        }

        public boolean isFree() {
            return isFree;
        }

        public void setFree(boolean free) {
            isFree = free;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public boolean isHasDiscount() {
            return hasDiscount;
        }

        public void setHasDiscount(boolean hasDiscount) {
            this.hasDiscount = hasDiscount;
        }

        public double getDiscountPrice() {
            return discountPrice;
        }

        public void setDiscountPrice(double discountPrice) {
            this.discountPrice = discountPrice;
        }

        public LocalDateTime getDiscountExpiryDate() {
            return discountExpiryDate;
        }

        public void setDiscountExpiryDate(LocalDateTime discountExpiryDate) {
            this.discountExpiryDate = discountExpiryDate;
        }
    }

    public static class CourseModule {
        private String title;
        private String description;
        private List<Lesson> lessons;
        private int duration; // in minutes
        private int order;

        // Constructors, getters, setters
        public CourseModule() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Lesson> getLessons() {
            return lessons;
        }

        public void setLessons(List<Lesson> lessons) {
            this.lessons = lessons;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }

    public static class Lesson {
        private String title;
        private String description;
        private String videoUrl;
        private int duration; // in minutes
        private int order;
        private List<String> topics;
        private double qualityScore; // AI-generated quality score

        // Constructors, getters, setters
        public Lesson() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public List<String> getTopics() {
            return topics;
        }

        public void setTopics(List<String> topics) {
            this.topics = topics;
        }

        public double getQualityScore() {
            return qualityScore;
        }

        public void setQualityScore(double qualityScore) {
            this.qualityScore = qualityScore;
        }
    }

    public static class ReviewAnalysis {
        private double sentimentScore; // -1 to 1
        private Map<String, Integer> topicMentions;
        private List<String> commonComplaints;
        private List<String> commonPraises;
        private Map<String, Double> aspectRatings; // content, instructor, value, etc.

        // Constructors, getters, setters
        public ReviewAnalysis() {
        }

        public double getSentimentScore() {
            return sentimentScore;
        }

        public void setSentimentScore(double sentimentScore) {
            this.sentimentScore = sentimentScore;
        }

        public Map<String, Integer> getTopicMentions() {
            return topicMentions;
        }

        public void setTopicMentions(Map<String, Integer> topicMentions) {
            this.topicMentions = topicMentions;
        }

        public List<String> getCommonComplaints() {
            return commonComplaints;
        }

        public void setCommonComplaints(List<String> commonComplaints) {
            this.commonComplaints = commonComplaints;
        }

        public List<String> getCommonPraises() {
            return commonPraises;
        }

        public void setCommonPraises(List<String> commonPraises) {
            this.commonPraises = commonPraises;
        }

        public Map<String, Double> getAspectRatings() {
            return aspectRatings;
        }

        public void setAspectRatings(Map<String, Double> aspectRatings) {
            this.aspectRatings = aspectRatings;
        }
    }
}
