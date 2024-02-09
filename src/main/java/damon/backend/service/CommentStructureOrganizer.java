package damon.backend.service;

import damon.backend.dto.response.ReviewCommentResponse;

import java.util.List;

public interface CommentStructureOrganizer {
    List<ReviewCommentResponse> organizeCommentStructure(Long reviewId);
}
