package com.example.employeemanagement.config;

import com.example.employeemanagement.dto.EmployeeRequestDTO;
import com.example.employeemanagement.model.Department;
import com.example.employeemanagement.model.Role;
import com.example.employeemanagement.model.User;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Dữ liệu mẫu: phòng ban, nhân viên, tài khoản admin/user (chỉ tạo khi chưa có)
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DepartmentRepository departmentRepository,
                           EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           EmployeeService employeeService,
                           PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.employeeService = employeeService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            departmentRepository.save(new Department(null, "IT", "Information Technology"));
            departmentRepository.save(new Department(null, "HR", "Human Resources"));
            departmentRepository.save(new Department(null, "FINANCE", "Finance & Accounting"));
        }

        if (employeeRepository.count() == 0) {
            employeeService.createEmployee(new EmployeeRequestDTO(
                    "Nguyen Van An", "an.nguyen@example.com", "IT", "Backend Developer", 1500.0));
            employeeService.createEmployee(new EmployeeRequestDTO(
                    "Tran Thi Binh", "binh.tran@example.com", "HR", "HR Specialist", 1200.0));
            employeeService.createEmployee(new EmployeeRequestDTO(
                    "Le Van Cuong", "cuong.le@example.com", "IT", "DevOps Engineer", 1800.0));
        }

        createUserIfMissing("admin", "admin123", Role.ROLE_ADMIN);
        createUserIfMissing("user", "user123", Role.ROLE_USER);
    }

    private void createUserIfMissing(String username, String rawPassword, Role role) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(new User(null, username, passwordEncoder.encode(rawPassword), role));
            logger.info("Created default account '{}' with role {}", username, role);
        }
    }
}
