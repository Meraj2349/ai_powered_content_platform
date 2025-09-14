package org.example.service;

import org.example.model.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for generating AI-powered learning paths
 */
@Service
public class LearningPathGenerationService {

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseAggregationService courseAggregationService;

    @Autowired
    private AIContentService aiContentService;

    /**
     * Generate a personalized learning path
     */
    public LearningPath generateLearningPath(String userId, String skillGoal,
            DifficultyLevel targetLevel,
            List<String> preferredPlatforms,
            String preferredLanguage,
            boolean preferFreeCourses,
            double maxBudget) {

        // Create new learning path
        LearningPath learningPath = new LearningPath(userId, skillGoal, targetLevel);
        learningPath.setTitle("Learning Path: " + skillGoal);
        learningPath.setPreferredPlatforms(preferredPlatforms);
        learningPath.setPreferredLanguage(preferredLanguage);

        // Generate AI-powered path description
        String description = aiContentService.generateLearningPathSuggestion(
                skillGoal, targetLevel.toString(), "Comprehensive skill development");
        learningPath.setDescription(description);

        // Find relevant courses
        List<Course> availableCourses = findRelevantCourses(
                skillGoal, targetLevel, preferredLanguage, preferredPlatforms);

        // Generate learning steps
        List<LearningPath.LearningPathStep> steps = generateLearningSteps(
                availableCourses, skillGoal, preferFreeCourses, maxBudget);
        learningPath.setSteps(steps);

        // Calculate duration and cost
        int totalDuration = steps.stream()
                .mapToInt(LearningPath.LearningPathStep::getEstimatedDurationHours)
                .sum();
        learningPath.setEstimatedDurationHours(totalDuration);

        // Set path type based on course sources
        learningPath.setPathType(determinePathType(steps));

        // Generate cost analysis
        LearningPath.CostAnalysis costAnalysis = generateCostAnalysis(steps);
        learningPath.setCostAnalysis(costAnalysis);

        // Initialize progress tracking
        LearningPath.LearningProgress progress = new LearningPath.LearningProgress();
        progress.setTotalSteps(steps.size());
        progress.setCompletedSteps(0);
        progress.setCompletionPercentage(0.0);
        learningPath.setProgress(progress);

        // Set generation metadata
        LearningPath.PathGenerationMetadata metadata = new LearningPath.PathGenerationMetadata();
        metadata.setAiModel("GPT-3.5-turbo");
        metadata.setConfidenceScore(0.85);
        metadata.setGeneratedAt(LocalDateTime.now());
        learningPath.setGenerationMetadata(metadata);

        learningPath.setStatus(PathStatus.DRAFT);

        return learningPathRepository.save(learningPath);
    }

    /**
     * Generate alternative learning paths
     */
    public List<LearningPath> generateAlternativePaths(String userId, String skillGoal,
            DifficultyLevel targetLevel,
            String preferredLanguage) {
        List<LearningPath> alternatives = new ArrayList<>();

        // Generate different path variations
        // 1. Free-only path
        LearningPath freePath = generateLearningPath(userId, skillGoal, targetLevel,
                Arrays.asList("YouTube", "Coursera"), preferredLanguage, true, 0.0);
        freePath.setTitle("Free Learning Path: " + skillGoal);
        alternatives.add(freePath);

        // 2. Premium path
        LearningPath premiumPath = generateLearningPath(userId, skillGoal, targetLevel,
                Arrays.asList("Udemy", "Pluralsight", "Coursera"), preferredLanguage, false, 500.0);
        premiumPath.setTitle("Premium Learning Path: " + skillGoal);
        alternatives.add(premiumPath);

        // 3. Mixed path
        LearningPath mixedPath = generateLearningPath(userId, skillGoal, targetLevel,
                Arrays.asList("YouTube", "Udemy", "edX"), preferredLanguage, false, 100.0);
        mixedPath.setTitle("Balanced Learning Path: " + skillGoal);
        alternatives.add(mixedPath);

        return alternatives;
    }

    /**
     * Optimize existing learning path
     */
    public LearningPath optimizeLearningPath(String learningPathId,
            Map<String, Object> optimizationCriteria) {
        Optional<LearningPath> pathOpt = learningPathRepository.findById(learningPathId);
        if (!pathOpt.isPresent()) {
            throw new RuntimeException("Learning path not found");
        }

        LearningPath path = pathOpt.get();

        // Re-analyze courses with current criteria
        List<Course> updatedCourses = findRelevantCourses(
                path.getSkillGoal(),
                path.getTargetLevel(),
                path.getPreferredLanguage(),
                path.getPreferredPlatforms());

        // Re-generate steps with optimization
        List<LearningPath.LearningPathStep> optimizedSteps = optimizeSteps(
                path.getSteps(), updatedCourses, optimizationCriteria);
        path.setSteps(optimizedSteps);

        // Update metadata
        path.getGenerationMetadata().setGeneratedAt(LocalDateTime.now());
        path.setUpdatedAt(LocalDateTime.now());

        return learningPathRepository.save(path);
    }

    /**
     * Get path recommendations based on user history
     */
    public List<LearningPath> getRecommendedPaths(String userId, int limit) {
        // Get user's completed paths to understand preferences
        List<LearningPath> completedPaths = learningPathRepository.findCompletedPathsByUser(userId);

        if (completedPaths.isEmpty()) {
            // Return popular paths for new users
            return learningPathRepository.findPopularPaths(
                    org.springframework.data.domain.PageRequest.of(0, limit)).getContent();
        }

        // Analyze user preferences
        Map<String, Integer> skillFrequency = new HashMap<>();
        Map<DifficultyLevel, Integer> levelPreference = new HashMap<>();

        for (LearningPath path : completedPaths) {
            skillFrequency.merge(path.getSkillGoal(), 1, Integer::sum);
            levelPreference.merge(path.getTargetLevel(), 1, Integer::sum);
        }

        // Find similar paths
        List<LearningPath> recommendations = new ArrayList<>();
        for (String skill : skillFrequency.keySet()) {
            List<LearningPath> similarPaths = learningPathRepository.findSimilarPaths(
                    skill, getMostPreferredLevel(levelPreference));
            recommendations.addAll(similarPaths);
        }

        return recommendations.stream()
                .filter(path -> !path.getUserId().equals(userId)) // Exclude user's own paths
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private List<Course> findRelevantCourses(String skillGoal, DifficultyLevel targetLevel,
            String preferredLanguage, List<String> preferredPlatforms) {

        // Generate skill keywords
        List<String> skillKeywords = aiContentService.generateSkillKeywords(skillGoal);

        // Find courses matching criteria
        List<Course> courses = courseRepository.findCoursesForLearningPath(
                skillKeywords, targetLevel, preferredLanguage);

        // Filter by preferred platforms if specified
        if (preferredPlatforms != null && !preferredPlatforms.isEmpty()) {
            courses = courses.stream()
                    .filter(course -> preferredPlatforms.contains(course.getPlatform()))
                    .collect(Collectors.toList());
        }

        // Sort by quality score
        courses.sort((c1, c2) -> Double.compare(
                courseAggregationService.calculateCourseQualityScore(c2),
                courseAggregationService.calculateCourseQualityScore(c1)));

        return courses;
    }

    private List<LearningPath.LearningPathStep> generateLearningSteps(
            List<Course> availableCourses, String skillGoal,
            boolean preferFreeCourses, double maxBudget) {

        List<LearningPath.LearningPathStep> steps = new ArrayList<>();
        double currentBudget = 0.0;

        // Group courses by learning progression
        Map<StepType, List<Course>> coursesByStep = categorizeCoursesByStep(availableCourses);

        int stepNumber = 1;

        // Foundation step
        if (coursesByStep.containsKey(StepType.FOUNDATION)) {
            LearningPath.LearningPathStep foundationStep = createStep(
                    stepNumber++, "Foundation", StepType.FOUNDATION,
                    coursesByStep.get(StepType.FOUNDATION), preferFreeCourses, maxBudget - currentBudget);
            steps.add(foundationStep);
            currentBudget += calculateStepCost(foundationStep);
        }

        // Core learning steps
        if (coursesByStep.containsKey(StepType.CORE_LEARNING)) {
            LearningPath.LearningPathStep coreStep = createStep(
                    stepNumber++, "Core Learning", StepType.CORE_LEARNING,
                    coursesByStep.get(StepType.CORE_LEARNING), preferFreeCourses, maxBudget - currentBudget);
            steps.add(coreStep);
            currentBudget += calculateStepCost(coreStep);
        }

        // Practice step
        if (coursesByStep.containsKey(StepType.PRACTICE) && currentBudget < maxBudget) {
            LearningPath.LearningPathStep practiceStep = createStep(
                    stepNumber++, "Practice & Projects", StepType.PRACTICE,
                    coursesByStep.get(StepType.PRACTICE), preferFreeCourses, maxBudget - currentBudget);
            steps.add(practiceStep);
        }

        return steps;
    }

    private Map<StepType, List<Course>> categorizeCoursesByStep(List<Course> courses) {
        Map<StepType, List<Course>> categorized = new HashMap<>();

        for (Course course : courses) {
            StepType stepType = determineStepType(course);
            categorized.computeIfAbsent(stepType, k -> new ArrayList<>()).add(course);
        }

        return categorized;
    }

    private StepType determineStepType(Course course) {
        String title = course.getTitle().toLowerCase();

        // Basic categorization logic
        if (title.contains("beginner") || title.contains("introduction") || title.contains("basics")) {
            return StepType.FOUNDATION;
        } else if (title.contains("project") || title.contains("practice") || title.contains("hands-on")) {
            return StepType.PRACTICE;
        } else if (title.contains("advanced") || title.contains("mastery")) {
            return StepType.ASSESSMENT;
        } else {
            return StepType.CORE_LEARNING;
        }
    }

    private LearningPath.LearningPathStep createStep(int stepNumber, String title, StepType stepType,
            List<Course> availableCourses,
            boolean preferFreeCourses, double remainingBudget) {

        LearningPath.LearningPathStep step = new LearningPath.LearningPathStep();
        step.setStepNumber(stepNumber);
        step.setTitle(title);
        step.setStepType(stepType);
        step.setCompleted(false);

        // Select best courses for this step
        List<String> selectedCourseIds = selectCoursesForStep(
                availableCourses, preferFreeCourses, remainingBudget, 3);
        step.setCourseIds(selectedCourseIds);

        // Calculate estimated duration
        int totalDuration = selectedCourseIds.stream()
                .mapToInt(courseId -> {
                    Optional<Course> course = courseRepository.findById(courseId);
                    return course.map(Course::getTotalDuration).orElse(0);
                })
                .sum();
        step.setEstimatedDurationHours(totalDuration / 60); // Convert minutes to hours

        // Extract topics
        List<String> topics = selectedCourseIds.stream()
                .map(courseId -> courseRepository.findById(courseId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(course -> course.getTags() != null ? course.getTags().stream() : Stream.empty())
                .distinct()
                .collect(Collectors.toList());
        step.setTopics(topics);

        step.setDescription("Complete the selected courses to master " + title.toLowerCase());

        return step;
    }

    private List<String> selectCoursesForStep(List<Course> availableCourses,
            boolean preferFreeCourses,
            double remainingBudget,
            int maxCourses) {

        List<Course> selectedCourses = availableCourses.stream()
                .filter(course -> {
                    if (preferFreeCourses) {
                        return course.getPricing() != null && course.getPricing().isFree();
                    }
                    return true;
                })
                .filter(course -> {
                    if (course.getPricing() != null && !course.getPricing().isFree()) {
                        return course.getPricing().getPrice() <= remainingBudget;
                    }
                    return true;
                })
                .limit(maxCourses)
                .collect(Collectors.toList());

        return selectedCourses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());
    }

    private double calculateStepCost(LearningPath.LearningPathStep step) {
        return step.getCourseIds().stream()
                .mapToDouble(courseId -> {
                    Optional<Course> course = courseRepository.findById(courseId);
                    if (course.isPresent() && course.get().getPricing() != null) {
                        return course.get().getPricing().isFree() ? 0.0 : course.get().getPricing().getPrice();
                    }
                    return 0.0;
                })
                .sum();
    }

    private PathType determinePathType(List<LearningPath.LearningPathStep> steps) {
        Set<String> platforms = new HashSet<>();
        int totalCourses = 0;

        for (LearningPath.LearningPathStep step : steps) {
            totalCourses += step.getCourseIds().size();
            for (String courseId : step.getCourseIds()) {
                Optional<Course> course = courseRepository.findById(courseId);
                if (course.isPresent()) {
                    platforms.add(course.get().getPlatform());
                }
            }
        }

        if (totalCourses == 1) {
            return PathType.SINGLE_COURSE;
        } else if (platforms.size() == 1) {
            return PathType.MULTI_COURSE_SINGLE_PLATFORM;
        } else {
            return PathType.MULTI_COURSE_MULTI_PLATFORM;
        }
    }

    private LearningPath.CostAnalysis generateCostAnalysis(List<LearningPath.LearningPathStep> steps) {
        LearningPath.CostAnalysis analysis = new LearningPath.CostAnalysis();

        double totalCost = 0.0;
        double freeCost = 0.0;
        double paidCost = 0.0;
        Map<String, Double> platformCosts = new HashMap<>();

        for (LearningPath.LearningPathStep step : steps) {
            for (String courseId : step.getCourseIds()) {
                Optional<Course> courseOpt = courseRepository.findById(courseId);
                if (courseOpt.isPresent()) {
                    Course course = courseOpt.get();
                    if (course.getPricing() != null) {
                        if (course.getPricing().isFree()) {
                            freeCost += 0.0;
                        } else {
                            double price = course.getPricing().getPrice();
                            paidCost += price;
                            totalCost += price;
                            platformCosts.merge(course.getPlatform(), price, Double::sum);
                        }
                    }
                }
            }
        }

        analysis.setTotalCost(totalCost);
        analysis.setFreeCost(freeCost);
        analysis.setPaidCost(paidCost);
        analysis.setCurrency("USD");

        List<LearningPath.PlatformCost> platformBreakdown = platformCosts.entrySet().stream()
                .map(entry -> {
                    LearningPath.PlatformCost pc = new LearningPath.PlatformCost();
                    pc.setPlatform(entry.getKey());
                    pc.setCost(entry.getValue());
                    return pc;
                })
                .collect(Collectors.toList());
        analysis.setPlatformBreakdown(platformBreakdown);

        analysis.setHasAlternativeFreeOption(freeCost > 0);

        return analysis;
    }

    private List<LearningPath.LearningPathStep> optimizeSteps(
            List<LearningPath.LearningPathStep> currentSteps,
            List<Course> updatedCourses,
            Map<String, Object> criteria) {

        // For now, return current steps - can be enhanced with optimization logic
        return currentSteps;
    }

    private DifficultyLevel getMostPreferredLevel(Map<DifficultyLevel, Integer> levelPreference) {
        return levelPreference.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DifficultyLevel.BEGINNER);
    }
}
