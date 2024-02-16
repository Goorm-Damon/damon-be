package damon.backend.repository;

import damon.backend.entity.Area;
import damon.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {


    // 페이징 처리 및 전체 리뷰 최신순으로 조회
    @Query("SELECT r FROM Review r ORDER BY r.id DESC")
    Page<Review> findAllOrderByIdDesc(Pageable pageable);

    // 지역별 리뷰 조회 및 페이징 처리
    @Query("SELECT r FROM Review r WHERE r.area = :area ORDER BY r.id DESC")
    Page<Review> findByArea(@Param("area") Area area, Pageable pageable);

    // 리뷰와 관련된 모든 댓글 및 대댓글 조회
    @Query("SELECT DISTINCT r FROM Review r LEFT JOIN FETCH r.reviewComments c WHERE r.id = :id")
    Optional<Review> findReviewWithCommentsAndRepliesByReviewId(@Param("id")Long id);

    // freeTags와 suggests 모두 포함하여 검색하는 메서드로 수정
    @Query("SELECT r FROM Review r WHERE :tag MEMBER OF r.freeTags OR :tag MEMBER OF r.suggests")
    Page<Review> findByTag(@Param("tag") String tag, Pageable pageable);

    // 좋아요 수가 많은 순으로 리뷰 조회
    @Query(value = "SELECT r.* FROM Review r LEFT JOIN Review_Like rl ON r.review_id = rl.review_id GROUP BY r.review_id ORDER BY COUNT(rl.member_id) DESC",
            countQuery = "SELECT count(*) FROM Review",
            nativeQuery = true)
    Page<Review> findTopReviewsByLikes(Pageable pageable);
}