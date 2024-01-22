package damon.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "review")
public class Review {
    //not null 이 너무 많아서 기본값을 not null로 설정
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NotNull {
        boolean nullable() default false;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_id")
    private Long id;
    private ZonedDateTime createTime;
    private ZonedDateTime updateTime;
    private int view;

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Area area;

    private Long cost;
    private String suggest;

    @Column(nullable = true)
    private String freeTag;

    @Column(columnDefinition = "TEXT")
    private String content;

    //멤버id 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User user;

    //리뷰이미지 매핑
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImages = new ArrayList<>();

    //리뷰좋아요 매핑
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> reviewLikes = new ArrayList<>();

    //리뷰댓글 매핑
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> reviewComments = new ArrayList<>();

    //연관관계 매핑 메서드
    public void setMember(User user){
        this.user = user;
        if (user != null) {
            user.getReviews().add(this);
        }
    }

}
