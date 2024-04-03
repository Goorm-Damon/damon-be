package damon.backend.service;

import damon.backend.dto.response.community.CommunityCommentDTO;
import damon.backend.dto.response.community.CommunityDetailDTO;
import damon.backend.dto.response.community.CommunitySimpleDTO;
import damon.backend.entity.community.Community;
import damon.backend.entity.community.CommunityComment;
import damon.backend.entity.user.User;
import damon.backend.enums.CommunityType;
import damon.backend.exception.custom.DataNotFoundException;
import damon.backend.repository.community.CommunityCommentRepository;
import damon.backend.repository.community.CommunityQueryRepository;
import damon.backend.repository.community.CommunityRepository;
import damon.backend.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 커뮤니티와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommunityQueryRepository communityQueryRepository;

    private User getUserEntity(String identifier) {
        return userRepository.findByIdentifier(identifier).orElseThrow(DataNotFoundException::new);
    }

    private Community getCommunityEntity(Long communityId) {
        return communityRepository.findOne(communityId).orElseThrow(DataNotFoundException::new);
    }

    private CommunityComment getCommentEntity(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(DataNotFoundException::new);
    }

    public boolean isCommunityWriter(String identifier, Long communityId) {
        return identifier.equals(getCommunityEntity(communityId).getUser().getIdentifier());
    }

    public boolean isCommentWriter(String identifier, Long commentId) {
        return identifier.equals(getCommentEntity(commentId).getUser().getIdentifier());
    }

    public CommunityDetailDTO getCommunity(Long communityId) {
        return new CommunityDetailDTO(getCommunityEntity(communityId));
    }

    public List<CommunitySimpleDTO> getCommunityList(CommunityType type) {
        List<Community> communities = communityRepository.findAllByList(type);

        return communities.stream()
                .map(CommunitySimpleDTO::new)
                .collect(Collectors.toList());
    }

    public Page<CommunitySimpleDTO> getCommunityPaging(CommunityType type, int page) {
        Page<Community> communities = communityRepository.findAllByPage(type, PageRequest.of(page, 20));
        return communities.map(CommunitySimpleDTO::new);
    }

    public List<CommunitySimpleDTO> getCommunityTop5(CommunityType type) {
        List<Community> communities = communityRepository.findTop5ByList(type);

        return communities.stream()
                .map(CommunitySimpleDTO::new)
                .collect(Collectors.toList());
    }

    public Page<CommunitySimpleDTO> getMyCommunityPaging(String identifier, CommunityType type, int page) {
        Page<Community> communities = communityRepository.findMyByPage(getUserEntity(identifier).getId(), type, PageRequest.of(page, 20));
        return communities.map(CommunitySimpleDTO::new);
    }

    @Transactional
    public CommunityDetailDTO addCommunity(String identifier, CommunityType type, String title, String content) {
        Community community = new Community(getUserEntity(identifier), type, title, content);
        communityRepository.save(community);
        return new CommunityDetailDTO(community);
    }

    @Transactional
    public CommunityDetailDTO setCommunity(Long communityId, String title, String content, List<String> deleteImageUrls, List<String> addImageUrls) {
        Community community = getCommunityEntity(communityId);

        List<String> newImageUrls = community.getImages();
        for (String imageUrl : deleteImageUrls) {
            if (newImageUrls.contains(imageUrl)) newImageUrls.remove(imageUrl);
        }
        newImageUrls.addAll(addImageUrls);

        community.setCommunity(title, content, newImageUrls);
        communityRepository.save(community);
        return new CommunityDetailDTO(community);
    }

    @Transactional
    public CommunityDetailDTO setCommunity(Long communityId, String title, String content) {
        Community community = getCommunityEntity(communityId);
        community.setCommunity(title, content);
        communityRepository.save(community);
        return new CommunityDetailDTO(community);
    }

    @Transactional
    public void removeCommunity(Long communityId) {
        Community community = getCommunityEntity(communityId);
        communityRepository.delete(community);
    }

    @Transactional
    public CommunityCommentDTO addComment(String identifier, Long communityId, String content) {
        CommunityComment comment = getCommunityEntity(communityId).addComment(getUserEntity(identifier), content);
        commentRepository.save(comment);
        return new CommunityCommentDTO(comment);
    }

    @Transactional
    public CommunityCommentDTO setComment(Long commentId, String newContent) {
        CommunityComment comment = getCommentEntity(commentId);
        comment.setCommunityComment(newContent);
        commentRepository.save(comment);
        return new CommunityCommentDTO(comment);
    }

    @Transactional
    public void removeComment(Long commentId) {
        CommunityComment communityComment = getCommentEntity(commentId);
        Community community = getCommunityEntity(communityComment.getCommunity().getCommunityId());
        community.getComments().remove(communityComment);
    }

    @Transactional
    public CommunityCommentDTO addChildComment(String identifier, Long parentCommentId, String content) {
        CommunityComment parentComment = getCommentEntity(parentCommentId);
        CommunityComment childComment = commentRepository.save(new CommunityComment(
                getUserEntity(identifier),
                getCommunityEntity(parentComment.getCommunity().getCommunityId()),
                content,
                parentComment
        ));

        parentComment.addChildComment(childComment);
        return new CommunityCommentDTO(childComment);
    }

    @Transactional
    public boolean toggleLike(String identifier, Long communityId) {
        User user = getUserEntity(identifier);
        Community community = getCommunityEntity(communityId);

        if (community.isLike(user)) {
            community.removeLike(user);
            return false;
        } else {
            community.addLike(user);
            return true;
        }
    }

    public boolean isLike(String identifier, Long communityId) {
        return getCommunityEntity(communityId).isLike(getUserEntity(identifier));
    }

    public Page<CommunitySimpleDTO> searchCommunity(String keyword, CommunityType type, int page) {
        Page<Community> communities = communityQueryRepository.searchCommunity(keyword, type, PageRequest.of(page, 20));
        return communities.map(CommunitySimpleDTO::new);
    }
}