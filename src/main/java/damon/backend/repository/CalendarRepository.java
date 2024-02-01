package damon.backend.repository;

import damon.backend.entity.Calendar;
import damon.backend.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    Page<Calendar> findByMember(Member member, Pageable pageable);
}
