package damon.backend.dto.response;

import damon.backend.entity.Area;
import damon.backend.entity.Review;
import damon.backend.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ReviewListResponse {

    private Long id;
    private String name;
    private String state;
    private String createdDate;

    private long viewCount;
    private long likeCount;
    private long commentCount;

    private String title;
    private Area area;
    private Long cost;
    private List<String> suggests;
    private List<String> tags;
    private String mainImage;

    // 날짜 포맷터 정의
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //정적 메소드
    public static ReviewListResponse from(Review review) {

        long commentCount = review.getReviewComments().size(); // 댓글수

        String state = review.isEdited() ? "편집됨" : ""; // isEdited 값에 따라 상태 설정

        List<String> tagValues = review.getTags().stream()
                .map(Tag::getValue)
                .collect(Collectors.toList());

        String mainImage = !review.getReviewImages().isEmpty() ? review.getReviewImages().get(0).getUrl() : null;

        return new ReviewListResponse(

                review.getId(),
                review.getUser() != null ? review.getUser().getNickname() : null,
                state,
                review.getCreatedDate().format(DATE_TIME_FORMATTER),
                review.getViewCount(),
                review.getLikeCount(),
                commentCount,
                review.getTitle(),
                review.getArea(),
                review.getCost(),
                review.getSuggests(),
                tagValues,
                mainImage
        );
    }
}