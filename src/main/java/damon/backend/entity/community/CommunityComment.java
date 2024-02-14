package damon.backend.entity.community;

import damon.backend.entity.BaseEntity;
import damon.backend.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "community_comment")
public class CommunityComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_comment_id")
    private Long commentId;

//    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

//    @Setter
    @Column(name = "community_comment_content")
    private String content;

//    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_comment_parent_id")
    private CommunityComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<CommunityComment> childComments = new ArrayList<>();

    public CommunityComment(Member member, Community community, String content) {
        this.member = member;
        this.community = community;
        this.content = content;
    }

    public CommunityComment(Member member, Community community, String content, CommunityComment parentComment) {
        this.member = member;
        this.community = community;
        this.content = content;
        this.parentComment = parentComment;
    }

    public void setCommunityComment(String content) {
        this.content = content;
    }

    public CommunityComment addChildComment(CommunityComment childComment) {
        childComments.add(childComment);
        return childComment;
    }
}
