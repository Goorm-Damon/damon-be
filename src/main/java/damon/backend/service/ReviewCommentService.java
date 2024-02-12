package damon.backend.service;

import damon.backend.dto.request.ReviewCommentRequest;
import damon.backend.dto.response.ReviewCommentResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.Member;
import damon.backend.entity.Review;
import damon.backend.entity.ReviewComment;
import damon.backend.exception.ReviewException;
import damon.backend.repository.MemberRepository;
import damon.backend.repository.ReviewCommentRepository;
import damon.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommentService implements CommentStructureOrganizer {
    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final MemberRepository memberRepository;

    // 댓글 등록
    public ReviewResponse postComment(Long reviewId, ReviewCommentRequest request, String providerName) {
        // 사용자 조회
        Member member = memberRepository.findByProviderName(providerName)
                .orElseThrow(ReviewException::memberNotFound);

        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewException::reviewNotFound);

        ReviewComment parentComment = null; // 초기화 변경

        // 부모 댓글 처리
        if (request.getParentId() != null) {
            parentComment = reviewCommentRepository.findById(request.getParentId())
                    .orElseThrow(ReviewException::commentNotFound);

        }

        ReviewComment newComment = ReviewComment.createContent(request.getContent(), review, member, parentComment);

        reviewCommentRepository.save(newComment);

        // Review와 관련된 모든 댓글 및 대댓글 조직화 후 ReviewResponse 생성
        List<ReviewCommentResponse> organizedComments = organizeCommentStructure(reviewId);

        // 댓글 구조를 다시 조직화하여 리뷰 전체 상태를 반환
        return ReviewResponse.from(review, organizedComments);
    }

    // 댓글 수정
    public ReviewResponse updateComment(Long commentId, ReviewCommentRequest request, String providerName) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(ReviewException::commentNotFound);

        // 사용자 조회
        Member member = memberRepository.findByProviderName(providerName)
                .orElseThrow(ReviewException::memberNotFound);

        if (!comment.getReview().getMember().getProviderName().equals(providerName)) {
            throw ReviewException.unauthorized();
        }

        // 댓글 업데이트 로직
        comment.updateContent(request.getContent());
        reviewCommentRepository.save(comment);

        // 댓글이 속한 리뷰의 ID를 얻음
        Long reviewId = comment.getReview().getId();

        // 여기서는 ReviewRepository를 사용하여 리뷰 정보를 직접 조회하고, 필요한 데이터를 조합하여 ReviewResponse를 생성합니다.
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewException::reviewNotFound);

        // 댓글 구조를 다시 조직화
        List<ReviewCommentResponse> organizedComments = organizeCommentStructure(reviewId);


        // 댓글 구조를 다시 조직화하여 리뷰 전체 상태를 반환
        return ReviewResponse.from(review, organizedComments);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String providerName) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(ReviewException::commentNotFound);

        // 사용자 조회
        Member member = memberRepository.findByProviderName(providerName)
                .orElseThrow(ReviewException::memberNotFound);

        if (!comment.getReview().getMember().getProviderName().equals(providerName)) {
            throw ReviewException.unauthorized();
        }

        // 부모 댓글 삭제 시 대댓글도 함께 삭제
        if (comment.getParent() == null) {
            reviewCommentRepository.delete(comment); // 대댓글 포함 삭제
        } else {
            // 대댓글만 삭제, 부모 댓글은 남김
            comment.getParent().getReplies().remove(comment);
            reviewCommentRepository.delete(comment);
        }
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
                    boolean isDuplicate = parentCommentResponse.getReplies().stream()
                            .anyMatch(reply -> reply.getId().equals(comment.getId()));
                    if (!isDuplicate) {
                        parentCommentResponse.getReplies().add(commentMap.get(comment.getId()));
                    }
                }
            } else {
                // 루트 댓글인 경우, topLevelComments에 추가
                topLevelComments.add(commentMap.get(comment.getId()));
            }
        }

        return topLevelComments;
    }


}