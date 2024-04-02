package damon.backend.service;
import damon.backend.dto.request.ReviewRequest;
import damon.backend.dto.response.ReviewCommentResponse;
import damon.backend.dto.response.ReviewListResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.*;
import damon.backend.entity.user.User;
import damon.backend.exception.custom.*;
import damon.backend.repository.ReviewImageRepository;
import damon.backend.repository.ReviewLikeRepository;
import damon.backend.repository.ReviewRepository;
import damon.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final AwsS3Service awsS3Service;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final CommentStructureOrganizer commentStructureOrganizer;

    // 등록
    @Transactional
    public ReviewResponse postReview(ReviewRequest request, String identifier) {

        User user = userRepository.findByIdentifier(identifier).orElseThrow(UserNotFoundException::new);

        Review review = Review.create(request.getTitle(), request.getStartDate(), request.getEndDate(),
                request.getArea(), request.getCost(), request.getSuggests(),
                request.getContent(), request.getTags(), user);
        review = reviewRepository.save(review);

        if (request.getImageUrls() != null) {
            for (String url : request.getImageUrls()) {
                ReviewImage reviewImage = new ReviewImage(url, review);
                reviewImageRepository.save(reviewImage);
                review.addImage(reviewImage);
            }
        }

        review = reviewRepository.save(review);  // 리뷰 엔티티 업데이트

        List<ReviewCommentResponse> emptyCommentsList = new ArrayList<>(); // 새 리뷰에는 댓글이 없으므로 빈 리스트 생성
        return ReviewResponse.from(review, emptyCommentsList); // 저장된 리뷰와 빈 댓글 목록을 전달
    }

    // 수정
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, String identifier) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(UserNotFoundException::new);

        if (!review.getUser().getIdentifier().equals(identifier)) {
            throw new UnauthorizedException();
        }

        // 리뷰 업데이트
        review.update(request.getTitle(), request.getStartDate(), request.getEndDate(), request.getArea(),
                request.getCost(), request.getSuggests(), request.getContent(), request.getTags());

        // 이미지 삭제 로직
        List<String> deleteImageUrls = request.getDeleteImageUrls();
        if (deleteImageUrls != null && !deleteImageUrls.isEmpty()) {
            log.info("리뷰 이미지 삭제: {}", deleteImageUrls);
            for (String url : deleteImageUrls) {
                reviewImageRepository.findByUrlAndReview(url, review).ifPresent(image -> {
                    log.info("데이터베이스에서 이미지 {} 삭제 중", url);
                    reviewImageRepository.delete(image);
                    reviewImageRepository.flush(); // 변경 사항 즉시 반영
                    log.info("데이터베이스에서 이미지 {} 삭제 완료, S3에서 삭제 요청 중", url);
                    awsS3Service.deleteImageByUrl(url);
                    log.info("S3에서 이미지 {} 삭제 요청 완료", url);
                });
            }
        }

        // 새로운 이미지 URL 처리
        List<String> newImageUrls = request.getNewImageUrls();
        if (newImageUrls != null && !newImageUrls.isEmpty()) {
            log.info("새 이미지 추가 중: {}", newImageUrls);
            for (String url : newImageUrls) {
                ReviewImage newImage = ReviewImage.createImage(url, review);
                review.addImage(newImage); // 리뷰 객체에 이미지 추가
                reviewImageRepository.save(newImage);
                log.info("새 이미지 추가됨: {}", url);
            }
        }

        reviewRepository.save(review); // 변경사항 저장

        // 댓글 구조를 다시 조직화
        List<ReviewCommentResponse> organizedComments = commentStructureOrganizer.organizeCommentStructure(reviewId);

        // 구조화된 댓글 목록을 포함하여 ReviewResponse 반환
        return ReviewResponse.from(review, organizedComments);
    }

    // 이미지 등록
    @Transactional
    public List<String> postImage(List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            log.info("Processing {} images", images.size());

            for (MultipartFile file : images) {
                try {
                    String imageUrl = awsS3Service.uploadImage(file);
                    imageUrls.add(imageUrl);
                } catch (IOException e) {
                    throw new ImageCountExceededException();
                } catch (MaxUploadSizeExceededException e) {
                    throw new ImageSizeExceededException();
                }
            }
        }
        return imageUrls;
    }

    // 상세 조회 (댓글 포함)
    @Transactional(readOnly = true)
    public ReviewResponse searchReviewDetail(Long reviewId, boolean incrementViewCount) {
        Review review = reviewRepository.findReviewWithCommentsAndRepliesByReviewId(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        if (incrementViewCount) {
            review.incrementViewCount();
            reviewRepository.save(review);
        }

        // 댓글 구조화 로직을 사용하여 댓글 목록 가져오기 (상세 조회 시에만 호출)
        List<ReviewCommentResponse> organizedComments = commentStructureOrganizer.organizeCommentStructure(reviewId);

        // DTO 반환 댓글 목록 포함
        return ReviewResponse.from(review, organizedComments); // 이 부분에서 organizedComments를 DTO에 포함시켜야 합니다.
    }

    // 전체 및 지역별 조회
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> searchReviewList(int page, int pageSize, Area area) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Review> reviews = area != null ? reviewRepository.findByArea(area, pageable) : reviewRepository.findAll(pageable);
        return reviews.map(ReviewListResponse::from);
    }

    // 검색
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> searchReviews(String searchMode, String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Review> reviews = reviewRepository.searchByCriteria(searchMode, keyword, pageable);
        return reviews.map(ReviewListResponse::from);
    }

    // 삭제
    public void deleteReview(Long reviewId, String identifier) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);
        User user = userRepository.findByIdentifier(identifier).orElseThrow(UserNotFoundException::new);

        if (!review.getUser().getIdentifier().equals(identifier)) {
            throw new UnauthorizedException();
        }

        // 리뷰와 연관된 모든 이미지를 S3에서 삭제
        List<ReviewImage> reviewImages = review.getReviewImages();
        for (ReviewImage reviewImage : reviewImages) {
            awsS3Service.deleteImageByUrl(reviewImage.getUrl());
            reviewImageRepository.delete(reviewImage);
        }

        reviewRepository.delete(review);
    }

    // 좋아요 수 (다시 누르면 좋아요 취소)
    @Transactional
    public void toggleLike(Long reviewId, String identifier) {
        User user = userRepository.findByIdentifier(identifier).orElseThrow(UserNotFoundException::new);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        Optional<ReviewLike> like = reviewLikeRepository.findByReviewAndUser(review, user);
        if (like.isPresent()) {
            review.decreaseLikeCount();
            reviewLikeRepository.delete(like.get());
        } else {
            ReviewLike reviewLike = ReviewLike.createLike(review, user);
            review.increaseLikeCount();
            reviewLikeRepository.save(reviewLike);
        }
    }

    // 좋아요 누른 게시글 조회
    @Transactional(readOnly = true)
    public Page<ReviewListResponse> searchLikedReviews(String identifier, int page, int pageSize) {
        User user = userRepository.findByIdentifier(identifier).orElseThrow(UserNotFoundException::new);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Review> likedReviews = reviewLikeRepository.findReviewsByUser(user, pageable);
        return likedReviews.map(ReviewListResponse::from);
    }


    // 메인 페이지 베스트 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewListResponse> findTopReviewsForMainPage(int size) {
        Pageable topFive = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "likeCount"));
        Page<Review> reviews = reviewRepository.findAll(topFive); // findAll 메서드 사용 시 정렬 조건에 likeCount를 사용
        return reviews.getContent()
                .stream()
                .map(ReviewListResponse::from)
                .collect(Collectors.toList());
    }

    // 내 리뷰 조회
    /*@Transactional(readOnly = true)
    public Page<ReviewListResponse> searchMyReview(String identifier) {
        User user = userRepository.findByIdentifier(identifier).orElseThrow(UserNotFoundException::new);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Review> myReviews = reviewLikeRepository.findMyReviews(user, pageable);

        List<Review> myReviews = reviewRepository.findMyReviews(user.getId());
        return myReviews.map(ReviewListResponse::from);
    }

     */
}
