package damon.backend.controller;


import damon.backend.dto.request.ReviewRequest;
import damon.backend.dto.response.ReviewListResponse;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.entity.Area;
import damon.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "리뷰 API", description = "리뷰 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewController {
    private final ReviewService reviewService;

    //게시글 등록
    @PostMapping
    @Operation(summary = "내 리뷰 등록", description = "내 리뷰를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 등록 성공")
    public ReviewResponse postReview(
            @RequestBody ReviewRequest reviewRequest
            ){
        return reviewService.postReview(reviewRequest);
    }


    //게시글 목록 조회
    @GetMapping("/list")
    @Operation(summary = "리뷰 전체 조회", description = "리뷰를 전체 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 전체 조회 성공")
    public List<ReviewListResponse> searchReviewList(
            @Schema(description = "페이지 인덱스", example="0")
            @RequestParam("page") int page,
            @Schema(description = "한 페이지 당 보여질 리뷰 개수", example="10")
            @RequestParam("pageSize") int pageSize,

            @RequestParam(value = "area", required = false) Optional<Area> area
    ){
        return reviewService.searchReviewList(page, pageSize, area.orElse(null));
    }


    //게시글 상세 조회 (댓글 포함)
    @GetMapping("/{reviewId}")
    @Operation(summary = "리뷰 상세 조회", description = "리뷰를 상세 조회합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 상세 조회 성공")
    public ReviewResponse searchReviewDetail(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId
    ) {
        ReviewResponse reviewResponse = reviewService.searchReview(reviewId);
        reviewService.incrementReviewViewCount(reviewId); // 조회수 증가
        return reviewResponse;
    }

    //게시글 수정
    @PutMapping("/{reviewId}")
    @Operation(summary = "내 리뷰 수정", description = "내 리뷰를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 수정 성공")
    public ResponseEntity<ReviewResponse> updateReview(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest reviewRequest){
        ReviewResponse updatedReview = reviewService.updateReview(reviewId, reviewRequest); //memberId 추후에 추가
        return ResponseEntity.ok(updatedReview);
    }

    //게시글 삭제
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "내 리뷰 삭제", description = "내 리뷰를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 등록 삭제")
    public ResponseEntity<Void> deleteReview(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(reviewId); // memberId 추가
        return ResponseEntity.ok().build(); // HTTP 200 OK 응답
    }

    // 좋아요 토글
    @PatchMapping("/{reviewId}")
    @Operation(summary = "리뷰 좋아요 누르기", description = "리뷰에 좋아요를 누릅니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 좋아요 성공")
    public ResponseEntity<ReviewResponse> toggleLike(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId
    ) {
        ReviewResponse updatedReview = reviewService.toggleLike(reviewId);
        return ResponseEntity.ok(updatedReview); // 토글 후 리뷰의 최신 상태 반환 // HTTP 200 OK 응답
    }


    // freeTag 기반 검색
    @GetMapping("/list/search")
    @Operation(summary = "리뷰 태그별 검색", description = "리뷰를 태그 별로 검색 합니다.")
    @ApiResponse(responseCode = "200", description = "리뷰 태그별 검색 성공")
    public List<ReviewListResponse> searchReviewsByFreeTag(
            @Schema(description = "검색할 태그명", example="강릉")
            @RequestParam("freeTag") String freeTag,
            @Schema(description = "페이지 인덱스", example="0")
            @RequestParam("page") int page,
            @Schema(description = "한 페이지 당 보여질 리뷰 개수", example="10")
            @RequestParam("pageSize") int pageSize
    ) {
        return reviewService.searchReviewsByFreeTag(freeTag, page, pageSize);
    }



}