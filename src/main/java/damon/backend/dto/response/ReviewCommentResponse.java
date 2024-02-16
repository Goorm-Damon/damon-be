package damon.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import damon.backend.entity.ReviewComment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ReviewCommentResponse {

    private Long id;
    private String name;
    private String state;
    private LocalDateTime createdDate;
    private Long reviewId; // 대댓글일 경우에는 부모 댓글의 Id
    private Long parentId;
    private String content;

    @JsonInclude(JsonInclude.Include.NON_EMPTY) // 빈 리스트일 경우 JSON에서 제외
    private List<ReviewCommentResponse> replies; // 대댓글 목록


    // 정적 메소드
    public static ReviewCommentResponse from(ReviewComment reviewComment){
        // 대댓글 목록을 초기화하고, 재귀적으로 하위 댓글을 설정합니다.
        String state = reviewComment.isEdited() ? "편집됨" : ""; // isEdited 값에 따라 상태 설정

        // 대댓글 목록 초기화 조건 변경
        List<ReviewCommentResponse> replies = new ArrayList<>();
        if (reviewComment.getParent() == null) { // 부모 댓글인 경우에만 대댓글 목록 초기화
            replies = reviewComment.getReplies() != null ?
                    reviewComment.getReplies().stream()
                            .map(ReviewCommentResponse::from)
                            .collect(Collectors.toList()) : new ArrayList<>();
        }

        return new ReviewCommentResponse(
                reviewComment.getId(),
                reviewComment.getMember() != null ? reviewComment.getMember().getName() : null,
                state,
                reviewComment.getCreatedDate(),
                reviewComment.getReview() != null ? reviewComment.getReview().getId() : null, // 리뷰 ID
                reviewComment.getParent() != null ? reviewComment.getParent().getId() : null, // 부모 댓글 ID
                reviewComment.getContent(),
                replies
        );
    }

}