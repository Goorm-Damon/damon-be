package damon.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
public class Member {

    @Id
    @Column(name = "member_id")
    private String id;

    private String nickname;

    private String profileImgUrl;

    //리뷰 매핑
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    //리뷰 좋아요 매핑
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> reviewLikes = new ArrayList<>();
}