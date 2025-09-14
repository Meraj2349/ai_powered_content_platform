package org.example.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.model.Review;
import org.example.model.SentimentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    // Find reviews by course
    List<Review> findByCourseId(String courseId);

    // Find reviews by user
    List<Review> findByUserId(String userId);

    // Find reviews by platform
    List<Review> findByPlatform(String platform);

    // Find reviews by rating range
    List<Review> findByOverallRatingBetween(int minRating, int maxRating);

    // Find reviews by sentiment type
    @Query("{ 'sentimentAnalysis.overallSentiment': ?0 }")
    List<Review> findBySentimentType(SentimentType sentimentType);

    // Find verified reviews
    List<Review> findByVerified(boolean isVerified);

    // Find reviews with minimum helpful votes
    @Query("{ 'helpfulCount': { $gte: ?0 } }")
    List<Review> findReviewsWithMinHelpfulVotes(int minVotes);

    // Find recent reviews
    @Query("{ 'reviewDate': { $gte: ?0 } }")
    List<Review> findRecentReviews(LocalDateTime after);

    // Find reviews by course and rating
    List<Review> findByCourseIdAndOverallRatingGreaterThanEqual(String courseId, int minRating);

    // Get average rating for course
    @Query(value = "{ 'courseId': ?0 }", fields = "{ 'overallRating': 1 }")
    List<Review> findRatingsByCourseId(String courseId);

    // Find reviews with high sentiment score
    @Query("{ 'sentimentAnalysis.sentimentScore': { $gte: ?0 } }")
    List<Review> findReviewsWithHighSentiment(double minSentimentScore);

    // Search reviews by content
    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    List<Review> searchReviewsByContent(String searchTerm);

    // Count reviews by course
    long countByCourseId(String courseId);

    // Find top-rated reviews
    Page<Review> findByOrderByOverallRatingDescHelpfulCountDesc(Pageable pageable);

    // Find reviews for sentiment analysis
    @Query("{ $and: [ { 'sentimentAnalysis': { $exists: false } }, { 'content': { $ne: null } } ] }")
    List<Review> findReviewsNeedingSentimentAnalysis();
}
