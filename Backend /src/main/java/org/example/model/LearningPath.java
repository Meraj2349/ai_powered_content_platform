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
 * Learning Path entity representing AI-generated custom learning paths
 */
@Document(collection = "learning_paths")
public class LearningPath {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private String description;
    private String skillGoal;
    private DifficultyLevel targetLevel;
    private List<String> preferredPlatforms;
    private String preferredLanguage;
    private int estimatedDurationHours;

    // Learning path structure
    private List<LearningPathStep> steps;
    private PathType pathType;
    private PathStatus status;

    // AI generation metadata
    private PathGenerationMetadata generationMetadata;

    // Progress tracking
    private LearningProgress progress;

    // Cost analysis
    private CostAnalysis costAnalysis;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public LearningPath() {
    }

    public LearningPath(String userId, String skillGoal, DifficultyLevel targetLevel) {
        this.userId = userId;
        this.skillGoal = skillGoal;
        this.targetLevel = targetLevel;
        this.status = PathStatus.DRAFT;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getSkillGoal() {
        return skillGoal;
    }

    public void setSkillGoal(String skillGoal) {
        this.skillGoal = skillGoal;
    }

    public DifficultyLevel getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(DifficultyLevel targetLevel) {
        this.targetLevel = targetLevel;
    }

    public List<String> getPreferredPlatforms() {
        return preferredPlatforms;
    }

    public void setPreferredPlatforms(List<String> preferredPlatforms) {
        this.preferredPlatforms = preferredPlatforms;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public int getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(int estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public List<LearningPathStep> getSteps() {
        return steps;
    }

    public void setSteps(List<LearningPathStep> steps) {
        this.steps = steps;
    }

    public PathType getPathType() {
        return pathType;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    public PathStatus getStatus() {
        return status;
    }

    public void setStatus(PathStatus status) {
        this.status = status;
    }

    public PathGenerationMetadata getGenerationMetadata() {
        return generationMetadata;
    }

    public void setGenerationMetadata(PathGenerationMetadata generationMetadata) {
        this.generationMetadata = generationMetadata;
    }

    public LearningProgress getProgress() {
        return progress;
    }

    public void setProgress(LearningProgress progress) {
        this.progress = progress;
    }

    public CostAnalysis getCostAnalysis() {
        return costAnalysis;
    }

    public void setCostAnalysis(CostAnalysis costAnalysis) {
        this.costAnalysis = costAnalysis;
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
    public static class LearningPathStep {
        private int stepNumber;
        private String title;
        private String description;
        private List<String> courseIds; // Multiple courses for this step
        private List<String> topics;
        private int estimatedDurationHours;
        private boolean isCompleted;
        private LocalDateTime completedAt;
        private StepType stepType;

        // Constructors, getters, setters
        public LearningPathStep() {
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public void setStepNumber(int stepNumber) {
            this.stepNumber = stepNumber;
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

        public List<String> getCourseIds() {
            return courseIds;
        }

        public void setCourseIds(List<String> courseIds) {
            this.courseIds = courseIds;
        }

        public List<String> getTopics() {
            return topics;
        }

        public void setTopics(List<String> topics) {
            this.topics = topics;
        }

        public int getEstimatedDurationHours() {
            return estimatedDurationHours;
        }

        public void setEstimatedDurationHours(int estimatedDurationHours) {
            this.estimatedDurationHours = estimatedDurationHours;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public void setCompleted(boolean completed) {
            isCompleted = completed;
        }

        public LocalDateTime getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }

        public StepType getStepType() {
            return stepType;
        }

        public void setStepType(StepType stepType) {
            this.stepType = stepType;
        }
    }

    public static class PathGenerationMetadata {
        private String aiModel;
        private double confidenceScore;
        private List<String> alternativePaths;
        private Map<String, Object> generationParams;
        private LocalDateTime generatedAt;

        // Constructors, getters, setters
        public PathGenerationMetadata() {
        }

        public String getAiModel() {
            return aiModel;
        }

        public void setAiModel(String aiModel) {
            this.aiModel = aiModel;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        public List<String> getAlternativePaths() {
            return alternativePaths;
        }

        public void setAlternativePaths(List<String> alternativePaths) {
            this.alternativePaths = alternativePaths;
        }

        public Map<String, Object> getGenerationParams() {
            return generationParams;
        }

        public void setGenerationParams(Map<String, Object> generationParams) {
            this.generationParams = generationParams;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
        }
    }

    public static class LearningProgress {
        private int totalSteps;
        private int completedSteps;
        private double completionPercentage;
        private int totalHoursSpent;
        private LocalDateTime lastActivityAt;
        private Map<String, Object> milestones;

        // Constructors, getters, setters
        public LearningProgress() {
        }

        public int getTotalSteps() {
            return totalSteps;
        }

        public void setTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
        }

        public int getCompletedSteps() {
            return completedSteps;
        }

        public void setCompletedSteps(int completedSteps) {
            this.completedSteps = completedSteps;
        }

        public double getCompletionPercentage() {
            return completionPercentage;
        }

        public void setCompletionPercentage(double completionPercentage) {
            this.completionPercentage = completionPercentage;
        }

        public int getTotalHoursSpent() {
            return totalHoursSpent;
        }

        public void setTotalHoursSpent(int totalHoursSpent) {
            this.totalHoursSpent = totalHoursSpent;
        }

        public LocalDateTime getLastActivityAt() {
            return lastActivityAt;
        }

        public void setLastActivityAt(LocalDateTime lastActivityAt) {
            this.lastActivityAt = lastActivityAt;
        }

        public Map<String, Object> getMilestones() {
            return milestones;
        }

        public void setMilestones(Map<String, Object> milestones) {
            this.milestones = milestones;
        }
    }

    public static class CostAnalysis {
        private double totalCost;
        private double freeCost;
        private double paidCost;
        private String currency;
        private List<PlatformCost> platformBreakdown;
        private boolean hasAlternativeFreeOption;

        // Constructors, getters, setters
        public CostAnalysis() {
        }

        public double getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(double totalCost) {
            this.totalCost = totalCost;
        }

        public double getFreeCost() {
            return freeCost;
        }

        public void setFreeCost(double freeCost) {
            this.freeCost = freeCost;
        }

        public double getPaidCost() {
            return paidCost;
        }

        public void setPaidCost(double paidCost) {
            this.paidCost = paidCost;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public List<PlatformCost> getPlatformBreakdown() {
            return platformBreakdown;
        }

        public void setPlatformBreakdown(List<PlatformCost> platformBreakdown) {
            this.platformBreakdown = platformBreakdown;
        }

        public boolean isHasAlternativeFreeOption() {
            return hasAlternativeFreeOption;
        }

        public void setHasAlternativeFreeOption(boolean hasAlternativeFreeOption) {
            this.hasAlternativeFreeOption = hasAlternativeFreeOption;
        }
    }

    public static class PlatformCost {
        private String platform;
        private double cost;
        private int courseCount;

        // Constructors, getters, setters
        public PlatformCost() {
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public int getCourseCount() {
            return courseCount;
        }

        public void setCourseCount(int courseCount) {
            this.courseCount = courseCount;
        }
    }
}
