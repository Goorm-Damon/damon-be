package damon.backend.dto.response.community;

import damon.backend.entity.community.Community;
import damon.backend.enums.CommunityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CommunityDetailDTO {

    private Long communityId;
    private Long userId; // 작성자 본인 여부 판단
    private String memberName;
    private String memberImage;
    private LocalDateTime createdDate;
    private CommunityType type;
    private String title;
    private String content;
    private int views;
    private List<String> images;
    private List<CommunityLikeDTO> likes;
    private List<CommunityCommentDTO> comments;

    public CommunityDetailDTO(Community community) {

        this.communityId = community.getCommunityId();
        this.userId = community.getUser().getId();
        this.memberName = community.getUser().getNickname();
        this.memberImage = community.getUser().getProfile();
        this.createdDate = community.getCreatedDate();
        this.type = community.getType();
        this.title = community.getTitle();
        this.content = community.getContent();
        this.views = community.getViews();
        this.images = community.getImages();

        this.likes = community.getLikes()
                .stream()
                .map(CommunityLikeDTO::new)
                .collect(Collectors.toList());

        this.comments = community.getComments()
                .stream()
                .map(CommunityCommentDTO::new)
                .sorted(Comparator.comparing(CommunityCommentDTO::getCreatedDate).reversed())
                .collect(Collectors.toList());
    }
}
