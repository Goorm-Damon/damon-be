package damon.backend.dto.response;

import damon.backend.entity.Area;
import damon.backend.entity.Review;
import damon.backend.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ReviewResponse {

    private Long id;

    private ZonedDateTime createTime;
    private String state;
    private long likeCount; // 좋아요 수
    private long viewCount; // 조회수

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Area area;
    private Long cost;
    private List<String> suggests;
    private List<String> freeTags;
    private List<String> imageUrls;

    private String content;

    private List<ReviewCommentResponse> reviewComments; // 댓글 목록 추가


    //정적 팩토리 메서드
    public static ReviewResponse from(Review review,  List<ReviewCommentResponse> organizedComments) {

        long likeCount = review.getReviewLikes().size();
        long viewCount = review.getViewCount(); // 조회수

        String state = ""; // 초기 상태값은 빈 문자열로 설정

        // updateTime이 null이 아니고, createTime과 updateTime이 다를 때 state를 "편집됨"으로 설정
        if (review.getUpdateTime() != null && !review.getCreateTime().isEqual(review.getUpdateTime())) {
            state = "편집됨";
        }

        // 이미지 리스트 가져오기
        List<String> imageUrls = review.getReviewImages().stream()
                .map(ReviewImage::getUrl)
                .collect(Collectors.toList());

        return new ReviewResponse(

                review.getId(),
                review.getCreateTime(),
                state,
                likeCount,
                review.getViewCount(),
                review.getTitle(),
                review.getStartDate(),
                review.getEndDate(),
                review.getArea(),
                review.getCost(),
                review.getSuggests(),
                review.getFreeTags(),
                imageUrls,
                review.getContent(),
                organizedComments // 계층적으로 구조화된 댓글 목록
        );
    }
}



