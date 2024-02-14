package damon.backend.dto.request.community;

import damon.backend.enums.CommunityType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CommunityCreateForm {

    @NotEmpty(message = "제목을 입력해주세요.")
    private String title;

    @NotEmpty(message = "내용을 입력해주세요.")
    private String content;

    @NotEmpty(message = "타입을 선택해주세요.")
    private CommunityType type;

    private List<String> images;
}