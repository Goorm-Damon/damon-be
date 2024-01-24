package damon.backend.dto.response;

import damon.backend.entity.Area;
import damon.backend.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ReviewListResponse {

    private Long id;
    private Area area;
    private ZonedDateTime createTime;

    private String firstImageUrl;

    private String title;

    private Long cost;
    private List<String> suggests; // 장소 추천
    private List<String> freeTags; // 자유 태그

    private long viewCount; // 조회수
    private long likeCount; // 좋아요 수
    private long commentCount; // 댓글 수

    //정적 메소드
    public static ReviewListResponse from(Review review) {

        String firstImageUrl = null;
        if (!review.getReviewImages().isEmpty()) {
            // 첫 번째 이미지의 URL을 가져옵니다.
            firstImageUrl = review.getReviewImages().get(0).getUrl();
        }

        long likeCount = review.getReviewLikes().size(); // 좋아요 수
        long viewCount = review.getViewCount(); // 조회수
        long commentCount = review.getReviewComments().size(); // 댓글수

        return new ReviewListResponse(

                review.getId(),
                review.getArea(),
                review.getCreateTime(),
                firstImageUrl,
                review.getTitle(),
                review.getCost(),
                review.getSuggests(),
                review.getFreeTags(),
                review.getViewCount(),
                likeCount,
                commentCount

        );
    }

}
