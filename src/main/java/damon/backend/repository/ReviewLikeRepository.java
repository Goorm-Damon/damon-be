package damon.backend.repository;

import damon.backend.entity.Review;
import damon.backend.entity.ReviewLike;
import damon.backend.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {


    // 특정 리뷰와 유저에 대한 좋아요 존재 여부 확인
    Optional<ReviewLike> findByReviewAndUser(Review review, User user);


    // 좋아요 누른 리뷰 조회
    @Query("SELECT rl.review FROM ReviewLike rl WHERE rl.user = :user")
    Page<Review> findReviewsByUser(@Param("user") User user, Pageable pageable);



}