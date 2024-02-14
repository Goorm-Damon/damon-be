package damon.backend.repository.community;

import damon.backend.entity.community.CommunityComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

}
