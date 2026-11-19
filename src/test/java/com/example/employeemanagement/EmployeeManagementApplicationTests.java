package com.example.employeemanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc   // cùng cấu hình với các test khác để dùng chung 1 Spring context
@ActiveProfiles("test")
class EmployeeManagementApplicationTests {

    @Test
    void contextLoads() {
    }
}
