package damon.backend.dto.request.community;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommunityChildCommentCreateForm {

    @NotNull
    private Long parentId;

    @NotEmpty(message = "내용을 입력해주세요.")
    private String content;
}
