package damon.backend.entity.community;

import damon.backend.entity.BaseEntity;
import damon.backend.entity.user.User;
import damon.backend.enums.CommunityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 커뮤니티 정보를 나타내는 엔티티 클래스입니다.
 */
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
    @JoinColumn(name = "user_id")
    private User user; // 작성자

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CommunityType type; // [번개, 자유]

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "views")
    private int views;

    @Setter
    @ElementCollection
    @CollectionTable(name = "community_images", joinColumns = @JoinColumn(name = "community_id"))
    @Column(name = "community_image_path")
    private List<String> images = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommunityLike> likes = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityComment> comments = new ArrayList<>();

    public Community(User user, CommunityType type, String title, String content) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.views = 0;
    }

    public Community(User user, CommunityType type, String title, String content, List<String> images) {
        this.user = user;
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

    public CommunityComment addComment(User user, String content) {
        CommunityComment comment = new CommunityComment(user, this, content);
        comments.add(comment);
        return comment;
    }

    public boolean isLike(User user) {
        return likes.stream().anyMatch(like -> like.getUser().getId().equals(user.getId()));
    }

    public void addLike(User user) {
        CommunityLike like = new CommunityLike(this, user);
        likes.add(like);
    }

    public void removeLike(User user) {
        CommunityLike findLike = likes.stream().filter(like -> like.getUser().getId().equals(user.getId())).findFirst().orElse(null);

        if (findLike != null) {
            likes.remove(findLike);
        }
    }
}