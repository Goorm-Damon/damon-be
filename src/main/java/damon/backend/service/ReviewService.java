package damon.backend.service;

import damon.backend.dto.request.ReviewRequest;
import damon.backend.dto.response.ReviewCommentResponse;
import damon.backend.dto.response.ReviewListResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.*;
import damon.backend.exception.ReviewException;
import damon.backend.repository.MemberRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MemberRepository memberRepository;
    private final CommentStructureOrganizer commentStructureOrganizer;

    //게시글 등록
    public ReviewResponse postReview(ReviewRequest request, String providerName) {
        Member member = memberRepository.findByProviderName(providerName)
                .orElseThrow(ReviewException::memberNotFound);
        ;

        Review review = Review.create(request, member);
        review = reviewRepository.save(review); // 리뷰 저장


        List<ReviewCommentResponse> emptyCommentsList = new ArrayList<>(); // 새 리뷰에는 댓글이 없으므로 빈 리스트 생성
        return ReviewResponse.from(review, emptyCommentsList); // 저장된 리뷰와 빈 댓글 목록을 전달
    }

    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<ReviewListResponse> searchReviewList(int page, int pageSize, Area area) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Review> reviewPage;

        if (area != null) {
            reviewPage = reviewRepository.findByArea(area, pageable);
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
                .orElseThrow(ReviewException::reviewNotFound);

        // 댓글 구조화 로직을 사용하여 댓글 목록 가져오기 (상세 조회 시에만 호출)
        List<ReviewCommentResponse> organizedComments = commentStructureOrganizer.organizeCommentStructure(reviewId);

        // DTO 반환 댓글 목록 포함
        return ReviewResponse.from(review, organizedComments); // 이 부분에서 organizedComments를 DTO에 포함시켜야 합니다.
    }

    // 조회수 로직
    public void incrementReviewViewCount(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewException::reviewNotFound);

        review.incrementViewCount(); // 조회수 증가
        reviewRepository.save(review); // 변경 사항 저장

    }


    // 게시글 수정
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, String providerName) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewException::reviewNotFound);

        Member member = memberRepository.findByProviderName(providerName)
                .orElseThrow(ReviewException::memberNotFound);

        if (!review.getMember().getProviderName().equals(providerName)) {
            throw ReviewException.unauthorized();
        }

        // 리뷰 업데이트
        review.update(request);

        review = reviewRepository.save(review);

        // 댓글 구조를 다시 조직화
        List<ReviewCommentResponse> organizedComments = commentStructureOrganizer.organizeCommentStructure(reviewId);

        // 구조화된 댓글 목록을 포함하여 ReviewResponse 반환
        return ReviewResponse.from(review, organizedComments);
    }

    //게시글 삭제
    public void deleteReview(Long reviewId, String providerName) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewException::reviewNotFound);
        Member member = memberRepository.findByProviderName(providerName)
                .orElseThrow(ReviewException::memberNotFound);

        if (!review.getMember().getProviderName().equals(providerName)) {
            throw ReviewException.unauthorized();
        }
        reviewRepository.delete(review);
    }


    //좋아요 수 계산 (다시 누르면 좋아요 취소)
    @Transactional
    public void toggleLike(Long reviewId, String providerName) {
        Member member = memberRepository.findByProviderName(providerName)
                .orElseThrow(ReviewException::memberNotFound);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewException::reviewNotFound);

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewAndMember(review, member);
        if (existingLike.isPresent()) {
            reviewLikeRepository.delete(existingLike.get()); // 좋아요 제거
            review.decrementLikeCount(); // Review 엔티티 내 좋아요 수 감소 메서드
        } else {
            ReviewLike newLike = new ReviewLike();
            newLike.setReview(review);
            newLike.setMember(member);
            reviewLikeRepository.save(newLike); // 좋아요 추가
            review.incrementLikeCount(); // Review 엔티티 내 좋아요 수 증가 메서드
        }
    }

    //태그를 통한 검색
    @Transactional(readOnly = true)
    public List<ReviewListResponse> searchReviewsTag(String tag, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Review> reviewPage = reviewRepository.findByTag(tag, pageable);

        if (reviewPage.hasContent()) {
            return reviewPage.map(ReviewListResponse::from).toList();
        } else {
            return new ArrayList<>();
        }
    }

    // 메인 페이지 베스트 리뷰 조회
    public List<ReviewListResponse> findTopReviewsForMainPage(int size) {
        Pageable topFive = PageRequest.of(0, size, Sort.unsorted());
        Page<Review> reviews = reviewRepository.findTopReviewsByLikes(topFive);
        return reviews.stream()
                .map(ReviewListResponse::from)
                .collect(Collectors.toList());
    }

}
