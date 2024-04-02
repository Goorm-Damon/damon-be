package damon.backend.entity;

import damon.backend.dto.request.ReviewRequest;
import damon.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    private long viewCount = 0;
    private long likeCount = 0;

    private boolean isEdited = false; // 변경 여부를 추적하는 필드

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Area area;

    private Long cost;

    @ElementCollection
    private List<String> suggests = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String content;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImages = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewLike> reviewLikes = new HashSet<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> reviewComments = new ArrayList<>();


    //연관관계 매핑 메서드
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            user.getReviews().add(this);
        }
    }

    // 리뷰 공통 필드 캡슐화
    private void populateReviewFields(String title, LocalDate startDate, LocalDate endDate, Area area, Long cost, List<String> suggests, String content, List<String> tags) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.area = area;
        this.cost = cost;
        this.suggests = new ArrayList<>(suggests);
        this.content = content;
        // 태그 정보 처리
        this.tags.clear();
        if (tags != null) {
            for (String tagValue : tags) {
                this.tags.add(new Tag(tagValue, this));
            }
        }
    }

    // 리뷰 생성
    public static Review create(String title, LocalDate startDate, LocalDate endDate, Area area, Long cost, List<String> suggests, String content, List<String> tags, User user) {
        Review review = new Review();
        review.populateReviewFields(title, startDate, endDate, area, cost, suggests, content, tags);
        review.setUser(user);
        return review;
    }

    // 리뷰 수정
    public void update(String title, LocalDate startDate, LocalDate endDate, Area area, Long cost, List<String> suggests, String content, List<String> tags) {
        this.populateReviewFields(title, startDate, endDate, area, cost, suggests, content, tags);
        this.isEdited = true;
    }

    // 이미지 생성
    public void addImage(ReviewImage reviewImage) {
        this.reviewImages.add(reviewImage);
    }

    // 조회수
    public void incrementViewCount() {
        this.viewCount++;
    }
  
    // 좋아요수
    public void increaseLikeCount() {
        this.likeCount++;
    }
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }

    }
}