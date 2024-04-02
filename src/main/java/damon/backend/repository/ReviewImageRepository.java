package damon.backend.repository;

import damon.backend.entity.Review;
import damon.backend.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewImageRepository  extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewId(Long reviewId);

    Optional<ReviewImage> findByUrlAndReview(String url, Review review);

    List<ReviewImage> findByUrlIn(List<String> urls);

}
