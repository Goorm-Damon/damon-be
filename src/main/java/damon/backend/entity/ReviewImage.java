package damon.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "review_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")

    private Long id;
    private String url;

    //리뷰id 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;


    //연관관계 매핑 메서드
    public void setReview(Review review){
        this.review = review;
        if (review != null && !review.getReviewImages().contains(this)) {
            review.getReviewImages().add(this);
        }
    }

    // 기존 생성자를 유지하면서 Review 객체를 함께 설정할 수 있는 생성자 추가
    public ReviewImage(String url, Review review) {
        this.url = url;
        this.review = review;
    }

    // 이미지 생성
    public static ReviewImage createImage(String url,Review review) {
        ReviewImage image = new ReviewImage();
        image.url = url;
        image.setReview(review);
        return image;
    }
}