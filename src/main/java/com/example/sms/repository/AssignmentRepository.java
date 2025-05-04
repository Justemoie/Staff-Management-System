package com.example.sms.repository;

import com.example.sms.entity.Assignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findById(Long id);

    Assignment save(Assignment assignment);

    @Query("SELECT COUNT(a) > 0 FROM Assignment a "
            + "WHERE LOWER(a.title) = LOWER(:title)")
    boolean existsByTitle(@Param("title") String title);
}
