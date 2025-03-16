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

    List<Employee> findByLastName(String lastName);

    @Query(value = "SELECT * FROM employees "
            + "WHERE (:first_name IS NULL OR first_name = :first_name) "
            + "AND (:last_name IS NULL OR last_name = :last_name)",
            nativeQuery = true)
    List<Employee> findByFirstNameAndLastName(
            @Param("first_name") String firstName,
            @Param("last_name") String lastName);


    Optional<Employee> findById(Long id);

    Employee save(Employee employee);
}
