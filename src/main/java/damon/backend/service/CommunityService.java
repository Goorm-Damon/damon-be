package damon.backend.service;

import damon.backend.dto.response.community.CommunityCommentDTO;
import damon.backend.dto.response.community.CommunityDetailDTO;
import damon.backend.dto.response.community.CommunitySimpleDTO;
import damon.backend.entity.community.Community;
import damon.backend.entity.community.CommunityComment;
import damon.backend.entity.Member;
import damon.backend.enums.CommunityType;
import damon.backend.exception.EntityNotFoundException;
import damon.backend.repository.MemberRepository;
import damon.backend.repository.community.CommunityCommentRepository;
import damon.backend.repository.community.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityCommentRepository commentRepository;
    private final MemberRepository memberRepository;

    private Member getMemberEntity(String provider) {
        return memberRepository.findByProviderName(provider).orElseThrow(() -> new EntityNotFoundException("provider", provider));
    }

    private Community getCommunityEntity(Long communityId) {
        return communityRepository.findOne(communityId).orElseThrow(() -> new EntityNotFoundException("community", communityId));
    }

    private CommunityComment getCommentEntity(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("comment", commentId));
    }

    public boolean isCommunityWriter(String memberId, Long communityId) {
        return memberId.equals(getCommunityEntity(communityId).getMember().getId());
    }

    public boolean isCommentWriter(String memberId, Long commentId) {
        return memberId.equals(getCommentEntity(commentId).getMember().getId());
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

    public Page<CommunitySimpleDTO> getMyCommunityPaging(String provider, CommunityType type, int page) {
        Page<Community> communities = communityRepository.findMyByPage(getMemberEntity(provider).getId(), type, PageRequest.of(page, 20));
        return communities.map(CommunitySimpleDTO::new);
    }

    @Transactional
    public CommunityDetailDTO addCommunity(String provider, CommunityType type, String title, String content) {
        Community community = new Community(getMemberEntity(provider), type, title, content);
        communityRepository.save(community);
        return new CommunityDetailDTO(community);
    }

    @Transactional
    public CommunityDetailDTO setCommunity(Long communityId, String title, String content, List<String> images) {
        Community community = getCommunityEntity(communityId);
        community.setCommunity(title, content, images);
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
        communityRepository.delete(getCommunityEntity(communityId));
    }

    @Transactional
    public CommunityCommentDTO addComment(String provider, Long communityId, String content) {
        CommunityComment comment = getCommunityEntity(communityId).addComment(getMemberEntity(provider), content);
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
    public CommunityCommentDTO addChildComment(String provider, Long parentCommentId, String content) {
        CommunityComment parentComment = getCommentEntity(parentCommentId);
        CommunityComment childComment = commentRepository.save(new CommunityComment(
                getMemberEntity(provider),
                getCommunityEntity(parentComment.getCommunity().getCommunityId()),
                content,
                parentComment
        ));

        parentComment.addChildComment(childComment);
        return new CommunityCommentDTO(childComment);
    }

    @Transactional
    public boolean toggleLike(String provider, Long communityId) {
        Member member = getMemberEntity(provider);
        Community community = getCommunityEntity(communityId);

        if (community.isLike(member)) {
            community.removeLike(member);
            return false;
        } else {
            community.addLike(member);
            return true;
        }
    }

    public boolean isLike(String provider, Long communityId) {
        return getCommunityEntity(communityId).isLike(getMemberEntity(provider));
    }
}