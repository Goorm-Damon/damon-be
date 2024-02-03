package damon.backend.service;

import damon.backend.dto.request.ReviewRequest;
import damon.backend.dto.response.ReviewCommentResponse;
import damon.backend.dto.response.ReviewListResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.Area;
import damon.backend.entity.Review;
import damon.backend.entity.ReviewComment;
import damon.backend.entity.ReviewLike;
import damon.backend.repository.ReviewCommentRepository;
import damon.backend.repository.ReviewLikeRepository;
import damon.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    //게시글 등록
    public ReviewResponse postReview(ReviewRequest request, String providerName) {
        Review review = new Review();
        review.setTitle(request.getTitle());
        review.setStartDate(request.getStartDate());
        review.setEndDate(request.getEndDate());
        review.setArea(request.getArea());
        review.setCost(request.getCost());
        review.setSuggests(request.getSuggests());
        review.setFreeTags(request.getFreeTags());
        review.setContent(request.getContent());
        review = reviewRepository.save(review); // 리뷰 저장


        List<ReviewCommentResponse> emptyCommentsList = new ArrayList<>(); // 새 리뷰에는 댓글이 없으므로 빈 리스트 생성
        return ReviewResponse.from(review, emptyCommentsList); // 저장된 리뷰와 빈 댓글 목록을 전달
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<ReviewListResponse> searchReviewList(int page, int pageSize, Area area) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Review> reviewPage;

        if (area != null) {
            reviewPage = reviewRepository.findByAreaWithSuggests(area, pageable);
        } else {
            reviewPage = reviewRepository.findAll(pageable);
        }
        if (reviewPage.hasContent()) {
            return reviewPage.map(ReviewListResponse::from).toList();
        } else {
            return new ArrayList<>();
        }
    }

    //게시글 상세 내용 조회 (댓글 포함)
    @Transactional(readOnly = true)
    public ReviewResponse searchReview(Long reviewId, String providerName) {
        Review review = reviewRepository.findReviewWithCommentsAndRepliesByReviewId(reviewId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다"));

        // 댓글 계층 구조 생성
        List<ReviewCommentResponse> organizedComments = organizeCommentStructure(reviewId);

        // DTO 반환
        return ReviewResponse.from(review, organizedComments); // 이 부분에서 organizedComments를 DTO에 포함시켜야 합니다.
    }

    // 조회수 로직 (따로 분리시킨 이유는 updateTime 때문에)
    public void incrementReviewViewCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다"));
        review.setViewCount(review.getViewCount() + 1);
        review.setUpdateTime(review.getUpdateTime()); // updateTime은 변경되지 않도록
        reviewRepository.save(review);

    }

    // 조회수 증가 메소드
    private void incrementReviewViewCount(Review review) {
        review.setViewCount(review.getViewCount() + 1);
        reviewRepository.save(review);
    }

    // 게시글 수정
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, String providerName) {
//        Long memberId = SecurityUtils.getCurrentUserId();
//        if (memberId == null || !isAuthor(reviewId, memberId)) {
//            throw new RuntimeException("Unauthorized access");
//        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // 리뷰 객체의 필드를 request의 데이터로 업데이트
        review.setTitle(request.getTitle());
        review.setStartDate(request.getStartDate());
        review.setEndDate(request.getEndDate());
        review.setArea(request.getArea());
        review.setCost(request.getCost());
        review.setSuggests(request.getSuggests());
        review.setFreeTags(request.getFreeTags());
        review.setContent(request.getContent());

        // updateTime을 수동으로 현재 시간으로 설정
        review.setUpdateTime(ZonedDateTime.now());

        review = reviewRepository.save(review);

        // 댓글 구조를 다시 조직화
        List<ReviewCommentResponse> organizedComments = organizeCommentStructure(reviewId);

        // 구조화된 댓글 목록을 포함하여 ReviewResponse 반환
        return ReviewResponse.from(review, organizedComments);
    }

    //게시글 삭제
    public void deleteReview(Long reviewId, String providerName) {
//        Long memberId = SecurityUtils.getCurrentUserId();
//        if (memberId == null || !isAuthor(reviewId, memberId)) {
//            throw new RuntimeException("Unauthorized access");
//        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다"));

        reviewRepository.delete(review);
    }


    //좋아요 수 계산 (다시 누르면 좋아요 취소)
    public ReviewResponse toggleLike(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewId(reviewId);

        if (existingLike.isPresent()) {
            reviewLikeRepository.delete(existingLike.get());
        } else {
            ReviewLike newLike = new ReviewLike();
            newLike.setReview(review); // Review 설정
            // 필요한 속성 설정
            reviewLikeRepository.save(newLike);
        }
        // 댓글 구조를 다시 조직화하여 리뷰 전체 상태를 반환
        return searchReview(reviewId);
    }

    //태그를 통한 검색
    @Transactional(readOnly = true)
    public List<ReviewListResponse> searchReviewsByFreeTag(String freeTag, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Review> reviewPage = reviewRepository.findByFreeTag(freeTag, pageable);

        if (reviewPage.hasContent()) {
            return reviewPage.map(ReviewListResponse::from).toList();
        } else {
            return new ArrayList<>();
        }
    }

}