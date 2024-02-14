package damon.backend.entity.community;

import damon.backend.entity.BaseEntity;
import damon.backend.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_like_id")
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public CommunityLike(Community community, Member member) {
        this.community = community;
        this.member = member;
    }
}