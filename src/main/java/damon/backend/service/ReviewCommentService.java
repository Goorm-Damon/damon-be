package damon.backend.service;

import damon.backend.dto.request.ReviewCommentRequest;
import damon.backend.dto.response.ReviewCommentResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.Member;
import damon.backend.entity.Review;
import damon.backend.entity.ReviewComment;
import damon.backend.repository.MemberRepository;
import damon.backend.repository.ReviewCommentRepository;
import damon.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommentService {
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final MemberRepository memberRepository;
    private final ReviewService reviewService;


    // 댓글 등록
    public ReviewResponse postComment(Long reviewId, String providerName, ReviewCommentRequest request) {
        // 사용자 조회
        Member member = memberRepository.findByProvidername(providerName)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
        // 리뷰 조회
        Review review = reviewRepository.findReviewWithCommentsAndRepliesByReviewId(reviewId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다"));

        // 새로운 댓글 생성
        ReviewComment reviewComment = new ReviewComment();
        reviewComment.setContent(request.getContent());

        // 부모 댓글이 있을 경우
        if (request.getParentId() != null) {
            ReviewComment parent = reviewCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다"));

            // 대대댓글 생성 금지 로직
            if (parent.getParent() != null) {
                throw new RuntimeException("대대댓글은 허용되지 않습니다");
            }

            // 부모 댓글이 현재 리뷰에 속하는지 확인
            if (parent.getReview() == null || !parent.getReview().getId().equals(reviewId)) {
                throw new RuntimeException("부모 댓글이 현재 리뷰에 속하지 않습니다");
            }

            // 댓글 및 대댓글 설정
            reviewComment.setParent(parent);
            parent.addReply(reviewComment);
        }

        // 댓글 존재 여부와 상관없이 리뷰 + 댓글 연결은 always
        reviewComment.setReview(review);

        // 댓글 저장
        reviewCommentRepository.save(reviewComment);

        // ReviewService의 메소드를 호출하여 댓글 구조를 조직화
        List<ReviewCommentResponse> organizedComments = organizeCommentStructure(reviewId);

        // 구조화된 댓글 목록을 포함하여 ReviewResponse 반환
        return ReviewResponse.from(review, organizedComments);
    }

    // 댓글, 대댓글 계층적 구조 생성
    @Transactional(readOnly = true)
    public List<ReviewCommentResponse> organizeCommentStructure(Long reviewId) {
        //모든 댓글 및 대댓글 리뷰 id를 기준으로 가져오기
        List<ReviewComment> allComments = reviewCommentRepository.findByReviewId(reviewId);

        // 대댓글이 없는 독립된 댓글
        Map<Long, ReviewCommentResponse> commentMap = new HashMap<>();

        // 대댓글이 없는 독립된 댓글
        List<ReviewCommentResponse> topLevelComments = new ArrayList<>();

        // 댓글을 DTO로 변환하면서 바로 구조화
        for (ReviewComment comment : allComments) {
            ReviewCommentResponse commentResponse = ReviewCommentResponse.from(comment);
            commentMap.put(comment.getId(), commentResponse);

            // 대댓글인 경우, 부모 댓글에 연결
            if (comment.getParent() != null) {
                ReviewCommentResponse parentCommentResponse = commentMap.get(comment.getParent().getId());
                if (parentCommentResponse != null) {
                    parentCommentResponse.getReplies().add(commentResponse);
                }
            } else {
                // 루트 댓글인 경우, topLevelComments에 추가
                topLevelComments.add(commentResponse);
            }
        }

        return topLevelComments;
    }


    // 댓글 수정
    public ReviewResponse updateComment(Long commentId, String providerName, ReviewCommentRequest request) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // 사용자 조회
        Member member = memberRepository.findByProvidername(providerName)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        // 댓글 업데이트 로직
        comment.setContent(request.getContent());
        comment.setUpdateTime(ZonedDateTime.now()); // 업데이트 시간 수동 설정
        reviewCommentRepository.save(comment);

        // 댓글이 속한 리뷰의 ID를 얻음
        Long reviewId = comment.getReview().getId();

        // 댓글 구조를 다시 조직화
        List<ReviewCommentResponse> organizedComments = organizeCommentStructure(reviewId);


        // 댓글 구조를 다시 조직화하여 리뷰 전체 상태를 반환
        return reviewService.searchReview(reviewId);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String providerName) {
         ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다"));

        // 사용자 조회
        Member member = memberRepository.findByProvidername(providerName)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));


        // 부모 댓글 삭제 시 대댓글도 함께 삭제
        if (comment.getParent() == null) {
            reviewCommentRepository.delete(comment); // 대댓글 포함 삭제
        } else {
            // 대댓글만 삭제, 부모 댓글은 남김
            comment.getParent().getReplies().remove(comment);
            reviewCommentRepository.delete(comment);
        }
    }




}
