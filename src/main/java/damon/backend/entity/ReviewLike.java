package damon.backend.entity;

import damon.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ReviewLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_like_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    //연관관계 매핑 메서드
    public void setReview(Review review) {
        this.review = review;
        review.getReviewLikes().add(this);
    }

    public void setUser(User user){
        this.user = user;
        if (user != null ) {
            user.getReviewLikes().add(this);
        }
    }

    // 좋아요
    public static ReviewLike createLike(Review review, User user) {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setReview(review);
        reviewLike.setUser(user);
        review.increaseLikeCount();
//        review.getReviewLikes().add(reviewLike);
  //      user.getReviewLikes().add(reviewLike);
        return reviewLike;
    }

}