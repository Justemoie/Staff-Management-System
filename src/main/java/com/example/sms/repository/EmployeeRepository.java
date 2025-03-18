package com.example.sms.repository;

import com.example.sms.entity.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query(value = "SELECT e FROM Employee e "
            + "WHERE (:first_name IS NULL OR e.firstName = :first_name)")
    List<Employee> findByFirstName(@Param("first_name") String firstName);

    @Query(value = "SELECT * FROM employees "
            + "WHERE (:last_name IS NULL OR last_name = :last_name)",
            nativeQuery = true)
    List<Employee> findByLastName(
            @Param("last_name") String lastName);


    @Query(value = "SELECT e.* FROM employees e " +
            "LEFT JOIN employee_assignments ea ON e.id = ea.employee_id " +
            "LEFT JOIN assignments a ON ea.assignment_id = a.id " +
            "WHERE (:assignmentId IS NULL OR a.id = :assignmentId)",
            nativeQuery = true)
    List<Employee> findEmployeesByAssignmentId(@Param("assignmentId") Long assignmentId);

    Optional<Employee> findById(Long id);

    Employee save(Employee employee);
}

