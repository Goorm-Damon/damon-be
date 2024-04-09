package damon.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import damon.backend.entity.ReviewComment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ReviewCommentResponse {

    private Long id;
    private String identifier;

    private String name;

    private String profileImage;
    private String state;
    private String createdDate;
    private Long reviewId;
    private Long parentId;
    private String content;

    @JsonInclude(JsonInclude.Include.NON_EMPTY) // 빈 리스트일 경우 JSON에서 제외
    private List<ReviewCommentResponse> replies; // 대댓글 목록

    // 날짜 포맷터 정의
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    // 정적 메소드
    public static ReviewCommentResponse from(ReviewComment reviewComment){
        // 대댓글 목록을 초기화하고, 재귀적으로 하위 댓글을 설정합니다.
        String state = reviewComment.isEdited() ? "편집됨" : ""; // isEdited 값에 따라 상태 설정

        // 대댓글 목록 초기화 조건 변경
        List<ReviewCommentResponse> replies = new ArrayList<>();

        // 부모 댓글이 없는 경우에만 대댓글 목록을 초기화합니다.
        if (reviewComment.getParent() == null && reviewComment.getReplies() != null) {
            replies = reviewComment.getReplies().stream()
                    .map(ReviewCommentResponse::from)
                    .collect(Collectors.toList());
        }

        return new ReviewCommentResponse(
                reviewComment.getId(),
                reviewComment.getUser() != null ? reviewComment.getUser().getIdentifier() : null, // 사용자 Identifier 추가
                reviewComment.getUser() != null ? reviewComment.getUser().getNickname() : null,
                reviewComment.getUser() != null ? reviewComment.getUser().getProfile() : null, // 프로필 이미지 URL 추가
                state,
                reviewComment.getCreatedDate().format(DATE_TIME_FORMATTER),
                reviewComment.getReview() != null ? reviewComment.getReview().getId() : null, // 리뷰 ID
                reviewComment.getParent() != null ? reviewComment.getParent().getId() : null, // 부모 댓글 ID
                reviewComment.getContent(),
                replies
        );
    }

}