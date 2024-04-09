package damon.backend.service;

import damon.backend.dto.request.ReviewRequest;
import damon.backend.dto.response.ReviewListResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.*;
import damon.backend.entity.user.User;
import damon.backend.repository.*;
import damon.backend.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @MockBean
    private AwsS3Service awsS3Service;

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private ReviewService reviewService;

    private User user1;
    private User user2;

    private Review review1;
    private Review review2;
    private Review review3;

    @Autowired
    private EntityManager entityManager;

    private ReviewImage image1;
    private ReviewImage image2;

    private ReviewImage image3;
    private ReviewImage image4;

    private ReviewImage image5;
    private ReviewImage image6;

    private ReviewLike like1;
    private ReviewLike like2;



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

        image1 = reviewImageRepository.save(new ReviewImage("https://goorm-damon-s3.s3.ap-northeast-2.amazonaws.com/review/1-1.png", review1));
        image2 = reviewImageRepository.save(new ReviewImage("https://goorm-damon-s3.s3.ap-northeast-2.amazonaws.com/review/1-2.png", review1));
        image3 = reviewImageRepository.save(new ReviewImage("https://goorm-damon-s3.s3.ap-northeast-2.amazonaws.com/review/2-1.png", review2));
        image4 = reviewImageRepository.save(new ReviewImage("https://goorm-damon-s3.s3.ap-northeast-2.amazonaws.com/review/2-2.png", review2));

        like1 = reviewLikeRepository.save(ReviewLike.createLike(review1, user1));
        like2 = reviewLikeRepository.save(ReviewLike.createLike(review2, user2));

        doNothing().when(awsS3Service).deleteImageByUrl(anyString());
    }


    @Test
    @DisplayName("리뷰 등록")
    void postReview() {
        //given
        ReviewRequest request = new ReviewRequest(
                "리뷰제목3",
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 3),
                Area.DAEJEON,
                300000L,
                Arrays.asList("추천장소3"),
                Arrays.asList("태그3"),
                "리뷰내용3",
                Arrays.asList("https://goorm-damon-s3.s3.ap-northeast-2.amazonaws.com/review/3.png"),
                Arrays.asList(""),
                Arrays.asList("")
        );

        // when
        ReviewResponse postResponse = reviewService.postReview(request, "2");

        // then
        assertAll(
                () -> assertEquals(postResponse.getImageUrls(), request.getImageUrls(), "대표로 등록된 리뷰 이미지 검증"),
                () -> assertEquals("", postResponse.getState(), "'편집됨' 상태 초기화 검증"),
                () -> assertEquals(0, postResponse.getLikeCount(), "좋아요수 초기화 검증"),
                () -> assertEquals(0, postResponse.getViewCount(), "조회수 상태 초기화 검증")
        );
    }

    @Test
    @DisplayName("리뷰 수정")
    void updateReview() {
        // given
        ReviewRequest request = new ReviewRequest();
        request.setStartDate(LocalDate.of(2024, 5, 1));
        request.setEndDate(LocalDate.of(2024, 5, 3));
        request.setNewImageUrls(Arrays.asList("https://goorm-damon-s3.s3.ap-northeast-2.amazonaws.com/review/1-3.png"));
        request.setDeleteImageUrls(Arrays.asList("https://goorm-damon-s3.s3.ap-northeast-2.amazonaws.com/review/1-1.png"));

        // when
        ReviewResponse updatedResponse = reviewService.updateReview(review1.getId(), request,"1");

        // then
        assertAll(
                () -> assertTrue(updatedResponse.getImageUrls().containsAll(request.getNewImageUrls()), "새 이미지 추가 검증"),
                () -> assertFalse(updatedResponse.getImageUrls().contains(request.getDeleteImageUrls()), "기존 이미지 삭제 검증"),
                () -> assertEquals(2, updatedResponse.getImageUrls().size(), "이미지 수정 검증"),
                () -> assertEquals("편집됨", updatedResponse.getState(), "'편집됨' 상태 검증")
        );
    }

    @Test
    @DisplayName("리뷰 상세 조회")
    void searchReviewDetail() {
        // given
        long initialViewCount = review1.getViewCount();

        // when
        ReviewResponse reviewDetail = reviewService.searchReviewDetail(review1.getId(), true);

        // then
        Review updatedReview = reviewRepository.findById(review1.getId()).get();

        assertAll(
                () -> assertEquals(initialViewCount + 1, updatedReview.getViewCount(), "조회 후 조회수 + 1 증가 검증"),
                () -> assertEquals(updatedReview.getViewCount(), reviewDetail.getViewCount(), "조회 후 조회수와 데이터베이스의 조회수 일치 검증")
        );
    }

    @Test
    @DisplayName("리뷰 전체 및 지역별 조회")
    void searchReviewList() {
        // when
        Page<ReviewListResponse> reviewsSeoul = reviewService.searchReviewList(0, 10, Area.SEOUL);
        Page<ReviewListResponse> reviewsAll = reviewService.searchReviewList(0, 10, null);

        // then
        assertAll(
                () -> assertEquals(1, reviewsSeoul.getTotalElements(), "서울 지역 리뷰 검증"),
                () -> assertTrue(reviewsSeoul.getContent().stream().allMatch(r -> r.getArea() == Area.SEOUL), "'서울' 지역 검증"),
                () -> assertEquals(2, reviewsAll.getTotalElements(), "전체 리뷰 검증")
        );
    }

    @Test
    @DisplayName("리뷰 검색")
    void searchReviews() {
        // when
        Page<ReviewListResponse> reviewsUser1 = reviewService.searchReviews("nickname", "사용자1", 0, 10);
        Page<ReviewListResponse> reviewsUser2 = reviewService.searchReviews("title", "리뷰제목1",0, 10);
        Page<ReviewListResponse> reviewsUser3 = reviewService.searchReviews("tag", "태그1",0, 10);

        // then
        assertAll(

                () -> assertEquals(1, reviewsUser1.getTotalElements(), "nickname 검색 검증"),
                () -> assertEquals(1, reviewsUser2.getTotalElements(), "title 검색 검증"),
                () -> assertEquals(1, reviewsUser3.getTotalElements(), "tag 검색 검증"),

                () -> assertTrue(reviewsUser1.getContent().stream().allMatch(r -> r.getName().equals("사용자1")), "'사용자1' 닉네임 검증"),
                () -> assertTrue(reviewsUser2.getContent().stream().allMatch(r -> r.getTitle().equals("리뷰제목1")), "'리뷰제목1' 제목 검증"),
                () -> assertTrue(reviewsUser3.getContent().stream().anyMatch(r -> r.getTags().contains("태그1")), "'태그1' 태그 검증")
        );
    }

    @Test
    @DisplayName("리뷰 좋아요 누르기")
    void toggleLike() {
        // when
        reviewService.toggleLike(review1.getId(), "1");
        reviewService.toggleLike(review2.getId(), "1");

        Review review1Like = reviewRepository.findById(review1.getId()).get();
        Review review2Like = reviewRepository.findById(review2.getId()).get();

        // then
        assertAll(
                () -> assertEquals(0, review1Like.getLikeCount(),
                        "review1의 좋아요 수 감소 검증"),

                () -> assertEquals(2, review2Like.getLikeCount(),
                        "review2의 좋아요 수 증가 검증")
        );
    }

    @Test
    @DisplayName("내가 좋아요 누른 리뷰 조회")
    void searchLikedReviews() {
        // when
        Page<ReviewListResponse> reviewsLike = reviewService.searchLikedReviews(user1.getIdentifier(), 0, 10);

        // then
        assertAll(
                () -> assertEquals(1, reviewsLike.getTotalElements(), "Identifier '1' 사용자가 좋아요 누른 리뷰 수 검증"),
                () -> assertTrue(reviewsLike.getContent().stream().anyMatch(r -> r.getId().equals(review1.getId())), "Identifier '1' 사용자가 누른 리뷰가 조회 결과에 포함되는지 검증")
        );
    }

    @Test
    @DisplayName("메인 페이지 베스트 리뷰 조회")
    void findTopReviewsForMainPage() {
        //given
        like2 = reviewLikeRepository.save(ReviewLike.createLike(review2, user1));

        // when
        List<ReviewListResponse> bestReviews = reviewService.findTopReviewsForMainPage();

        System.out.println(bestReviews);
        // then
        assertEquals(bestReviews.get(0).getId(), review2.getId());
        assertEquals(bestReviews.get(1).getId(), review1.getId());
    }

    @Test
    @DisplayName("내 리뷰 조회")
    void searchMyReviews() {
        // when
        Page<ReviewListResponse> reviewsUser1 = reviewService.searchMyReviews("1", 0, 10);
        Page<ReviewListResponse> reviewsUser2 = reviewService.searchMyReviews(user2.getIdentifier(), 0, 10);

        // then
        assertAll(
                () -> assertEquals(1, reviewsUser1.getTotalElements(), "내 리뷰 검증"),
                () -> assertTrue(reviewsUser1.getContent().stream().allMatch(r -> r.getIdentifier().equals("1")), "'1' Identifier 검증")
        );
    }
}
