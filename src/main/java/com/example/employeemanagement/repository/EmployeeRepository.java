package com.example.employeemanagement.repository;

import com.example.employeemanagement.dto.DepartmentStatDTO;
import com.example.employeemanagement.dto.SalaryAnalyticsDTO;
import com.example.employeemanagement.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // Module 4: tìm kiếm theo tên hoặc phòng ban (derived query)
    List<Employee> findByNameContainingIgnoreCaseOrderByIdAsc(String name);

    List<Employee> findByDepartmentNameContainingIgnoreCaseOrderByIdAsc(String departmentName);

    List<Employee> findByNameContainingIgnoreCaseAndDepartmentNameContainingIgnoreCaseOrderByIdAsc(
            String name, String departmentName);

    // Module 10: thống kê bằng @Query
    @Query("SELECT COUNT(e) FROM Employee e")
    long countTotalEmployees();

    @Query("SELECT new com.example.employeemanagement.dto.DepartmentStatDTO(d.name, COUNT(e)) "
            + "FROM Department d LEFT JOIN d.employees e GROUP BY d.name ORDER BY d.name")
    List<DepartmentStatDTO> countEmployeesByDepartment();

    @Query("SELECT new com.example.employeemanagement.dto.SalaryAnalyticsDTO("
            + "d.name, AVG(e.salary), MIN(e.salary), MAX(e.salary), COUNT(e)) "
            + "FROM Department d JOIN d.employees e GROUP BY d.name ORDER BY d.name")
    List<SalaryAnalyticsDTO> getSalaryAnalyticsByDepartment();
}
