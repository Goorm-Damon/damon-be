package damon.backend.dto.response;

import damon.backend.entity.Area;
import damon.backend.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ReviewResponse {

    private Long id;

    private LocalDateTime createdDate;
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

    private String content;

    private List<ReviewCommentResponse> reviewComments; // 댓글 목록 추가


    //정적 팩토리 메서드
    public static ReviewResponse from(Review review,  List<ReviewCommentResponse> organizedComments) {

        long likeCount = review.getReviewLikes().size();
        long viewCount = review.getViewCount(); // 조회수

        String state = review.isEdited() ? "편집됨" : ""; // isEdited 값에 따라 상태 설정

        return new ReviewResponse(

                review.getId(),
                review.getCreatedDate(),
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
//                imageUrls,
                review.getContent(),
                organizedComments // 계층적으로 구조화된 댓글 목록
        );
    }
}



