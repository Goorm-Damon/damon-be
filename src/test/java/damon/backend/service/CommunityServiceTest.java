package damon.backend.service;

import damon.backend.dto.response.community.CommunityCommentDTO;
import damon.backend.dto.response.community.CommunityDetailDTO;
import damon.backend.dto.response.community.CommunitySimpleDTO;
import damon.backend.entity.Member;
import damon.backend.entity.community.Community;
import damon.backend.enums.CommunityType;
import damon.backend.exception.EntityNotFoundException;
import damon.backend.repository.MemberRepository;
import damon.backend.repository.community.CommunityCommentRepository;
import damon.backend.repository.community.CommunityLikeRepository;
import damon.backend.repository.community.CommunityRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

//@SpringBootTest
//@Transactional
//class CommunityServiceTest {
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private CommunityRepository communityRepository;
//
//    @Autowired
//    private CommunityCommentRepository commentRepository;
//
//    @Autowired
//    private CommunityLikeRepository likeRepository;
//
//    @Autowired
//    private CommunityService communityService;
//
//    private Member member1;
//    private Member member2;
//
//    private Community community1;
//    private Community community2;
//
//    @BeforeEach
//    void beforeEach() {
//        // given
//        member1 = new Member();
//        member1.setProvidername("1");
//        member1.setName("member1");
//        member1.setEmail("email1");
//        member1.setProfileImgUrl("/member1");
//        member1 = memberRepository.save(member1);
//
//        member2 = new Member();
//        member2.setProvidername("2");
//        member2.setName("member2");
//        member1.setEmail("email2");
//        member2.setProfileImgUrl("/member2");
//        member2 = memberRepository.save(member2);
//
//        community1 = new Community(
//                member1,
//                CommunityType.번개,
//                "community1 title",
//                "community1 content"
//        );
//
//        community1 = communityRepository.save(community1);
//
//        community2 = new Community(
//                member2,
//                CommunityType.자유,
//                "community2 title",
//                "community2 content"
//        );
//
//        community2 = communityRepository.save(community2);
//    }
//
//    @Test
//    @DisplayName("커뮤니티 정보 조회")
//    void getCommunity() {
//        // when
//        CommunityDetailDTO dto = communityService.getCommunity(community1.getCommunityId());
//
//        // then
//        assertAll(
//                () -> assertEquals(dto.getCommunityId(), community1.getCommunityId()),
//                () -> assertEquals(dto.getTitle(), community1.getTitle()),
//                () -> assertEquals(dto.getContent(), community1.getContent())
//        );
//
//    }
//
//    @Test
//    @DisplayName("커뮤니티 전체 리스트 조회")
//    void getCommunityList() {
//        // when
//        Community community3 = new Community(member1, CommunityType.번개, "community3 title", "community3 content");
//        community3 = communityRepository.save(community3);
//
//        Community community4 = new Community(member1, CommunityType.번개, "community4 title", "community4 content");
//        community4 = communityRepository.save(community4);
//
//        List<CommunitySimpleDTO> list1 = communityService.getCommunityList(CommunityType.번개);
//        List<CommunitySimpleDTO> list2 = communityService.getCommunityList(CommunityType.자유);
//
//        // then
//        assertAll(
//                () -> assertEquals(list1.size(), 3),
//                () -> assertEquals(list2.size(), 1),
//                () -> assertEquals(list1.getLast().getCommunityId(), community1.getCommunityId()),
//                () -> assertEquals(list1.getLast().getTitle(), community1.getTitle())
//        );
//    }
//
//    @Test
//    @DisplayName("커뮤니티 전체 페이징 조회")
//    void getCommunityPaging() {
//        // when
//        Community community3 = new Community(member1, CommunityType.번개, "community3 title", "community3 content");
//        community3 = communityRepository.save(community3);
//
//        Community community4 = new Community(member1, CommunityType.번개, "community4 title", "community4 content");
//        community4 = communityRepository.save(community4);
//
//        Page<CommunitySimpleDTO> page1 = communityService.getCommunityPaging(CommunityType.번개, 0);
//        Page<CommunitySimpleDTO> page2 = communityService.getCommunityPaging(CommunityType.자유, 0);
//
//        // then
//        assertAll(
//                () -> assertEquals(page1.getContent().size(), 3), // 조회된 데이터 수
//                () -> assertEquals(page2.getContent().size(), 1), // 조회된 데이터 수
//                () -> assertEquals(page1.getTotalElements(), 3), // 전체 데이터 수
//                () -> assertEquals(page1.getNumber(), 0), // 현재 페이지 번호
//                () -> assertEquals(page1.getTotalPages(), 1), // 전체 페이지 수
//                () -> assertTrue(page1.isFirst()), // 첫번째 항목인가
//                () -> assertFalse(page1.hasNext()) // 다음 페이지가 있는가
//        );
//    }
//
//    @Test
//    @DisplayName("커뮤니티 최근 5개 리스트 조회")
//    void getCommunityTop5() {
//        // when
//        Community community3 = new Community(member1, CommunityType.번개, "community3 title", "community3 content");
//        community3 = communityRepository.save(community3);
//
//        Community community4 = new Community(member1, CommunityType.번개, "community4 title", "community4 content");
//        community4 = communityRepository.save(community4);
//
//        Community community5 = new Community(member1, CommunityType.번개, "community5 title", "community5 content");
//        community5 = communityRepository.save(community5);
//
//        Community community6 = new Community(member1, CommunityType.번개, "community6 title", "community6 content");
//        community6 = communityRepository.save(community6);
//
//        Community community7 = new Community(member1, CommunityType.번개, "community7 title", "community7 content");
//        community7 = communityRepository.save(community7);
//
//        List<CommunitySimpleDTO> list1 = communityService.getCommunityTop5(CommunityType.번개);
//        List<CommunitySimpleDTO> list2 = communityService.getCommunityTop5(CommunityType.자유);
//
//        // then
//        Community finalCommunity1 = community3;
//        Community finalCommunity2 = community7;
//        assertAll(
//                () -> assertEquals(list1.size(), 5),
//                () -> assertEquals(list2.size(), 1),
//                () -> assertEquals(list1.getLast().getCommunityId(), finalCommunity1.getCommunityId()),
//                () -> assertEquals(list1.getFirst().getCommunityId(), finalCommunity2.getCommunityId())
//        );
//    }
//
//    @Test
//    @DisplayName("내가 작성한 커뮤니티 페이징 조회")
//    void getMyCommunityPaging() {
//        // when
//        Page<CommunitySimpleDTO> page1 = communityService.getMyCommunityPaging(member1.getProvidername(), CommunityType.번개, 0);
//        Page<CommunitySimpleDTO> page2 = communityService.getMyCommunityPaging(member1.getProvidername(), CommunityType.자유, 0);
//
//        // then
//        assertAll(
//                () -> assertEquals(page1.getContent().size(), 1), // 조회된 데이터 수
//                () -> assertEquals(page2.getContent().size(), 0), // 조회된 데이터 수
//                () -> assertEquals(page1.getContent().getLast().getCommunityId(), community1.getCommunityId())
//        );
//    }
//
//    @Test
//    @DisplayName("커뮤니티 추가")
//    void addCommunity() {
//        // when
//        Long communityId = communityService.addCommunity(member1.getProvidername(), CommunityType.번개, "community3 title", "community3 content").getCommunityId();
//        CommunityDetailDTO dto = communityService.getCommunity(communityId);
//
//        // then
//        assertAll(
//                () -> assertEquals(dto.getTitle(), "community3 title"),
//                () -> assertEquals(dto.getContent(), "community3 content")
//        );
//    }
//
//    @Test
//    @DisplayName("커뮤니티 수정")
//    void setCommunity() {
//        // when
//        communityService.setCommunity(community1.getCommunityId(), "update title", "update content");
//        CommunityDetailDTO dto = communityService.getCommunity(community1.getCommunityId());
//
//        // then
//        assertAll(
//                () -> assertEquals(dto.getTitle(), "update title"),
//                () -> assertEquals(dto.getContent(), "update content")
//        );
//    }
//
//    @Test
//    @DisplayName("커뮤니티 제거")
//    void removeCommunity() {
//        // when
//        communityService.removeCommunity(community1.getCommunityId());
//
//        // then
//        assertThrows(EntityNotFoundException.class, () -> {
//            communityService.getCommunity(community1.getCommunityId());
//        });
//    }
//
//    @Test
//    @DisplayName("커뮤니티 댓글 추가")
//    void addComment() {
//        // when
//        communityService.addComment(member1.getProvidername(), community1.getCommunityId(), "comment1 content");
//        communityService.addComment(member1.getProvidername(), community1.getCommunityId(), "comment2 content");
//        communityService.addComment(member1.getProvidername(), community1.getCommunityId(), "comment3 content");
//        CommunityCommentDTO dto = communityService.getCommunity(community1.getCommunityId()).getComments().getLast();
//
//        // then
//        assertEquals(dto.getContent(), "comment1 content");
//    }
//
//    @Test
//    @DisplayName("커뮤니티 댓글 수정")
//    void setComment() {
//        // when
//        Long commentId = communityService.addComment(member1.getProvidername(), community1.getCommunityId(), "comment1 content").getCommentId();
//        communityService.setComment(commentId, "update content");
//        CommunityCommentDTO dto = communityService.getCommunity(community1.getCommunityId()).getComments().getLast();
//
//        // then
//        assertEquals(dto.getContent(), "update content");
//    }
//
//    @Test
//    @DisplayName("커뮤니티 댓글 제거")
//    void removeComment() {
//        // when
//        Long commentId = communityService.addComment(member1.getProvidername(), community1.getCommunityId(), "comment1 content").getCommentId();
//        communityService.removeComment(commentId);
//
//        // then
//        assertEquals(communityService.getCommunity(community1.getCommunityId()).getComments().size(), 0);
//    }
//
//    @Test
//    @DisplayName("커뮤니티 대댓글 추가")
//    void addChildComment() {
//        // when
//        Long commentId = communityService.addComment(member1.getProvidername(), community1.getCommunityId(), "comment1 content").getCommentId();
//        Long childCommentId = communityService.addChildComment(member1.getProvidername(), commentId, "childComment1 content").getCommentId(); // 대댓글
//        communityService.addChildComment(member1.getProvidername(), commentId, "childComment2 content");
//
//        communityService.addChildComment(member1.getProvidername(), childCommentId, "child of child content"); // 대대댓글
//
//        CommunityCommentDTO dto = communityService.getCommunity(community1.getCommunityId()).getComments().getLast();
//
//        // then
//        assertAll(
//                () -> assertEquals(dto.getChildComments().getFirst().getContent(), "childComment1 content"),
//                () -> assertEquals(dto.getChildComments().getFirst().getChildComments().getFirst().getContent(), "child of child content")
//        );
//    }
//
//    @Test
//    @DisplayName("커뮤니티 댓글 제거")
//    void removeChildComment() {
//        // when
//        Long commentId = communityService.addComment(member1.getProvidername(), community1.getCommunityId(), "comment1 content").getCommentId();
//        communityService.addChildComment(member1.getProvidername(), commentId, "childComment1 content"); // 대댓글
//
//        CommunityCommentDTO dto = communityService.getCommunity(community1.getCommunityId()).getComments().getLast().getChildComments().getLast(); // 대댓글
//
//        communityService.removeComment(commentId);
//
//        // then
//        assertEquals(communityService.getCommunity(community1.getCommunityId()).getComments().size(), 0); // 하위 레벨의 댓글도 같이 삭제 됨.
//    }
//
//    @Test
//    @DisplayName("커뮤니티 좋아요")
//    void like() {
//        // when
//        communityService.toggleLike(member1.getProvidername(), community1.getCommunityId());
//        communityService.toggleLike(member1.getProvidername(), community2.getCommunityId());
//        communityService.toggleLike(member1.getProvidername(), community2.getCommunityId());
//
//        // then
//        assertAll(
//                () -> assertTrue(communityService.isLike(member1.getProvidername(), community1.getCommunityId())),
//                () -> assertFalse(communityService.isLike(member2.getProvidername(), community1.getCommunityId())),
//                () -> assertFalse(communityService.isLike(member1.getProvidername(), community2.getCommunityId())),
//                () -> assertFalse(communityService.isLike(member2.getProvidername(), community2.getCommunityId()))
//        );
//    }
//}