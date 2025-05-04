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
            + "WHERE (:firstName IS NULL OR UPPER(e.firstName) LIKE UPPER(CONCAT('%', :firstName, '%'))) "
            + "AND (:lastName IS NULL OR UPPER(e.lastName) LIKE UPPER(CONCAT('%', :lastName, '%')))")
    List<Employee> findByFirstNameAndLastName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName);

    @Query(value = "SELECT e FROM Employee e "
            + "WHERE (:firstName IS NULL OR UPPER(e.firstName) LIKE UPPER(CONCAT('%', :firstName, '%')))")
    List<Employee> findByFirstName(@Param("firstName") String firstName);

    @Query(value = "SELECT e FROM Employee e "
            + "WHERE (:lastName IS NULL OR UPPER(e.lastName) LIKE UPPER(CONCAT('%', :lastName, '%')))")
    List<Employee> findByLastName(@Param("lastName") String lastName);

    @Query(value = "SELECT e FROM Employee e "
            + "JOIN e.assignments a "
            + "WHERE (:assignmentId IS NULL OR a.id = :assignmentId)")
    List<Employee> findEmployeesByAssignmentId(@Param("assignmentId") Long assignmentId);

    Optional<Employee> findById(Long id);

    Employee save(Employee employee);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);
}