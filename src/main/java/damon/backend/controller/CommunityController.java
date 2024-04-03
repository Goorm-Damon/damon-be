package damon.backend.controller;

import damon.backend.dto.Result;
import damon.backend.dto.request.community.*;
import damon.backend.dto.response.community.CommunityCommentDTO;
import damon.backend.dto.response.community.CommunityDetailDTO;
import damon.backend.dto.response.community.CommunitySimpleDTO;
import damon.backend.entity.ReviewImage;
import damon.backend.enums.CommunityType;
import damon.backend.exception.custom.InvalidFormException;
import damon.backend.exception.custom.UnauthorizedException;
import damon.backend.service.AwsS3Service;
import damon.backend.service.CommunityService;
import damon.backend.util.login.AuthToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 커뮤니티 API 컨트롤러입니다.
 */
@Tag(name = "커뮤니티 API", description = "커뮤니티, 댓글, 좋아요 관련 API")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CommunityController {

    private final AwsS3Service awsS3Service;
    private final CommunityService communityService;

    /**
     * 커뮤니티 전체 리스트를 조회합니다.
     *
     * @param type 커뮤니티 타입(번개, 자유)
     * @return 커뮤니티 전체 리스트
     */
    @Operation(summary = "커뮤니티 전체 리스트 조회")
    @GetMapping
    public Result<List<CommunitySimpleDTO>> getCommunityList(
            @Schema(description = "커뮤니티 타입(번개, 자유)", defaultValue = "번개")
            @RequestParam CommunityType type
    ) {
        List<CommunitySimpleDTO> communityList = communityService.getCommunityList(type);
        return Result.success(communityList);
    }

    /**
     * 커뮤니티 전체를 페이징하여 조회합니다.
     *
     * @param type 커뮤니티 타입(번개, 자유)
     * @param page 페이지 번호(0부터)
     * @return 커뮤니티 전체 페이징 조회 결과
     */
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

    /**
     * 커뮤니티 상위 5개를 조회합니다.
     *
     * @param type 커뮤니티 타입(번개, 자유)
     * @return 커뮤니티 상위 5개
     */
    @Operation(summary = "커뮤니티 상위 5개 조회")
    @GetMapping("/top5")
    public Result<List<CommunitySimpleDTO>> getCommunityTop5(
            @Schema(description = "커뮤니티 타입(번개, 자유)", defaultValue = "번개")
            @RequestParam CommunityType type
    ) {
        List<CommunitySimpleDTO> communityList = communityService.getCommunityTop5(type);
        return Result.success(communityList);
    }

    /**
     * 현재 사용자가 작성한 커뮤니티를 페이징하여 조회합니다.
     *
     * @param identifier 유저 식별자
     * @param type 커뮤니티 타입(번개, 자유)
     * @param page 페이지 번호(0부터)
     * @return 현재 사용자가 작성한 커뮤니티 페이징 조회 결과
     */
    @Operation(summary = "내가 쓴 커뮤니티 페이징 조회")
    @GetMapping("/my")
    public Result<Page<CommunitySimpleDTO>> getMyCommunityPaging(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @Schema(description = "커뮤니티 타입(번개, 자유)", defaultValue = "번개")
            @RequestParam CommunityType type,
            @Schema(description = "페이지 번호(0부터)", defaultValue = "0")
            @RequestParam int page
    ) {
        Page<CommunitySimpleDTO> communityList = communityService.getMyCommunityPaging(identifier, type, page);
        return Result.success(communityList);
    }

    /**
     * 커뮤니티를 조회합니다.
     *
     * @param communityId 커뮤니티 아이디
     * @return 커뮤니티 상세 정보
     */
    @Operation(summary = "커뮤니티 단건 조회")
    @GetMapping("/{communityId}")
    public Result<CommunityDetailDTO> getCommunity(
            @Schema(description = "커뮤니티 아이디", defaultValue = "1")
            @PathVariable Long communityId
    ) {
        CommunityDetailDTO communityDetail = communityService.getCommunity(communityId);
        return Result.success(communityDetail);
    }

    @PostMapping("/upload")
    @Operation(summary = "내 이미지 등록", description = "내 이미지를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "이미지 등록 성공")
    public Result<List<String>> imageUpload(
            @RequestParam("images") List<MultipartFile> images
    ) {
        List<String> imageUrls = awsS3Service.upload(images);

        return Result.success(imageUrls);
    }

    /**
     * 커뮤니티를 추가합니다.
     *
     * @param identifier 유저 식별자
     * @param createForm 커뮤니티 생성 폼
     * @param bindingResult 바인딩 결과
     * @return 추가된 커뮤니티 상세 정보
     * @throws InvalidFormException 폼이 유효하지 않을 경우 발생하는 예외
     */
    @Operation(summary = "커뮤니티 추가")
    @PostMapping
    public Result<CommunityDetailDTO> addCommunity(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @RequestBody @Valid CommunityCreateForm createForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new InvalidFormException();
        }

        CommunityDetailDTO addedCommunity = communityService.addCommunity(
                identifier,
                createForm.getType(),
                createForm.getTitle(),
                createForm.getContent()
        );
        return Result.success(addedCommunity);
    }

    /**
     * 커뮤니티를 수정합니다.
     *
     * @param identifier 유저 식별자
     * @param updateForm 커뮤니티 수정 폼
     * @param bindingResult 바인딩 결과
     * @return 수정된 커뮤니티 상세 정보
     * @throws InvalidFormException 폼이 유효하지 않을 경우 발생하는 예외
     * @throws UnauthorizedException 수정 권한이 없는 경우 발생하는 예외
     */
    @Operation(summary = "커뮤니티 수정")
    @PutMapping
    public Result<CommunityDetailDTO> setCommunity(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @RequestBody @Valid CommunityUpdateForm updateForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new InvalidFormException();
        }

        if (!communityService.isCommunityWriter(identifier, updateForm.getCommunityId())) {
            throw new UnauthorizedException();
        }

        // 이미지 삭제 처리
        awsS3Service.delete(updateForm.getDeleteImageUrls());

        CommunityDetailDTO updatedCommunity = communityService.setCommunity(
                updateForm.getCommunityId(),
                updateForm.getTitle(),
                updateForm.getContent(),
                updateForm.getDeleteImageUrls(),
                updateForm.getAddImageUrls()
        );
        return Result.success(updatedCommunity);
    }

    /**
     * 커뮤니티를 삭제합니다.
     *
     * @param identifier 유저 식별자
     * @param communityId 커뮤니티 아이디
     * @return 삭제 여부
     * @throws UnauthorizedException 삭제 권한이 없는 경우 발생하는 예외
     */
    @Operation(summary = "커뮤니티 삭제", description = "정상적으로 제거 되었는지 여부를 반환해 줍니다.")
    @DeleteMapping("/{communityId}")
    public Result<Boolean> removeCommunity(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @Schema(description = "커뮤니티 아이디", defaultValue = "1")
            @PathVariable Long communityId
    ) {
        if (!communityService.isCommunityWriter(identifier, communityId)) {
            throw new UnauthorizedException();
        }

        // 이미지 삭제 처리
        awsS3Service.delete(communityService.getCommunity(communityId).getImages());
        communityService.removeCommunity(communityId);
        return Result.success(true);
    }

    /**
     * 커뮤니티에 댓글을 추가합니다.
     *
     * @param identifier 유저 식별자
     * @param commentCreateForm 커뮤니티 댓글 생성 폼
     * @param bindingResult 바인딩 결과
     * @return 추가된 커뮤니티 댓글 정보
     * @throws InvalidFormException 폼이 유효하지 않을 경우 발생하는 예외
     */
    @Operation(summary = "커뮤니티 댓글 추가")
    @PostMapping("/comment")
    public Result<CommunityCommentDTO> addComment(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @RequestBody @Valid CommunityCommentCreateForm commentCreateForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new InvalidFormException();
        }

        CommunityCommentDTO addedComment = communityService.addComment(
                identifier,
                commentCreateForm.getCommunityId(),
                commentCreateForm.getContent()
        );
        return Result.success(addedComment);
    }

    /**
     * 커뮤니티 댓글을 수정합니다.
     *
     * @param identifier 유저 식별자
     * @param updateForm 커뮤니티 댓글 수정 폼
     * @param bindingResult 바인딩 결과
     * @return 수정된 커뮤니티 댓글 정보
     * @throws InvalidFormException 폼이 유효하지 않을 경우 발생하는 예외
     * @throws UnauthorizedException 권한이 없을 경우 발생하는 예외
     */
    @Operation(summary = "커뮤니티 댓글 수정")
    @PutMapping("/comment")
    public Result<CommunityCommentDTO> setComment(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @RequestBody @Valid CommunityCommentUpdateForm updateForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new InvalidFormException();
        }

        if (!communityService.isCommentWriter(identifier, updateForm.getCommentId())) {
            throw new UnauthorizedException();
        }

        CommunityCommentDTO comment = communityService.setComment(
                updateForm.getCommentId(),
                updateForm.getContent()
        );
        return Result.success(comment);
    }

    /**
     * 커뮤니티 댓글을 삭제합니다.
     *
     * @param identifier 유저 식별자
     * @param commentId 커뮤니티 댓글 아이디
     * @return 삭제 여부를 나타내는 불리언 값
     * @throws UnauthorizedException 권한이 없을 경우 발생하는 예외
     */
    @Operation(summary = "커뮤니티 댓글 제거", description = "정상적으로 제거 되었는지 여부를 반환해 줍니다.")
    @DeleteMapping("/comment/{commentId}")
    public Result<Boolean> removeComment(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @Schema(description = "커뮤니티 댓글 아이디", defaultValue = "1")
            @PathVariable Long commentId
    ) {
        if (!communityService.isCommentWriter(identifier, commentId)) {
            throw new UnauthorizedException();
        }

        communityService.removeComment(commentId);
        return Result.success(true);
    }

    /**
     * 커뮤니티 댓글에 대댓글을 추가합니다.
     *
     * @param identifier 유저 식별자
     * @param form 커뮤니티 대댓글 추가 폼
     * @param bindingResult 바인딩 결과
     * @return 추가된 커뮤니티 대댓글 정보
     * @throws InvalidFormException 폼이 유효하지 않을 경우 발생하는 예외
     */
    @Operation(summary = "커뮤니티 대댓글 추가")
    @PostMapping("/comment/child")
    public Result<CommunityCommentDTO> addChildComment(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @RequestBody @Valid CommunityChildCommentCreateForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            throw new InvalidFormException();
        }

        CommunityCommentDTO addedChildComment = communityService.addChildComment(
                identifier,
                form.getParentId(),
                form.getContent()
        );
        return Result.success(addedChildComment);
    }

    /* ---커뮤니티 좋아요 확인--- */

    /**
     * 해당 유저가 특정 커뮤니티에 좋아요를 눌렀는지 확인합니다.
     *
     * @param identifier 유저 식별자
     * @param communityId 커뮤니티 아이디
     * @return 해당 커뮤니티에 대한 좋아요 여부
     */
    @Operation(summary = "커뮤니티 좋아요 확인")
    @GetMapping("/like/{communityId}")
    public Result<Boolean> isLike(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @Schema(description = "커뮤니티 아이디", defaultValue = "1")
            @PathVariable Long communityId
    ) {
        return Result.success(communityService.isLike(identifier, communityId));
    }

    /**
     * 해당 유저가 특정 커뮤니티에 좋아요를 추가하거나 제거합니다.
     *
     * @param identifier 유저 식별자
     * @param communityId 커뮤니티 아이디
     * @return 최종적으로 변경된 좋아요 여부
     */
    @Operation(summary = "커뮤니티 좋아요 토글", description = "최종적으로 좋아요 여부를 반환해 줍니다.")
    @PostMapping("/like/{communityId}")
    public Result<Boolean> addLike(
            @Parameter(description = "유저 식별자", required = true, hidden = true)
            @AuthToken String identifier,
            @Schema(description = "커뮤니티 아이디", defaultValue = "1")
            @PathVariable Long communityId
    ) {
        return Result.success(communityService.toggleLike(identifier, communityId));
    }

    /**
     * 검색어에 따라 커뮤니티를 검색합니다.
     *
     * @param keyword 검색어
     * @param type 커뮤니티 타입
     * @param page 페이지 번호
     * @return 검색된 커뮤니티 목록 페이지
     */
    @Operation(summary = "커뮤니티 검색")
    @GetMapping("/search")
    public Result<Page<CommunitySimpleDTO>> searchCommunity(
            @Schema(description = "검색어")
            @RequestParam(required = false) String keyword,
            @Schema(description = "커뮤니티 타입(번개, 자유)")
            @RequestParam(required = false) CommunityType type,
            @Schema(description = "페이지 번호(0부터)", defaultValue = "0")
            @RequestParam int page
    ) {
        return Result.success(communityService.searchCommunity(keyword, type, page));
    }
}
