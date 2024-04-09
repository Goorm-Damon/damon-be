package damon.backend.service;

import damon.backend.dto.request.ReviewCommentRequest;
import damon.backend.dto.response.ReviewCommentResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.*;
import damon.backend.entity.user.User;
import damon.backend.repository.*;
import damon.backend.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class ReviewCommentServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewCommentRepository reviewCommentRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewCommentService reviewCommentService;

    private User user1;
    private User user2;

    private Review review1;
    private Review review2;

    private ReviewComment reviewComment1;
    private ReviewComment reviewComment2;

    @BeforeEach
    void beforeEach() {

        user1 = userRepository.save(new User("1", "사용자1", "ex1@naver.com", "http://ex1.png"));
        user2 = userRepository.save(new User("2", "사용자2", "ex2@naver.com", "http://ex2.png"));

        review1 = reviewRepository.save(Review.create(
                "리뷰제목1",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3),
                Area.SEOUL, 100000L, Arrays.asList("추천장소1"), "리뷰내용1", Arrays.asList("태그1"), user1));
        review2 = reviewRepository.save(Review.create(
                "리뷰제목2",
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 3),
                Area.INCHEON, 200000L, Arrays.asList("추천장소2"), "리뷰내용2", Arrays.asList("태그2"), user2));

        reviewComment1 = reviewCommentRepository.save(ReviewComment.createComment(review1, user1, "부모댓글1", null));
        reviewComment2 = reviewCommentRepository.save(ReviewComment.createComment(review1, user1, "자식댓글1", reviewComment1));
    }

    @Test
    @DisplayName("리뷰 댓글 등록")
    void postComment() {
        //given
        ReviewCommentRequest request1 = new ReviewCommentRequest(
                null,
                "댓글1"
        );

        // when
        reviewCommentService.postComment(review2.getId(), request1, "1");
        ReviewResponse response = reviewService.searchReviewDetail(review2.getId(), false);

        // then
        assertAll(
                () -> assertEquals(1, response.getReviewComments().size(), "댓글1 등록 검증"),
                () -> assertEquals("", response.getReviewComments().get(0).getState(), "'편집됨' 상태 초기화 검증")
       );
    }

    @Test
    @DisplayName("리뷰 댓글 수정")
    void updateComment() {
        // given
        ReviewCommentRequest updateRequest = new ReviewCommentRequest(
                null,
                "부모댓글1 수정"
        );

        // when
        reviewCommentService.updateComment(reviewComment1.getId(), updateRequest, "1");

        ReviewResponse updatedResponse = reviewService.searchReviewDetail(review1.getId(), false);

        // then
        assertAll(
                () -> assertEquals(updatedResponse.getReviewComments().get(0).getContent(),updateRequest.getContent(), "댓글 수정 검증"),
                () -> assertEquals("편집됨", updatedResponse.getReviewComments().get(0).getState(), "'편집됨' 상태 검증")
        );
    }

    @Test
    @DisplayName("리뷰 댓글 계층적 구조")
    void searchReviewDetail() {
        // when
        List<ReviewCommentResponse> organizedComments = reviewCommentService.organizeCommentStructure(review1.getId());

        // then
        assertAll(
                () -> assertEquals(1, organizedComments.size(), "최상위 댓글 수 검증"),
                () -> assertEquals("부모댓글1", organizedComments.get(0).getContent(), "부모 댓글 내용 검증"),
                () -> assertEquals(1, organizedComments.get(0).getReplies().size(), "대댓글 수 검증"),
                () -> assertEquals("자식댓글1", organizedComments.get(0).getReplies().get(0).getContent(), "자식 댓글 내용 검증")
        );
    }
}
