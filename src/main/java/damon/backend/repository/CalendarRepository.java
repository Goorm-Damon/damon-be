package damon.backend.repository;

import damon.backend.entity.Calendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    @Query("SELECT c FROM Calendar c WHERE c.member.id = :memberId")
    Page<Calendar> findPageByMember(@Param("memberId")String memberId, Pageable pageable);

    @Query("SELECT distinct c FROM Calendar c JOIN FETCH c.travels WHERE c.id = :calendarId")
    Optional<Calendar> findByIdWithTravel(@Param("calendarId") Long calendarId);
}
