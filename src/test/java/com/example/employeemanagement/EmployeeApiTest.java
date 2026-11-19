package com.example.employeemanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Module 3, 4, 5, 9, 10: CRUD + validation + phân quyền + JWT + thống kê (H2 in-memory, profile "test")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String employeeJson(String name, String email, String department, String position, double salary) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"department\":\"" + department
                + "\",\"position\":\"" + position + "\",\"salary\":" + salary + "}";
    }

    private static String uniqueEmail() {
        return "test" + System.nanoTime() + "@example.com";
    }

    // ---------- Module 9: phân quyền ----------

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanListButCannotCreate() throws Exception {
        mockMvc.perform(get("/api/employees").with(httpBasic("user", "user123")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employees").with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Test User", uniqueEmail(), "IT", "Dev", 1000)))
                .andExpect(status().isForbidden());
    }

    // ---------- Module 3 + 4: CRUD ----------

    @Test
    void adminCanCreateReadUpdateAndDelete() throws Exception {
        String email = uniqueEmail();

        MvcResult created = mockMvc.perform(post("/api/employees").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("tran   van   test", email, "it", "Tester", 1234.5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Tran Van Test"))     // UtilityService.normalizeName
                .andExpect(jsonPath("$.data.department").value("IT"))           // UtilityService.formatDepartment
                .andReturn();

        JsonNode data = objectMapper.readTree(created.getResponse().getContentAsString()).get("data");
        long id = data.get("id").asLong();
        assertEquals(String.format("EMP%05d", id), data.get("employeeCode").asText());

        mockMvc.perform(get("/api/employees/" + id).with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        mockMvc.perform(put("/api/employees/" + id).with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Tran Van Test", email, "HR", "Senior Tester", 2000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.department").value("HR"))
                .andExpect(jsonPath("$.data.position").value("Senior Tester"));

        mockMvc.perform(delete("/api/employees/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByNameAndDepartment() throws Exception {
        mockMvc.perform(get("/api/employees").param("name", "nguyen")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Nguyen Van An"));

        mockMvc.perform(get("/api/employees").param("department", "finance")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ---------- Module 5: validation + exception handling ----------

    @Test
    void invalidRequestReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/employees").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("", "not-an-email", "IT", "Dev", 1000)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/employees").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"A\", \"salary\": \"abc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        mockMvc.perform(post("/api/employees").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Another One", "an.nguyen@example.com", "IT", "Dev", 1000)))
                .andExpect(status().isConflict());
    }

    // ---------- Module 9: đăng ký + JWT ----------

    @Test
    void registerAlwaysCreatesUserRoleAndJwtLoginWorks() throws Exception {
        String username = "jwtuser" + System.nanoTime();

        // cố tình gửi role=ROLE_ADMIN: server phải bỏ qua
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"secret123\",\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ROLE_USER"))
                .andReturn();

        String token = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("data").get("token").asText();

        mockMvc.perform(get("/api/employees").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employees").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Test User", uniqueEmail(), "IT", "Dev", 1000)))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- Module 8 + 10: thống kê ----------

    @Test
    void statisticsEndpointsReturnData() throws Exception {
        mockMvc.perform(get("/api/employees/count").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(greaterThanOrEqualTo(3)));

        mockMvc.perform(get("/api/employees/statistics").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.department == 'IT')]").exists());

        mockMvc.perform(get("/api/employees/analytics/salary").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].averageSalary").exists());

        mockMvc.perform(get("/api/employees/reports/summary").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDepartments").value(greaterThanOrEqualTo(3)));
    }
}
