package com.example.SMS.repository;

import com.example.SMS.entity.Assignment;
import com.example.SMS.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
