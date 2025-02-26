package com.example.sms.repository;

import com.example.sms.entity.Assignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findById(long id);

    Assignment save(Assignment assignment);
}
