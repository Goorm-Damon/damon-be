package damon.backend.repository;

import damon.backend.entity.Member;
import damon.backend.entity.Review;
import damon.backend.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {


    //특정 리뷰의 좋아요 추가 or 취소
    Optional<ReviewLike> findByReviewAndMember(Review review, Member member);


}
