package damon.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    private String providerName; // provider + providerId 로 이루어진 커스텀 필드
    private String name; // 사용자 이름 (카카오= 닉네임 / 네이버= 이름)
    private String email; // 사용자 이메일
    private String profileImgUrl; // 사용자 프로필 이미지
    private String refreshToken;


    //리뷰 매핑
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    //리뷰 댓글 매핑
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewComment> reviewComments = new HashSet<>();

    //리뷰 좋아요 매핑
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> reviewLikes = new ArrayList<>();

    // 회원 정보 신규등록
    public void createInfo(String providerName, String name, String email, String profileImgUrl) {
        this.providerName = providerName;
        this.name = name;
        this.email = email;
        this.profileImgUrl = profileImgUrl;
    }

    // 회원 정보 업데이트
    public void updateInfo(String name, String profileImgUrl) {
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }
}


