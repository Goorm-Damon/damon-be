package damon.backend.repository.community;

import damon.backend.entity.community.Community;
import damon.backend.enums.CommunityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long>  {

    @Query("SELECT c FROM Community c JOIN FETCH c.member WHERE c.communityId = :communityId")
    Optional<Community> findOne(Long communityId);

    @Query("SELECT c FROM Community c JOIN FETCH c.member WHERE c.type = :type ORDER BY c.createdDate DESC")
    List<Community> findAllByList(CommunityType type);

    @Query("SELECT c FROM Community c JOIN FETCH c.member WHERE c.type = :type ORDER BY c.createdDate DESC")
    Page<Community> findAllByPage(CommunityType type, Pageable pageable);

    @Query("SELECT c FROM Community c JOIN FETCH c.member WHERE c.type = :type ORDER BY c.createdDate DESC LIMIT 5")
    List<Community> findTop5ByList(CommunityType type);

    @Query("SELECT c FROM Community c JOIN FETCH c.member m WHERE c.type = :type AND m.id = :memberId ORDER BY c.createdDate DESC")
    Page<Community> findMyByPage(Long memberId, CommunityType type, Pageable pageable);
}
