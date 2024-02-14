package damon.backend.entity.community;

import damon.backend.entity.BaseEntity;
import damon.backend.entity.Member;
import damon.backend.enums.CommunityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "community")
public class Community extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_id")
    private Long communityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 작성자

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CommunityType type; // [번개, 자유]

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "views")
    private int views;

    @ElementCollection
    @CollectionTable(name = "community_images", joinColumns = @JoinColumn(name = "community_id"))
    @Column(name = "community_image_path")
    private List<String> images = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommunityLike> likes = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityComment> comments = new ArrayList<>();

    public Community(Member member, CommunityType type, String title, String content) {
        this.member = member;
        this.type = type;
        this.title = title;
        this.content = content;
        this.views = 0;
    }

    public Community(Member member, CommunityType type, String title, String content, List<String> images) {
        this.member = member;
        this.type = type;
        this.title = title;
        this.content = content;
        this.views = 0;
        this.images = images;
    }

    public void setCommunity(String title, String content) {
        this.title = title;
        this.content = content;
        this.lastModifiedDate = LocalDateTime.now(); // 마지막 수정 시간
    }

    public void setCommunity(String title, String content, List<String> images) {
        setCommunity(title, content);
        this.images = images;
    }

    public CommunityComment addComment(Member member, String content) {
        CommunityComment comment = new CommunityComment(member, this, content);
        comments.add(comment);
        return comment;
    }

    public boolean isLike(Member member) {
        return likes.stream().anyMatch(like -> like.getMember().getId().equals(member.getId()));
    }

    public void addLike(Member member) {
        CommunityLike like = new CommunityLike(this, member);
        likes.add(like);
    }

    public void removeLike(Member member) {
        CommunityLike findLike = likes.stream().filter(like -> like.getMember().getId().equals(member.getId())).findFirst().orElse(null);

        if (findLike != null) {
            likes.remove(findLike);
        }
    }
}