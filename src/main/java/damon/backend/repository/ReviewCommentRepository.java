package damon.backend.repository;

import damon.backend.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    // 특정 리뷰에 대한 모든 댓글 조회
    @Query("SELECT c FROM ReviewComment c WHERE c.review.id = :reviewId")
    List<ReviewComment> findByReviewId(Long reviewId);

    // 특정 댓글의 모든 대댓글 조회
    @Query("SELECT c FROM ReviewComment c WHERE c.parent.id = :parentId")
    List<ReviewComment> findByParentId(Long parentId);
}

