package damon.backend.dto.response;

import damon.backend.entity.Area;
import damon.backend.entity.Review;
import damon.backend.entity.ReviewImage;
import damon.backend.entity.Tag;
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
    private String identifier;
    private String name;
    private String profileImage;
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
    private List<String> tags;
    private List<String> imageUrls;
    private String content;

    private List<ReviewCommentResponse> reviewComments;

    // 날짜 포맷터 정의
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 정적 팩토리 메서드
    public static ReviewResponse from(Review review, List<ReviewCommentResponse> organizedComments) {
        String state = review.isEdited() ? "편집됨" : ""; // isEdited 값에 따라 상태 설정

        List<String> tags = review.getTags().stream()
                .map(tag -> tag.getValue())
                .collect(Collectors.toList());

        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getUrl)
                .collect(Collectors.toList());

        return new ReviewResponse(
                review.getId(),
                review.getUser() != null ? review.getUser().getIdentifier() : null, // 사용자 Identifier 추가
                review.getUser() != null ? review.getUser().getNickname() : null,
                review.getUser() != null ? review.getUser().getProfile() : null, // 프로필 이미지 URL 추가
                state,
                review.getCreatedDate().format(DATE_TIME_FORMATTER),    // LocalDateTime -> String
                review.getViewCount(),
                review.getLikeCount(),
                review.getTitle(),
                review.getArea(),
                review.getStartDate().format(DATE_FORMATTER),    // LocalDate -> String
                review.getEndDate().format(DATE_FORMATTER),     // LocalDate -> String
                review.getCost(),
                review.getSuggests(),
                tags,
                imageUrls,
                review.getContent(),
                organizedComments// 계층적으로 구조화된 댓글 목록
        );
    }
}