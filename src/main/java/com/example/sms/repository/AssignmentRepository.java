package com.example.sms.repository;

import com.example.sms.entity.Assignment;

import java.util.List;
import java.util.Optional;

import com.example.sms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Optional<Assignment> findById(long id);

    Assignment save(Assignment assignment);

    boolean existsByTitle(String title);
}
