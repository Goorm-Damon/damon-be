package damon.backend.controller;

import damon.backend.dto.Result;
import damon.backend.dto.request.ReviewRequest;
import damon.backend.dto.response.ReviewListResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.Area;
import damon.backend.service.AwsS3Service;
import damon.backend.service.ReviewService;
import damon.backend.util.Log;
import damon.backend.util.login.AuthToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Tag(name = "리뷰 API", description = "리뷰 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewController {
    private final ReviewService reviewService;
    private final AwsS3Service awsS3Service;

    @PostMapping
    @Operation(summary = "내 리뷰 등록", description = "내 리뷰를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 등록 성공")
    public Result<ReviewResponse> postReview(
            @Valid
            @RequestBody ReviewRequest reviewRequest,
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier
    ){
        ReviewResponse review = reviewService.postReview(reviewRequest, identifier);
        return Result.success(review);
    }

    @PostMapping("/upload")
    @Operation(summary = "내 이미지 등록", description = "내 이미지를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "이미지 등록 성공")
    public Result<List<String>> imageUpload(

            @RequestParam("images") List<MultipartFile> images
    ) {
        log.info("Starting image upload process with {} images.", images.size());
        List<String> imageUrls = reviewService.postImage(images);  // 이미지 처리 로직을 서비스 계층으로 이동
        log.info("이미지 업로드 프로세스 완료: 업로드된 이미지 URL {}", imageUrls);

        return Result.success(imageUrls);
    }
    @PutMapping("/{reviewId}")
    @Operation(summary = "내 리뷰 수정", description = "내 리뷰를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 수정 성공")
    public Result<ReviewResponse> updateReview(
            @Schema(description = "리뷰 인덱스", example="1")
            @Valid
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest reviewRequest,
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier){
        ReviewResponse updatedReview = reviewService.updateReview(
                reviewId, reviewRequest,
                identifier);

        return Result.success(updatedReview);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "리뷰 및 댓글 상세 조회", description = "리뷰와 댓글을 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 및 댓글 상세 조회 성공")
    public Result<ReviewResponse> searchReviewDetail(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String viewCookieName = "reviewView";
        boolean hasViewed = hasViewed(request.getCookies(), viewCookieName, reviewId);

        ReviewResponse reviewDetail = reviewService.searchReviewDetail(reviewId, !hasViewed);
        if (!hasViewed) {
            setViewCookie(response, viewCookieName, reviewId);
        }

        return Result.success(reviewDetail);
    }

    private boolean hasViewed(Cookie[] cookies, String cookieName, Long reviewId) {
//        if문 새로추가
        if (cookies == null) {
            return false;
        }

        return Arrays.stream(cookies)
                .filter(c -> cookieName.equals(c.getName()))
                .anyMatch(c -> c.getValue().contains("|" + reviewId + "|"));
    }

    private void setViewCookie(HttpServletResponse response, String cookieName, Long reviewId) {
        Cookie cookie = new Cookie(cookieName, "|" + reviewId + "|");
        cookie.setMaxAge(24 * 60 * 60); // Set for 24 hours
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @GetMapping("/list")
    @Operation(summary = "리뷰 전체 및 지역별 조회", description = "리뷰를 전체 및 지역별로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 전체 및 지역별 조회 성공")
    public Result<Page<ReviewListResponse>> searchReviewList(
            @Schema(description = "페이지 인덱스", example="0")
            @RequestParam("page") int page,
            @Schema(description = "한 페이지 당 보여질 리뷰 개수", example="10")
            @RequestParam("pageSize") int pageSize,
            @Schema(description = "지역", example="강릉")
            @RequestParam(value = "area", required = false) Optional<Area> area
    ) {
        Page<ReviewListResponse> reviewList = reviewService.searchReviewList(page, pageSize, area.orElse(null));
        return Result.success(reviewList);
    }

    @GetMapping("/list/search")
    @Operation(summary = "리뷰 검색", description = "작성자 이름, 리뷰 제목, 태그를 기반으로 리뷰를 검색합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 검색 성공")
    public Result<Page<ReviewListResponse>> searchReviews(
            @Schema(description = "검색 옵션", example="작성자 / 제목 / 태그")
            @RequestParam("mode") String mode,
            @Schema(description = "검색어", example="강민우")
            @RequestParam("keyword") String keyword,
            @Schema(description = "페이지 인덱스", example="0")
            @RequestParam("page") int page,
            @Schema(description = "한 페이지 당 보여질 리뷰 개수", example="10")
            @RequestParam("pageSize") int pageSize) {
        Page<ReviewListResponse> searchReviews = reviewService.searchReviews(mode, keyword, page, pageSize);
        return Result.success(searchReviews);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "내 리뷰 삭제", description = "내 리뷰를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 등록 삭제")
    public Result<Void> deleteReview(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId,
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier
    ) {
        reviewService.deleteReview(reviewId, identifier);
        return Result.success(null);
    }

    @PatchMapping("/{reviewId}")
    @Operation(summary = "리뷰 좋아요 누르기", description = "리뷰에 좋아요를 누릅니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 좋아요 성공")
    public Result<ReviewResponse> toggleLike(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId,
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier
    ) {
        reviewService.toggleLike(reviewId, identifier);
        return Result.success(null);
    }

    @GetMapping("/likes")
    @Operation(summary = "내 좋아요 게시글", description = "내가 좋아요 누른 리뷰를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 좋아요 조회 성공")
    public Result<Page<ReviewListResponse>> searchLikedReviews(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @Schema(description = "페이지 인덱스", example="0")
            @RequestParam("page") int page,
            @Schema(description = "한 페이지 당 보여질 리뷰 개수", example="10")
            @RequestParam("pageSize") int pageSize
    ) {
        Page<ReviewListResponse> likedReviews = reviewService.searchLikedReviews(identifier, page, pageSize);
        return Result.success(likedReviews);
    }

    @GetMapping("/best")
    @Operation(summary = "베스트 리뷰", description = "메인 페이지에서 베스트 리뷰를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "베스트 리뷰 조회 성공")

    public Result<List<ReviewListResponse>> getTopReviewsForMainPage() {
        List<ReviewListResponse> topReviews = reviewService.findTopReviewsForMainPage(5);
        return Result.success(topReviews);
    }

    // 내 게시글 목록 조회
    /*@GetMapping("/my/list")
    @Operation(summary = "내 리뷰 조회", description = "내가 작성한 리뷰를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 리뷰 조회 성공")
    public Result<Page<ReviewListResponse>> searchMyReviewList(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @Schema(description = "페이지 인덱스", example="0")
            @RequestParam("page") int page,
            @Schema(description = "한 페이지 당 보여질 리뷰 개수", example="10")
            @RequestParam("pageSize") int pageSize
    ){
        Page<ReviewListResponse> myReviews = reviewService.searchMyReview(page, pageSize);
        return Result.success(myReviews);
    }

     */
}