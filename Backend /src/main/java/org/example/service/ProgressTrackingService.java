package org.example.service;

import org.example.model.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for tracking user progress across learning paths and courses
 */
@Service
public class ProgressTrackingService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AIContentService aiContentService;

    /**
     * Record course start for a user
     */
    public UserProgress startCourse(String userId, String courseId, String learningPathId) {
        UserProgress progress = findOrCreateUserProgress(userId);

        // Record course start
        UserProgress.CourseProgress courseProgress = new UserProgress.CourseProgress();
        courseProgress.setCourseId(courseId);
        courseProgress.setStartDate(LocalDateTime.now());
        courseProgress.setStatus(CourseStatus.IN_PROGRESS);
        courseProgress.setCompletionPercentage(0.0);
        courseProgress.setTimeSpent(0);

        // Add to course list
        progress.getCourses().add(courseProgress);

        // Record user action
        recordUserAction(progress, UserProgress.UserAction.ActionType.COURSE_STARTED, courseId,
                "Started course: " + getCourseTitle(courseId));

        // Update learning path progress if applicable
        if (learningPathId != null) {
            updateLearningPathProgress(progress, learningPathId, courseId);
        }

        return userProgressRepository.save(progress);
    }

    /**
     * Update course progress
     */
    public UserProgress updateCourseProgress(String userId, String courseId,
            double completionPercentage,
            int timeSpentMinutes) {
        UserProgress progress = findOrCreateUserProgress(userId);

        // Find course progress
        Optional<UserProgress.CourseProgress> courseProgressOpt = progress.getCourses().stream()
                .filter(cp -> cp.getCourseId().equals(courseId))
                .findFirst();

        if (!courseProgressOpt.isPresent()) {
            throw new RuntimeException("Course progress not found. User must start the course first.");
        }

        UserProgress.CourseProgress courseProgress = courseProgressOpt.get();
        courseProgress.setCompletionPercentage(completionPercentage);
        courseProgress.setTimeSpent(courseProgress.getTimeSpent() + timeSpentMinutes);
        courseProgress.setLastActivityDate(LocalDateTime.now());

        // Check if course is completed
        if (completionPercentage >= 100.0 && courseProgress.getStatus() != CourseStatus.COMPLETED) {
            courseProgress.setStatus(CourseStatus.COMPLETED);
            courseProgress.setCompletionDate(LocalDateTime.now());

            recordUserAction(progress, UserProgress.UserAction.ActionType.COURSE_COMPLETED, courseId,
                    "Completed course: " + getCourseTitle(courseId));

            // Update achievements
            updateAchievements(progress, courseId);
        }

        // Update total learning time
        updateTotalLearningTime(progress);

        return userProgressRepository.save(progress);
    }

    /**
     * Add bookmark for a course or lesson
     */
    public UserProgress addBookmark(String userId, String courseId, String lessonId,
            String title, String notes) {
        UserProgress progress = findOrCreateUserProgress(userId);

        UserProgress.Bookmark bookmark = new UserProgress.Bookmark();
        bookmark.setCourseId(courseId);
        bookmark.setLessonId(lessonId);
        bookmark.setTitle(title);
        bookmark.setNotes(notes);
        bookmark.setCreatedAt(LocalDateTime.now());

        progress.getBookmarks().add(bookmark);

        recordUserAction(progress, UserProgress.UserAction.ActionType.BOOKMARK_ADDED, courseId,
                "Bookmarked: " + title);

        return userProgressRepository.save(progress);
    }

    /**
     * Add or update note
     */
    public UserProgress addNote(String userId, String courseId, String lessonId,
            String title, String content, List<String> tags) {
        UserProgress progress = findOrCreateUserProgress(userId);

        UserProgress.Note note = new UserProgress.Note();
        note.setCourseId(courseId);
        note.setLessonId(lessonId);
        note.setTitle(title);
        note.setContent(content);
        note.setTags(tags);
        note.setCreatedAt(LocalDateTime.now());

        progress.getNotes().add(note);

        recordUserAction(progress, UserProgress.UserAction.ActionType.NOTE_ADDED, courseId,
                "Added note: " + title);

        return userProgressRepository.save(progress);
    }

    /**
     * Generate personalized study notes using AI
     */
    public String generateStudyNotes(String userId, String courseId, String topicFocus) {
        UserProgress progress = findOrCreateUserProgress(userId);

        // Get course details
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            throw new RuntimeException("Course not found");
        }

        Course course = courseOpt.get();

        // Get user's existing notes for context
        List<UserProgress.Note> existingNotes = progress.getNotes().stream()
                .filter(note -> note.getCourseId().equals(courseId))
                .collect(Collectors.toList());

        // Generate AI-powered study notes
        String studyNotes = aiContentService.generateStudyNotes(
                course.getTitle(), course.getDescription(), topicFocus);

        // Save generated notes
        addNote(userId, courseId, null, "AI Study Notes: " + topicFocus,
                studyNotes, Arrays.asList("ai-generated", topicFocus));

        return studyNotes;
    }

    /**
     * Get learning analytics for a user
     */
    public UserProgress.LearningAnalytics getLearningAnalytics(String userId) {
        UserProgress progress = findOrCreateUserProgress(userId);

        UserProgress.LearningAnalytics analytics = new UserProgress.LearningAnalytics();

        // Calculate basic stats
        int totalCourses = progress.getCourses().size();
        int completedCourses = (int) progress.getCourses().stream()
                .filter(cp -> cp.getStatus() == CourseStatus.COMPLETED)
                .count();

        analytics.setTotalCoursesStarted(totalCourses);
        analytics.setTotalCoursesCompleted(completedCourses);
        analytics.setCompletionRate(totalCourses > 0 ? (double) completedCourses / totalCourses * 100 : 0.0);

        // Calculate learning streaks
        int currentStreak = calculateCurrentLearningStreak(progress);
        int longestStreak = calculateLongestLearningStreak(progress);
        analytics.setCurrentLearningStreak(currentStreak);
        analytics.setLongestLearningStreak(longestStreak);

        // Calculate time analytics
        int totalMinutes = progress.getCourses().stream()
                .mapToInt(UserProgress.CourseProgress::getTimeSpent)
                .sum();
        analytics.setTotalLearningTimeMinutes(totalMinutes);
        analytics.setAverageDailyLearningMinutes(calculateAverageDailyLearning(progress));

        // Preferred learning times
        analytics.setPreferredLearningHours(calculatePreferredLearningHours(progress));

        // Monthly progress
        analytics.setMonthlyProgress(calculateMonthlyProgress(progress));

        // Skill progress
        analytics.setSkillProgress(calculateSkillProgress(progress));

        return analytics;
    }

    /**
     * Get learning recommendations based on user progress
     */
    public List<String> getLearningRecommendations(String userId) {
        UserProgress progress = findOrCreateUserProgress(userId);
        List<String> recommendations = new ArrayList<>();

        // Analyze user's learning patterns
        UserProgress.LearningAnalytics analytics = getLearningAnalytics(userId);

        // Recommendation based on completion rate
        if (analytics.getCompletionRate() < 50.0) {
            recommendations.add("Focus on completing started courses before beginning new ones");
        }

        // Recommendation based on learning streak
        if (analytics.getCurrentLearningStreak() == 0) {
            recommendations.add("Start a new learning streak by studying for at least 15 minutes today");
        } else if (analytics.getCurrentLearningStreak() > 7) {
            recommendations.add("Great job on your learning streak! Consider taking a rest day to avoid burnout");
        }

        // Recommendation based on daily learning time
        if (analytics.getAverageDailyLearningMinutes() < 30) {
            recommendations.add("Try to increase your daily learning time to at least 30 minutes for better retention");
        }

        // Course-specific recommendations
        List<String> inProgressCourses = progress.getCourses().stream()
                .filter(cp -> cp.getStatus() == CourseStatus.IN_PROGRESS)
                .map(UserProgress.CourseProgress::getCourseId)
                .collect(Collectors.toList());

        if (inProgressCourses.size() > 3) {
            recommendations.add("You have many courses in progress. Consider focusing on 2-3 courses at a time");
        }

        // Note-taking recommendations
        if (progress.getNotes().size() < progress.getCourses().size()) {
            recommendations.add("Take more notes while learning to improve retention and create study materials");
        }

        return recommendations;
    }

    /**
     * Calculate learning path completion progress
     */
    public double calculateLearningPathProgress(String userId, String learningPathId) {
        Optional<LearningPath> pathOpt = learningPathRepository.findById(learningPathId);
        if (!pathOpt.isPresent()) {
            return 0.0;
        }

        LearningPath path = pathOpt.get();
        UserProgress userProgress = findOrCreateUserProgress(userId);

        int totalSteps = path.getSteps().size();
        int completedSteps = 0;

        for (LearningPath.LearningPathStep step : path.getSteps()) {
            boolean stepCompleted = true;
            for (String courseId : step.getCourseIds()) {
                boolean courseCompleted = userProgress.getCourses().stream()
                        .anyMatch(cp -> cp.getCourseId().equals(courseId) &&
                                cp.getStatus() == CourseStatus.COMPLETED);
                if (!courseCompleted) {
                    stepCompleted = false;
                    break;
                }
            }
            if (stepCompleted) {
                completedSteps++;
            }
        }

        return totalSteps > 0 ? (double) completedSteps / totalSteps * 100 : 0.0;
    }

    // Private helper methods
    public UserProgress findOrCreateUserProgress(String userId) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUserId(userId);

        if (progressOpt.isPresent()) {
            return progressOpt.get();
        }

        // Create new user progress
        UserProgress progress = new UserProgress();
        progress.setUserId(userId);
        progress.setStartDate(LocalDateTime.now());
        progress.setCourses(new ArrayList<>());
        progress.setLearningPaths(new ArrayList<>());
        progress.setBookmarks(new ArrayList<>());
        progress.setNotes(new ArrayList<>());
        progress.setAchievements(new ArrayList<>());
        progress.setUserActions(new ArrayList<>());

        return progress;
    }

    private void recordUserAction(UserProgress progress, UserProgress.UserAction.ActionType actionType,
            String relatedId, String description) {
        UserProgress.UserAction action = new UserProgress.UserAction();
        action.setActionType(actionType);
        action.setTimestamp(LocalDateTime.now());
        action.setRelatedId(relatedId);
        action.setDescription(description);

        progress.getUserActions().add(action);

        // Keep only last 100 actions to prevent excessive growth
        if (progress.getUserActions().size() > 100) {
            progress.getUserActions().remove(0);
        }
    }

    private String getCourseTitle(String courseId) {
        Optional<Course> course = courseRepository.findById(courseId);
        return course.map(Course::getTitle).orElse("Unknown Course");
    }

    private void updateLearningPathProgress(UserProgress progress, String learningPathId, String courseId) {
        // Find or create learning path progress
        Optional<UserProgress.LearningPathProgress> pathProgressOpt = progress.getLearningPaths().stream()
                .filter(lp -> lp.getLearningPathId().equals(learningPathId))
                .findFirst();

        UserProgress.LearningPathProgress pathProgress;
        if (pathProgressOpt.isPresent()) {
            pathProgress = pathProgressOpt.get();
        } else {
            pathProgress = new UserProgress.LearningPathProgress();
            pathProgress.setLearningPathId(learningPathId);
            pathProgress.setStartDate(LocalDateTime.now());
            pathProgress.setCompletedSteps(new ArrayList<>());
            progress.getLearningPaths().add(pathProgress);
        }

        pathProgress.setLastActivityDate(LocalDateTime.now());
    }

    private void updateAchievements(UserProgress progress, String courseId) {
        List<String> newAchievements = new ArrayList<>();

        // First course completion
        long completedCourses = progress.getCourses().stream()
                .filter(cp -> cp.getStatus() == CourseStatus.COMPLETED)
                .count();

        if (completedCourses == 1) {
            newAchievements.add("First Course Completed");
        } else if (completedCourses == 5) {
            newAchievements.add("Course Enthusiast - 5 Courses Completed");
        } else if (completedCourses == 10) {
            newAchievements.add("Learning Champion - 10 Courses Completed");
        }

        // Add new achievements
        for (String achievement : newAchievements) {
            if (progress.getAchievements().stream().noneMatch(a -> a.getTitle().equals(achievement))) {
                UserProgress.Achievement newAchievement = new UserProgress.Achievement();
                newAchievement.setTitle(achievement);
                newAchievement.setDescription("Achievement earned through dedicated learning");
                newAchievement.setEarnedDate(LocalDateTime.now());
                progress.getAchievements().add(newAchievement);

                recordUserAction(progress, UserProgress.UserAction.ActionType.ACHIEVEMENT_EARNED,
                        courseId, "Earned achievement: " + achievement);
            }
        }
    }

    private void updateTotalLearningTime(UserProgress progress) {
        int totalMinutes = progress.getCourses().stream()
                .mapToInt(UserProgress.CourseProgress::getTimeSpent)
                .sum();
        progress.setTotalLearningTimeMinutes(totalMinutes);
    }

    private int calculateCurrentLearningStreak(UserProgress progress) {
        // Simplified calculation - can be enhanced
        List<UserProgress.UserAction> recentActions = progress.getUserActions().stream()
                .filter(action -> action.getTimestamp().isAfter(LocalDateTime.now().minusDays(30)))
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .collect(Collectors.toList());

        int streak = 0;
        LocalDateTime currentDate = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);

        for (int i = 0; i < 30; i++) {
            LocalDateTime checkDate = currentDate.minusDays(i);
            boolean hasActivity = recentActions.stream()
                    .anyMatch(action -> action.getTimestamp().truncatedTo(ChronoUnit.DAYS).equals(checkDate));

            if (hasActivity) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }

    private int calculateLongestLearningStreak(UserProgress progress) {
        // Simplified calculation
        return calculateCurrentLearningStreak(progress);
    }

    private int calculateAverageDailyLearning(UserProgress progress) {
        if (progress.getStartDate() == null) {
            return 0;
        }

        long daysSinceStart = ChronoUnit.DAYS.between(progress.getStartDate(), LocalDateTime.now());
        if (daysSinceStart == 0) {
            daysSinceStart = 1;
        }

        return (int) (progress.getTotalLearningTimeMinutes() / daysSinceStart);
    }

    private List<Integer> calculatePreferredLearningHours(UserProgress progress) {
        // Analyze user action timestamps to find preferred hours
        Map<Integer, Integer> hourFrequency = new HashMap<>();

        for (UserProgress.UserAction action : progress.getUserActions()) {
            int hour = action.getTimestamp().getHour();
            hourFrequency.merge(hour, 1, Integer::sum);
        }

        return hourFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Map<String, Integer> calculateMonthlyProgress(UserProgress progress) {
        Map<String, Integer> monthlyProgress = new HashMap<>();

        for (UserProgress.CourseProgress courseProgress : progress.getCourses()) {
            if (courseProgress.getCompletionDate() != null) {
                String monthKey = courseProgress.getCompletionDate().getYear() + "-" +
                        courseProgress.getCompletionDate().getMonthValue();
                monthlyProgress.merge(monthKey, 1, Integer::sum);
            }
        }

        return monthlyProgress;
    }

    private Map<String, Double> calculateSkillProgress(UserProgress progress) {
        Map<String, Double> skillProgress = new HashMap<>();

        // Analyze completed courses to determine skill progress
        for (UserProgress.CourseProgress courseProgress : progress.getCourses()) {
            if (courseProgress.getStatus() == CourseStatus.COMPLETED) {
                Optional<Course> courseOpt = courseRepository.findById(courseProgress.getCourseId());
                if (courseOpt.isPresent()) {
                    Course course = courseOpt.get();
                    if (course.getTags() != null) {
                        for (String tag : course.getTags()) {
                            skillProgress.merge(tag, 1.0, Double::sum);
                        }
                    }
                }
            }
        }

        return skillProgress;
    }
}
