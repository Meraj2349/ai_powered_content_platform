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
 * User Progress entity for tracking comprehensive learning progress
 */
@Document(collection = "user_progress")
public class UserProgress {

    @Id
    private String id;

    @Indexed
    private String userId;

    private LocalDateTime startDate;
    private int totalLearningTimeMinutes;

    // Collections for tracking different aspects
    private List<CourseProgress> courses;
    private List<LearningPathProgress> learningPaths;
    private List<Bookmark> bookmarks;
    private List<Note> notes;
    private List<Achievement> achievements;
    private List<UserAction> userActions;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public UserProgress() {
    }

    public UserProgress(String userId) {
        this.userId = userId;
        this.startDate = LocalDateTime.now();
        this.totalLearningTimeMinutes = 0;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public int getTotalLearningTimeMinutes() {
        return totalLearningTimeMinutes;
    }

    public void setTotalLearningTimeMinutes(int totalLearningTimeMinutes) {
        this.totalLearningTimeMinutes = totalLearningTimeMinutes;
    }

    public List<CourseProgress> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseProgress> courses) {
        this.courses = courses;
    }

    public List<LearningPathProgress> getLearningPaths() {
        return learningPaths;
    }

    public void setLearningPaths(List<LearningPathProgress> learningPaths) {
        this.learningPaths = learningPaths;
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public List<UserAction> getUserActions() {
        return userActions;
    }

    public void setUserActions(List<UserAction> userActions) {
        this.userActions = userActions;
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
    public static class CourseProgress {
        private String courseId;
        private CourseStatus status;
        private double completionPercentage;
        private int timeSpent; // in minutes
        private LocalDateTime startDate;
        private LocalDateTime lastActivityDate;
        private LocalDateTime completionDate;

        public CourseProgress() {
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public CourseStatus getStatus() {
            return status;
        }

        public void setStatus(CourseStatus status) {
            this.status = status;
        }

        public double getCompletionPercentage() {
            return completionPercentage;
        }

        public void setCompletionPercentage(double completionPercentage) {
            this.completionPercentage = completionPercentage;
        }

        public int getTimeSpent() {
            return timeSpent;
        }

        public void setTimeSpent(int timeSpent) {
            this.timeSpent = timeSpent;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public LocalDateTime getLastActivityDate() {
            return lastActivityDate;
        }

        public void setLastActivityDate(LocalDateTime lastActivityDate) {
            this.lastActivityDate = lastActivityDate;
        }

        public LocalDateTime getCompletionDate() {
            return completionDate;
        }

        public void setCompletionDate(LocalDateTime completionDate) {
            this.completionDate = completionDate;
        }
    }

    public static class LearningPathProgress {
        private String learningPathId;
        private LocalDateTime startDate;
        private LocalDateTime lastActivityDate;
        private List<String> completedSteps;

        public LearningPathProgress() {
        }

        public String getLearningPathId() {
            return learningPathId;
        }

        public void setLearningPathId(String learningPathId) {
            this.learningPathId = learningPathId;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public LocalDateTime getLastActivityDate() {
            return lastActivityDate;
        }

        public void setLastActivityDate(LocalDateTime lastActivityDate) {
            this.lastActivityDate = lastActivityDate;
        }

        public List<String> getCompletedSteps() {
            return completedSteps;
        }

        public void setCompletedSteps(List<String> completedSteps) {
            this.completedSteps = completedSteps;
        }
    }

    public static class Bookmark {
        private String courseId;
        private String lessonId;
        private String title;
        private String notes;
        private LocalDateTime createdAt;

        public Bookmark() {
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getLessonId() {
            return lessonId;
        }

        public void setLessonId(String lessonId) {
            this.lessonId = lessonId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class Note {
        private String courseId;
        private String lessonId;
        private String title;
        private String content;
        private List<String> tags;
        private LocalDateTime createdAt;

        public Note() {
        }

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getLessonId() {
            return lessonId;
        }

        public void setLessonId(String lessonId) {
            this.lessonId = lessonId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class Achievement {
        private String title;
        private String description;
        private LocalDateTime earnedDate;

        public Achievement() {
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

        public LocalDateTime getEarnedDate() {
            return earnedDate;
        }

        public void setEarnedDate(LocalDateTime earnedDate) {
            this.earnedDate = earnedDate;
        }
    }

    public static class LearningAnalytics {
        private int totalCoursesStarted;
        private int totalCoursesCompleted;
        private double completionRate;
        private int currentLearningStreak;
        private int longestLearningStreak;
        private int totalLearningTimeMinutes;
        private int averageDailyLearningMinutes;
        private List<Integer> preferredLearningHours;
        private Map<String, Integer> monthlyProgress;
        private Map<String, Double> skillProgress;

        public LearningAnalytics() {
        }

        public int getTotalCoursesStarted() {
            return totalCoursesStarted;
        }

        public void setTotalCoursesStarted(int totalCoursesStarted) {
            this.totalCoursesStarted = totalCoursesStarted;
        }

        public int getTotalCoursesCompleted() {
            return totalCoursesCompleted;
        }

        public void setTotalCoursesCompleted(int totalCoursesCompleted) {
            this.totalCoursesCompleted = totalCoursesCompleted;
        }

        public double getCompletionRate() {
            return completionRate;
        }

        public void setCompletionRate(double completionRate) {
            this.completionRate = completionRate;
        }

        public int getCurrentLearningStreak() {
            return currentLearningStreak;
        }

        public void setCurrentLearningStreak(int currentLearningStreak) {
            this.currentLearningStreak = currentLearningStreak;
        }

        public int getLongestLearningStreak() {
            return longestLearningStreak;
        }

        public void setLongestLearningStreak(int longestLearningStreak) {
            this.longestLearningStreak = longestLearningStreak;
        }

        public int getTotalLearningTimeMinutes() {
            return totalLearningTimeMinutes;
        }

        public void setTotalLearningTimeMinutes(int totalLearningTimeMinutes) {
            this.totalLearningTimeMinutes = totalLearningTimeMinutes;
        }

        public int getAverageDailyLearningMinutes() {
            return averageDailyLearningMinutes;
        }

        public void setAverageDailyLearningMinutes(int averageDailyLearningMinutes) {
            this.averageDailyLearningMinutes = averageDailyLearningMinutes;
        }

        public List<Integer> getPreferredLearningHours() {
            return preferredLearningHours;
        }

        public void setPreferredLearningHours(List<Integer> preferredLearningHours) {
            this.preferredLearningHours = preferredLearningHours;
        }

        public Map<String, Integer> getMonthlyProgress() {
            return monthlyProgress;
        }

        public void setMonthlyProgress(Map<String, Integer> monthlyProgress) {
            this.monthlyProgress = monthlyProgress;
        }

        public Map<String, Double> getSkillProgress() {
            return skillProgress;
        }

        public void setSkillProgress(Map<String, Double> skillProgress) {
            this.skillProgress = skillProgress;
        }
    }

    public static class UserAction {
        private ActionType actionType;
        private LocalDateTime timestamp;
        private String relatedId;
        private String description;

        public UserAction() {
        }

        public ActionType getActionType() {
            return actionType;
        }

        public void setActionType(ActionType actionType) {
            this.actionType = actionType;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getRelatedId() {
            return relatedId;
        }

        public void setRelatedId(String relatedId) {
            this.relatedId = relatedId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public enum ActionType {
            COURSE_STARTED,
            COURSE_COMPLETED,
            LESSON_COMPLETED,
            BOOKMARK_ADDED,
            NOTE_ADDED,
            ACHIEVEMENT_EARNED,
            LEARNING_PATH_STARTED,
            LEARNING_PATH_COMPLETED
        }
    }
}
