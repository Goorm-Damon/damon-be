package damon.backend.controller;

import damon.backend.dto.Result;
import damon.backend.dto.request.community.*;
import damon.backend.dto.response.community.CommunityCommentDTO;
import damon.backend.dto.response.community.CommunityDetailDTO;
import damon.backend.dto.response.community.CommunitySimpleDTO;
import damon.backend.enums.CommunityType;
import damon.backend.exception.PermissionDeniedException;
import damon.backend.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "커뮤니티 API", description = "커뮤니티, 댓글, 좋아요 관련 API")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CommunityController {

    private final CommunityService communityService;

    /* ---커뮤니티 조회--- */

    @Operation(summary = "커뮤니티 전체 리스트 조회")
    @GetMapping
    public Result<List<CommunitySimpleDTO>> getCommunityList(
            @Schema(description = "커뮤니티 타입(번개, 자유)", defaultValue = "번개")
            @RequestParam CommunityType type
    ) {
        List<CommunitySimpleDTO> communityList = communityService.getCommunityList(type);
        return Result.success(communityList);
    }

    @Operation(summary = "커뮤니티 전체 페이징 조회")
    @GetMapping("/paging")
    public Result<Page<CommunitySimpleDTO>> getCommunityPaging(
            @Schema(description = "커뮤니티 타입(번개, 자유)", defaultValue = "번개")
            @RequestParam CommunityType type,
            @Schema(description = "페이지 번호(0부터)", defaultValue = "0")
            @RequestParam int page
    ) {
        Page<CommunitySimpleDTO> communityList = communityService.getCommunityPaging(type, page);
        return Result.success(communityList);
    }

    @Operation(summary = "커뮤니티 상위 5개 조회")
    @GetMapping("/top5")
    public Result<List<CommunitySimpleDTO>> getCommunityTop5(
            @Schema(description = "커뮤니티 타입(번개, 자유)", defaultValue = "번개")
            @RequestParam CommunityType type
    ) {
        List<CommunitySimpleDTO> communityList = communityService.getCommunityTop5(type);
        return Result.success(communityList);
    }

    @Operation(summary = "내가 쓴 커뮤니티 페이징 조회")
    @GetMapping("/my")
    public Result<Page<CommunitySimpleDTO>> getMyCommunityPaging(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @Schema(description = "커뮤니티 타입(번개, 자유)", defaultValue = "번개")
            @RequestParam CommunityType type,
            @Schema(description = "페이지 번호(0부터)", defaultValue = "0")
            @RequestParam int page
    ) {
        Page<CommunitySimpleDTO> communityList = communityService.getMyCommunityPaging(provider, type, page);
        return Result.success(communityList);
    }

    @Operation(summary = "커뮤니티 단건 조회")
    @GetMapping("/{communityId}")
    public Result<CommunityDetailDTO> getCommunity(
            @Schema(description = "커뮤니티 아이디", defaultValue = "1")
            @PathVariable Long communityId
    ) {
        CommunityDetailDTO communityDetail = communityService.getCommunity(communityId);
        return Result.success(communityDetail);
    }

    /* ---커뮤니티 추가/수정/삭제--- */

    @Operation(summary = "커뮤니티 추가")
    @PostMapping
    public Result<CommunityDetailDTO> addCommunity(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @RequestBody CommunityCreateForm createForm
    ) {
        CommunityDetailDTO addedCommunity = communityService.addCommunity(
                provider,
                createForm.getType(),
                createForm.getTitle(),
                createForm.getContent()
        );
        return Result.success(addedCommunity);
    }

    @Operation(summary = "커뮤니티 수정")
    @PutMapping
    public Result<CommunityDetailDTO> setCommunity(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @RequestBody CommunityUpdateForm updateForm
    ) {
        if (!communityService.isCommunityWriter(provider, updateForm.getCommunityId())) {
            throw new PermissionDeniedException();
        }

        CommunityDetailDTO updatedCommunity = communityService.setCommunity(
                updateForm.getCommunityId(),
                updateForm.getTitle(),
                updateForm.getContent(),
                updateForm.getImages()
        );
        return Result.success(updatedCommunity);
    }

    @Operation(summary = "커뮤니티 삭제", description = "정상적으로 제거 되었는지 여부를 반환해 줍니다.")
    @DeleteMapping("/{communityId}")
    public Result<Boolean> removeCommunity(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @Schema(description = "커뮤니티 아이디", defaultValue = "1")
            @PathVariable Long communityId
    ) {
        if (!communityService.isCommunityWriter(provider, communityId)) {
            throw new PermissionDeniedException();
        }

        communityService.removeCommunity(communityId);
        return Result.success(true);
    }

    /* ---커뮤니티 댓글 대댓글 추가/수정/삭제--- */

    @Operation(summary = "커뮤니티 댓글 추가")
    @PostMapping("/comment")
    public Result<CommunityCommentDTO> addComment(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @RequestBody CommunityCommentCreateForm commentCreateForm
    ) {
        CommunityCommentDTO addedComment = communityService.addComment(
                provider,
                commentCreateForm.getCommunityId(),
                commentCreateForm.getContent()
        );
        return Result.success(addedComment);
    }

    @Operation(summary = "커뮤니티 댓글 수정")
    @PutMapping("/comment")
    public Result<CommunityCommentDTO> setComment(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @RequestBody CommunityCommentUpdateForm updateForm
    ) {
        if (!communityService.isCommentWriter(provider, updateForm.getCommentId())) {
            throw new PermissionDeniedException();
        }

        CommunityCommentDTO comment = communityService.setComment(
                updateForm.getCommentId(),
                updateForm.getContent()
        );
        return Result.success(comment);
    }

    @Operation(summary = "커뮤니티 댓글 제거", description = "정상적으로 제거 되었는지 여부를 반환해 줍니다.")
    @DeleteMapping("/comment/{commentId}")
    public Result<Boolean> removeComment(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @Schema(description = "커뮤니티 댓글 아이디", defaultValue = "1")
            @PathVariable Long commentId
    ) {
        if (!communityService.isCommentWriter(provider, commentId)) {
            throw new PermissionDeniedException();
        }

        communityService.removeComment(commentId);
        return Result.success(true);
    }

    @Operation(summary = "커뮤니티 대댓글 추가")
    @PostMapping("/comment/child")
    public Result<CommunityCommentDTO> addChildComment(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @RequestBody CommunityChildCommentCreateForm form
    ) {
        CommunityCommentDTO addedChildComment = communityService.addChildComment(
                provider,
                form.getParentId(),
                form.getContent()
        );
        return Result.success(addedChildComment);
    }

    /* ---커뮤니티 좋아요 확인/추가/삭제--- */

    @Operation(summary = "커뮤니티 좋아요 확인")
    @GetMapping("/like/{communityId}")
    public Result<Boolean> isLike(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @Schema(description = "커뮤니티 아이디", defaultValue = "1")
            @PathVariable Long communityId
    ) {
        return Result.success(communityService.isLike(provider, communityId));
    }

    @Operation(summary = "커뮤니티 좋아요 토글", description = "최종적으로 좋아요 여부를 반환해 줍니다.")
    @PostMapping("/like/{communityId}")
    public Result<Boolean> addLike(
            @Schema(description = "멤버아이디", defaultValue = "1")
            @RequestParam String provider,
            @Schema(description = "커뮤니티 아이디", defaultValue = "1")
            @PathVariable Long communityId
    ) {
        return Result.success(communityService.toggleLike(provider, communityId));
    }
}
