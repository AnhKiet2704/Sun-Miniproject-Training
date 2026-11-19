package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.DepartmentStatDTO;
import com.example.employeemanagement.dto.EmployeeRequestDTO;
import com.example.employeemanagement.dto.EmployeeResponseDTO;
import com.example.employeemanagement.dto.ReportSummaryDTO;
import com.example.employeemanagement.dto.SalaryAnalyticsDTO;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.model.Department;
import com.example.employeemanagement.model.Employee;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private static final String COUNT_CACHE = "employeeCount";

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UtilityService utilityService;
    private final ModelMapper modelMapper;

    // Module 2: Constructor Injection (UtilityService + ModelMapper bean tự định nghĩa trong AppConfig)
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               UtilityService utilityService,
                               ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.utilityService = utilityService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees(String name, String department) {
        String n = name == null ? "" : name.trim();
        String d = department == null ? "" : department.trim();
        logger.debug("Searching employees: name='{}', department='{}'", n, d);

        List<Employee> employees;
        if (n.isEmpty() && d.isEmpty()) {
            employees = employeeRepository.findAll(Sort.by("id"));
        } else if (d.isEmpty()) {
            employees = employeeRepository.findByNameContainingIgnoreCaseOrderByIdAsc(n);
        } else if (n.isEmpty()) {
            employees = employeeRepository.findByDepartmentNameContainingIgnoreCaseOrderByIdAsc(d);
        } else {
            employees = employeeRepository
                    .findByNameContainingIgnoreCaseAndDepartmentNameContainingIgnoreCaseOrderByIdAsc(n, d);
        }
        return employees.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @CacheEvict(value = COUNT_CACHE, allEntries = true)
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        String email = request.getEmail().trim();
        if (employeeRepository.existsByEmailIgnoreCase(email)) {
            logger.warn("Create employee failed, duplicate email: {}", email);
            throw new DuplicateResourceException("Email đã tồn tại: " + email);
        }

        Employee employee = new Employee();
        applyRequest(employee, request);
        Employee saved = employeeRepository.save(employee);          // IDENTITY: id có ngay sau khi save
        saved.setEmployeeCode(utilityService.generateEmployeeCode(saved.getId()));

        logger.info("Employee created: id={}, code={}, email={}", saved.getId(), saved.getEmployeeCode(), saved.getEmail());
        return toResponse(saved);
    }

    @Override
    @CacheEvict(value = COUNT_CACHE, allEntries = true)
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO request) {
        Employee existing = findOrThrow(id);
        String email = request.getEmail().trim();
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            logger.warn("Update employee {} failed, duplicate email: {}", id, email);
            throw new DuplicateResourceException("Email đã tồn tại: " + email);
        }

        applyRequest(existing, request);
        Employee updated = employeeRepository.save(existing);

        logger.info("Employee updated: id={}, code={}", updated.getId(), updated.getEmployeeCode());
        return toResponse(updated);
    }

    @Override
    @CacheEvict(value = COUNT_CACHE, allEntries = true)
    public void deleteEmployee(Long id) {
        Employee existing = findOrThrow(id);
        employeeRepository.delete(existing);
        logger.info("Employee deleted: id={}, code={}", id, existing.getEmployeeCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentStatDTO> getDepartmentStatistics() {
        return employeeRepository.countEmployeesByDepartment();
    }

    // Module 8: Caching (TTL 1 phút cấu hình trong application.yml)
    @Override
    @Transactional(readOnly = true)
    @Cacheable(COUNT_CACHE)
    public long countTotalEmployees() {
        logger.debug("Counting employees from database (cache miss)");
        return employeeRepository.countTotalEmployees();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryAnalyticsDTO> getSalaryAnalytics() {
        return employeeRepository.getSalaryAnalyticsByDepartment();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportSummaryDTO getReportSummary() {
        return new ReportSummaryDTO(
                employeeRepository.countTotalEmployees(),
                departmentRepository.count(),
                employeeRepository.countEmployeesByDepartment(),
                employeeRepository.getSalaryAnalyticsByDepartment());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll(Sort.by("name"));
    }

    // ---------------- helpers ----------------

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> {
            logger.warn("Employee not found: id={}", id);
            return new ResourceNotFoundException("Không tìm thấy nhân viên với id: " + id);
        });
    }

    private void applyRequest(Employee employee, EmployeeRequestDTO request) {
        String deptName = utilityService.formatDepartment(request.getDepartment());
        Department department = departmentRepository.findByNameIgnoreCase(deptName)
                .orElseGet(() -> departmentRepository.save(new Department(null, deptName, deptName + " Department")));

        employee.setName(utilityService.normalizeName(request.getName()));
        employee.setEmail(request.getEmail().trim());
        employee.setPosition(request.getPosition().trim());
        employee.setSalary(request.getSalary());
        employee.setDepartment(department);
    }

    private EmployeeResponseDTO toResponse(Employee employee) {
        EmployeeResponseDTO dto = modelMapper.map(employee, EmployeeResponseDTO.class);
        // Department là object lồng nhau -> gán thủ công tên phòng ban cho DTO
        dto.setDepartment(employee.getDepartment() != null ? employee.getDepartment().getName() : null);
        return dto;
    }
}
