package damon.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name="review_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewComment extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_comment_id")
    private Long id;
    private boolean isEdited = false; // 변경 여부를 추적하는 필드

    @Column(columnDefinition = "TEXT")
    private String content;

    //리뷰id 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    //부모댓글참조 (대댓글일 경우의 자기참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ReviewComment parent;

    //자식댓글목록 (대댓글)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> replies = new ArrayList<>();


    public void setReview(Review review){
        this.review = review;
        if (review != null ) {
            review.getReviewComments().add(this);
        }
    }

    //연관관계 매핑 메서드
    public void setParent(ReviewComment parent){
        this.parent = parent;
        if (parent != null) {
            parent.getReplies().add(this);
        }
    }

    public void addReply(ReviewComment reply){
        this.replies.add(reply);
        reply.setParent(this);
    }

    // 내용 변경 시, isEdited를 true로 설정
    public void updateContent(String newContent) {
        this.content = newContent;
        this.isEdited = true;
    }
}
