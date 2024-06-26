package damon.backend.service;

import damon.backend.dto.request.ReviewCommentRequest;
import damon.backend.dto.response.ReviewCommentResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.Review;
import damon.backend.entity.ReviewComment;
import damon.backend.entity.user.User;
import damon.backend.exception.custom.CommentNotFoundException;
import damon.backend.exception.custom.ReviewNotFoundException;
import damon.backend.exception.custom.UnauthorizedException;
import damon.backend.exception.custom.UserNotFoundException;
import damon.backend.repository.ReviewCommentRepository;
import damon.backend.repository.ReviewRepository;
import damon.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommentService implements CommentStructureOrganizer {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final UserRepository userRepository;

    // 댓글 등록
    public ReviewResponse postComment(Long reviewId, ReviewCommentRequest request, String identifier) {
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(UserNotFoundException::new);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        ReviewComment parentComment = null; // 초기화 변경

        // 부모 댓글 처리
        if (request.getParentId() != null && request.getParentId() != 0) {
            parentComment = reviewCommentRepository.findById(request.getParentId())
                    .orElseThrow(CommentNotFoundException::new);
        }

        ReviewComment newComment = ReviewComment.createComment(review, user, request.getContent(), parentComment);

        reviewCommentRepository.save(newComment);

        // Review와 관련된 모든 댓글 및 대댓글 조직화 후 ReviewResponse 생성
        List<ReviewCommentResponse> organizedComments = organizeCommentStructure(reviewId);
        return ReviewResponse.from(review, organizedComments);
    }

    // 댓글 수정
    public ReviewResponse updateComment(Long commentId, ReviewCommentRequest request, String identifier) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        // 사용자 조회
        User user = userRepository.findByIdentifier(identifier).orElseThrow(UserNotFoundException::new);

        if (!comment.getReview().getUser().getIdentifier().equals(identifier)) {
            throw new UnauthorizedException();
        }

        // 댓글 업데이트 로직
        comment.updateContent(request.getContent());
        reviewCommentRepository.save(comment);

        // 댓글이 속한 리뷰의 ID를 얻음
        Long reviewId = comment.getReview().getId();

        // 여기서는 ReviewRepository를 사용하여 리뷰 정보를 직접 조회하고, 필요한 데이터를 조합하여 ReviewResponse를 생성합니다.
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        // 댓글 구조를 다시 조직화
        List<ReviewCommentResponse> organizedComments = organizeCommentStructure(reviewId);


        // 댓글 구조를 다시 조직화하여 리뷰 전체 상태를 반환
        return ReviewResponse.from(review, organizedComments);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String identifier) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        User user = userRepository.findByIdentifier(identifier).orElseThrow(UserNotFoundException::new);

        if (!comment.getReview().getUser().getIdentifier().equals(identifier)) {
            throw new UnauthorizedException();
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

        // 댓글 ID와 댓글 응답 객체의 매핑을 저장하는 맵 (해시맵 보완점)
        // 전역변수(static) - 동시성이슈 생긴다
        // but 지역변수에 선언 - 하나의 스레드만 생성
        // 계층 구조 조회할 때마다 생성하고 버리니까 (캐싱 보완)
        Map<Long, ReviewCommentResponse> commentMap = new HashMap<>();

        // 대댓글이 없는 독립된 댓글 (최상위 댓글)
        List<ReviewCommentResponse> topLevelComments = new ArrayList<>();

        // 모든 댓글을 변환하고 맵에 저장
        for (ReviewComment comment : allComments) {
            commentMap.put(comment.getId(), ReviewCommentResponse.from(comment));
        }

        // 대댓글을 부모의 replies 목록에 추가
        for (ReviewComment comment : allComments) {
            if (comment.getParent() != null) {
                ReviewCommentResponse child = commentMap.get(comment.getId());
                ReviewCommentResponse parent = commentMap.get(comment.getParent().getId());
                // 중복 추가 방지
                if (!parent.getReplies().contains(child)) {
                    parent.getReplies().add(child);
                }
            } else {
                topLevelComments.add(commentMap.get(comment.getId()));
            }
        }
        return topLevelComments;
    }
}