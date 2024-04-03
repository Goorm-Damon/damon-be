package damon.backend.controller;

import damon.backend.dto.Result;
import damon.backend.dto.request.community.CommunityCommentCreateForm;
import damon.backend.dto.request.community.CommunityCommentUpdateForm;
import damon.backend.dto.request.community.CommunityCreateForm;
import damon.backend.dto.request.community.CommunityUpdateForm;
import damon.backend.dto.response.community.CommunityCommentDTO;
import damon.backend.dto.response.community.CommunityDetailDTO;
import damon.backend.dto.response.community.CommunitySimpleDTO;
import damon.backend.entity.community.Community;
import damon.backend.entity.user.User;
import damon.backend.enums.CommunityType;
import damon.backend.exception.custom.DataNotFoundException;
import damon.backend.repository.community.CommunityRepository;
import damon.backend.repository.user.UserRepository;
import damon.backend.service.CommunityService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Slf4j
@SpringBootTest
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 개별적으로 로드하여 테스트 실행 -> 태스트가 너무 느려짐
@Transactional
class CommunityControllerTest {

    @Autowired
    private CommunityController communityController;

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    BindingResult bindingResult;

    @BeforeEach
    void beforeEach() {
        bindingResult = mock(BindingResult.class);
        User user = userRepository.save(new User("1", "장성준", "", ""));
        Community community = communityRepository.save(new Community(user, CommunityType.자유, "community title", "community content"));
    }

    @Test
    void getCommunityList() {
        Result<List<CommunitySimpleDTO>> result = communityController.getCommunityList(CommunityType.자유);
        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().size(), 1)
        );

//        Result
//        {
//            "status" : "200 OK",
//                "message" : null,
//                "data" : [ {
//            "communityId" : 1,
//                    "userId" : 1,
//                    "memberName" : "장성준",
//                    "memberImage" : "",
//                    "createdDate" : [ 2024, 3, 2, 14, 41, 50, 999771800 ],
//            "type" : "자유",
//                    "title" : "community title",
//                    "views" : 0,
//                    "likesCount" : 0,
//                    "commentsCount" : 0
//        } ]
//        }
    }

    @Test
    void getCommunityPaging() {
        Result<Page<CommunitySimpleDTO>> result = communityController.getCommunityPaging(CommunityType.자유, 0);
        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().getContent().size(), 1)
        );
    }

    @Test
    void getCommunityTop5() {
        Result<List<CommunitySimpleDTO>> result = communityController.getCommunityTop5(CommunityType.자유);
        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().size(), 1)
        );
    }

    @Test
    void getMyCommunityPaging() {
        Result<Page<CommunitySimpleDTO>> result = communityController.getMyCommunityPaging("1", CommunityType.자유, 0);
        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().getContent().size(), 1)
        );
    }

    @Test
    @Disabled
    void getCommunity() {
        Result<CommunityDetailDTO> result = communityController.getCommunity(1L);
        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().getTitle(), "community title"),
                () -> assertEquals(result.getData().getContent(), "community content")
        );
    }

    @Test
    void addCommunity() {
        CommunityCreateForm form = new CommunityCreateForm();
        form.setTitle("title");
        form.setContent("content");
        form.setType(CommunityType.자유);
        form.setImageUrls(null);

        Result<CommunityDetailDTO> result = communityController.addCommunity("1", form, bindingResult);

        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().getTitle(), "title"),
                () -> assertEquals(result.getData().getContent(), "content")
        );

//        Result
//        {
//            "status" : "200 OK",
//                "message" : null,
//                "data" : {
//            "communityId" : 2,
//                    "userId" : 1,
//                    "memberName" : "장성준",
//                    "memberImage" : "",
//                    "createdDate" : [ 2024, 3, 2, 14, 55, 3, 51112700 ],
//            "type" : "자유",
//                    "title" : "title",
//                    "content" : "content",
//                    "views" : 0,
//                    "images" : [ ],
//            "likes" : [ ],
//            "comments" : [ ]
//        }
//        }
    }

    @Test
    @Disabled
    void setCommunity() {
        CommunityUpdateForm form = new CommunityUpdateForm();
        form.setCommunityId(1L);
        form.setTitle("update title");
        form.setContent("update content");
        form.setImages(null);

        Result<CommunityDetailDTO> result = communityController.setCommunity("1", form, bindingResult);

        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().getTitle(), "update title"),
                () -> assertEquals(result.getData().getContent(), "update content")
        );
    }

    @Test
    @Disabled
    void removeCommunity() {
        CommunityUpdateForm form = new CommunityUpdateForm();
        form.setCommunityId(1L);
        form.setTitle("update title");
        form.setContent("update content");
        form.setImages(null);

        Result<Boolean> result = communityController.removeCommunity("1", 1L);

        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertTrue(result.getData()),
                () -> assertThrows(DataNotFoundException.class, () -> {communityService.getCommunity(1L);})
        );
    }

    @Test
    @Disabled
    void addComment() {
        CommunityCommentCreateForm form = new CommunityCommentCreateForm();
        form.setCommunityId(1L);
        form.setContent("comment");

        Result<CommunityCommentDTO> result = communityController.addComment("1", form, bindingResult);

        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().getContent(), "comment")
        );
    }

    @Test
    @Disabled
    void setComment() {
        CommunityCommentCreateForm createForm = new CommunityCommentCreateForm();
        createForm.setCommunityId(1L);
        createForm.setContent("comment");
        communityController.addComment("1", createForm, bindingResult);

        CommunityCommentUpdateForm form = new CommunityCommentUpdateForm();
        form.setCommentId(1L);
        form.setContent("update comment");

        Result<CommunityCommentDTO> result = communityController.setComment("1", form, bindingResult);

        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertEquals(result.getData().getContent(), "update comment")
        );
    }

    @Test
    @Disabled
    void removeComment() {
        CommunityCommentCreateForm form = new CommunityCommentCreateForm();
        form.setCommunityId(1L);
        form.setContent("comment");
        communityController.addComment("1", form, bindingResult);

        Result<Boolean> result = communityController.removeComment("1", 1L);

        assertAll(
                () -> assertEquals(result.getStatus(), "200 OK"),
                () -> assertNull(result.getMessage()),
                () -> assertTrue(result.getData())
        );
    }
}