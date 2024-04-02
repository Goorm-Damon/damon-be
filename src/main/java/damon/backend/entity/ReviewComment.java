package damon.backend.entity;

import damon.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name="review_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    //멤버id 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    //연관관계 매핑 메서드

    public void setReview(Review review) {
        this.review = review;
        if (review != null) {
            review.getReviewComments().add(this);
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            user.getReviewComments().add(this);
        }
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setParent(ReviewComment parent) {
        this.parent = parent;
        // 대대댓글 생성 금지 로직
        if (parent != null && parent.getParent() != null) {
            throw new IllegalStateException("Creating a reply to a reply (nested beyond two levels) is not allowed");
        }
        if (parent != null) {
            parent.getReplies().add(this);
        }
    }

    public void addReply(ReviewComment reply) {
        this.replies.add(reply);
        reply.setParent(this);
    }

    // 댓글 생성
    public static ReviewComment createComment(Review review, User user, String content, ReviewComment parent) {
        ReviewComment comment = new ReviewComment();
        comment.setReview(review);
        comment.setUser(user);
        comment.setContent(content);
        comment.setParent(parent);
        return comment;
    }

    // 댓글 수정
    public void updateContent(String content) {
        this.content = content;
        this.isEdited = true;
    }
}