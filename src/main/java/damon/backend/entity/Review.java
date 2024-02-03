package damon.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_id")
    private Long id;

    private long viewCount;
    private boolean isEdited = false; // 변경 여부를 추적하는 필드

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Area area;

    private Long cost;

    @ElementCollection
    private List<String> suggests = new ArrayList<>();

    @ElementCollection
    @Column
    private List<String> freeTags = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String content;

    //멤버id 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

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
    public void setMember(Member member){
        this.member = member;
        if (member != null ) {
            member.getReviews().add(this);
        }
    }
    // 내용 변경 시, isEdited를 true로 설정
    public void updateContent(String newContent) {
        this.content = newContent;
        this.isEdited = true;
    }
}
