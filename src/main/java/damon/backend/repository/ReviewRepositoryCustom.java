package damon.backend.repository;

import damon.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository

public interface ReviewRepositoryCustom {
    Page<Review> searchByCriteria(String searchMode, String keyword, Pageable pageable);
}