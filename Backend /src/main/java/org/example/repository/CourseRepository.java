package org.example.repository;

import org.example.model.Course;
import org.example.model.CourseStatus;
import org.example.model.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    // Find courses by platform
    List<Course> findByPlatform(String platform);

    // Find courses by category
    List<Course> findByCategory(String category);

    // Find courses by difficulty level
    List<Course> findByDifficulty(DifficultyLevel difficulty);

    // Find courses by status
    List<Course> findByStatus(CourseStatus status);

    // Find courses by language
    List<Course> findByLanguage(String language);

    // Find free courses
    @Query("{ 'pricing.isFree': true }")
    List<Course> findFreeCourses();

    // Find courses within price range
    @Query("{ 'pricing.price': { $gte: ?0, $lte: ?1 } }")
    List<Course> findCoursesByPriceRange(double minPrice, double maxPrice);

    // Find courses by instructor
    List<Course> findByInstructor(String instructor);

    // Find courses by tags
    List<Course> findByTagsIn(List<String> tags);

    // Search courses by title or description
    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<Course> searchCourses(String searchTerm);

    // Find top-rated courses
    @Query("{ 'averageRating': { $gte: ?0 } }")
    Page<Course> findTopRatedCourses(double minRating, Pageable pageable);

    // Find courses with minimum number of reviews
    @Query("{ 'totalReviews': { $gte: ?0 } }")
    List<Course> findCoursesWithMinReviews(int minReviews);

    // Find courses by platform and category
    List<Course> findByPlatformAndCategory(String platform, String category);

    // Find courses by external platform ID
    Optional<Course> findByPlatformCourseId(String platformCourseId);

    // Advanced search with multiple filters
    @Query("{ $and: [ " +
            "{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }, "
            +
            "{ 'category': ?1 }, " +
            "{ 'difficulty': ?2 }, " +
            "{ 'language': ?3 }, " +
            "{ 'status': 'ACTIVE' } ] }")
    Page<Course> findCoursesWithFilters(String searchTerm, String category,
            DifficultyLevel difficulty, String language, Pageable pageable);

    // Find courses for learning path generation
    @Query("{ $and: [ " +
            "{ 'tags': { $in: ?0 } }, " +
            "{ 'difficulty': ?1 }, " +
            "{ 'language': ?2 }, " +
            "{ 'status': 'ACTIVE' } ] }")
    List<Course> findCoursesForLearningPath(List<String> topics, DifficultyLevel difficulty, String language);

    // Count courses by platform
    @Query(value = "{ 'platform': ?0 }", count = true)
    long countByPlatform(String platform);
}
