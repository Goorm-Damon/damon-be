package damon.backend.dto.response;

import damon.backend.entity.Area;
import damon.backend.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ReviewListResponse {

    private Long id;
    private String name;
    private String state;
    private LocalDateTime createdDate;

    private long viewCount; // 조회수
    private long likeCount; // 좋아요 수
    private long commentCount; // 댓글 수

    private String title;
    private Area area;

    private Long cost;
    private List<String> suggests; // 장소 추천
    private List<String> freeTags; // 자유 태그


    //정적 메소드
    public static ReviewListResponse from(Review review) {

        long viewCount = review.getViewCount(); // 조회수
        long likeCount = review.getReviewLikes().size(); // 좋아요 수
        long commentCount = review.getReviewComments().size(); // 댓글수

        String state = review.isEdited() ? "편집됨" : ""; // isEdited 값에 따라 상태 설정

        return new ReviewListResponse(

                review.getId(),
                review.getMember() != null ? review.getMember().getName() : null,
                state,
                review.getCreatedDate(),
                review.getViewCount(),
                likeCount,
                commentCount,
                review.getTitle(),
                review.getArea(),
                review.getCost(),
                review.getSuggests(),
                review.getFreeTags()
//              firstImageUrl,

        );
    }

}
