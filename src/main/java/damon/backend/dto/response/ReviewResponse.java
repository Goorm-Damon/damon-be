package damon.backend.dto.response;

import damon.backend.entity.Area;
import damon.backend.entity.Review;
import damon.backend.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private String name;
    private String state;
    private String createdDate;

    private long viewCount; // 조회수
    private long likeCount; // 좋아요 수

    private String title;
    private Area area;
    private String startDate;
    private String endDate;

    private Long cost;
    private List<String> suggests;
    private List<String> freeTags;

    private List<String> imageUrls; // 이미지 URL 리스트 추가
    private String content;

    private List<ReviewCommentResponse> reviewComments; // 댓글 목록 추가

    private String url;

    // 날짜 포맷터 정의
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 정적 팩토리 메서드
    public static ReviewResponse from(Review review, List<ReviewCommentResponse> organizedComments) {
        long viewCount = review.getViewCount(); // 조회수
        long likeCount = review.getReviewLikes().size();

        String state = review.isEdited() ? "편집됨" : ""; // isEdited 값에 따라 상태 설정

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getUrl)
                .collect(Collectors.toList());

        return new ReviewResponse(
                review.getId(),
                review.getUser() != null ? review.getUser().getNickname() : null,
                state,
                review.getCreatedDate().format(DATE_TIME_FORMATTER),    // LocalDateTime -> String
                viewCount,
                likeCount,
                review.getTitle(),
                review.getArea(),
                review.getStartDate().format(DATE_FORMATTER),    // LocalDate -> String
                review.getEndDate().format(DATE_FORMATTER),     // LocalDate -> String
                review.getCost(),
                review.getSuggests(),
                review.getFreeTags(),
                imageUrls, // 이미지 URL 리스트
                review.getContent(),
                organizedComments, // 계층적으로 구조화된 댓글 목록
                ""
        );
    }

    // 추가
    public ReviewResponse(Review review) {
        long viewCount = review.getViewCount(); // 조회수
        long likeCount = review.getReviewLikes().size();

        String state = review.isEdited() ? "편집됨" : ""; // isEdited 값에 따라 상태 설정

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getUrl)
                .collect(Collectors.toList());

        this.id = review.getId();
        this.name = review.getUser() != null ? review.getUser().getNickname() : null;
        this.state = state;
        this.createdDate = review.getCreatedDate().format(DATE_TIME_FORMATTER); // LocalDateTime -> String
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.title = review.getTitle();
        this.area = review.getArea();
        this.startDate = review.getStartDate().format(DATE_FORMATTER); // LocalDate -> String
        this.endDate = review.getEndDate().format(DATE_FORMATTER); // LocalDate -> String
        this.cost = review.getCost();
        this.suggests = review.getSuggests();
        this.freeTags = review.getFreeTags();
        this.imageUrls = imageUrls; // 이미지 URL 리스트
        this.content = review.getContent();
        this.reviewComments = new ArrayList<>(); // 계층적으로 구조화된 댓글 목록
        this.url = review.getReviewImages().get(0).getUrl();
    }
}
