package damon.backend.controller;

import damon.backend.dto.request.ReviewCommentRequest;
import damon.backend.dto.response.ReviewResponse;
import damon.backend.service.ReviewCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰 댓글 API", description = "리뷰 댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewCommentController {
    private final ReviewCommentService reviewCommentService;

    // 댓글 등록
    @PostMapping("/{reviewId}/comments")
    @Operation(summary = "내 댓글 등록", description = "내 댓글을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 등록 성공")
    public ReviewResponse postComment(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId,
            @RequestBody ReviewCommentRequest reviewCommentRequest) {
        return reviewCommentService.postComment(reviewId, reviewCommentRequest);
    }


    // 댓글 수정
//    @PreAuthorize("#providerName == authentication.principal.providername")
    @PatchMapping("/{reviewId}/comments/{commentId}")
    @Operation(summary = "내 댓글 수정", description = "내 댓글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    public ResponseEntity<ReviewResponse> updateComment(
            @Schema(description = "리뷰 인덱스", example="1")
            @PathVariable Long reviewId,
            @Schema(description = "댓글 인덱스", example="1")
            @PathVariable Long commentId,
            @RequestBody ReviewCommentRequest request) { // @AuthenticationPrincipal CustomOAuth2User customOAuth2User 추후에 추가

        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            ReviewResponse updatedReview = reviewCommentService.updateComment(commentId, request);
            return ResponseEntity.ok(updatedReview); // 수정된 리뷰의 최신 상태를 반환
        }
        return ResponseEntity.badRequest().build();

       }

    // 댓글 삭제
//    @PreAuthorize("#providerName == authentication.principal.providername")
    @DeleteMapping("/{reviewId}/comments/{commentId}")
    @Operation(summary = "내 댓글 삭제", description = "내 댓글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    public ResponseEntity<Void> deleteComment(
            @Schema(description = "댓글 인덱스", example="1")
            @PathVariable Long commentId
    ) { // @AuthenticationPrincipal CustomOAuth2User customOAuth2User 추후에 추가
        reviewCommentService.deleteComment(commentId);
        return ResponseEntity.ok().build(); // HTTP 200 OK 응답
    }



}
