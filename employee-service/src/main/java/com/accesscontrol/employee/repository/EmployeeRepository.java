package com.accesscontrol.employee.repository;

import com.accesscontrol.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByDocument(String document);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByStatus(Boolean status);
    boolean existsByDocument(String document);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
}

