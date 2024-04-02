package damon.backend.dto.request;

import lombok.Data;

@Data
public class ReviewCommentRequest {

    private Long parentId; // 대댓글의 경우 상위 댓글 ID, null 가능
    private String content;

}