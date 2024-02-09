package damon.backend.entity;

import damon.backend.dto.request.ReviewRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Review extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_id")
    private Long id;

    private long viewCount;
    private Long likeCount;
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
    private Set<ReviewLike> reviewLikes = new HashSet<>();

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


    // 조회수 증가 메소드
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 생성자 메서드
    public static Review create(ReviewRequest request, Member member) {
        Review review = new Review();
        populateReviewFields(review, request);
        review.member = member; // Member 설정은 create 시에만 수행
        return review;
    }

    // 업데이트 메서드
    public void update(ReviewRequest request) {
        populateReviewFields(this, request);
    }

    // 공통 필드 설정 메서드
    private static void populateReviewFields(Review review, ReviewRequest request) {
        review.title = request.getTitle();
        review.startDate = request.getStartDate();
        review.endDate = request.getEndDate();
        review.area = request.getArea();
        review.cost = request.getCost();
        review.suggests = request.getSuggests();
        review.freeTags = request.getFreeTags();
        review.content = request.getContent();
        // updateTime은 @LastModifiedDate 어노테이션을 사용하여 자동 업데이트 되도록 설정할 수 있습니다.
    }

    // 기존의 addLike 메서드를 대체하는 toggleLike 메서드
    public void toggleLike(Member member) {
        // 해당 멤버의 좋아요 찾기
        Optional<ReviewLike> existingLike = reviewLikes.stream()
                .filter(like -> like.getMember().equals(member))
                .findFirst();

        if (existingLike.isPresent()) {
            // 이미 좋아요가 있다면 좋아요 제거
            reviewLikes.remove(existingLike.get());
        } else {
            // 좋아요가 없다면 새로운 좋아요 추가
            ReviewLike newLike = new ReviewLike();
            newLike.setReview(this);
            newLike.setMember(member);
            reviewLikes.add(newLike);
        }
    }


}
