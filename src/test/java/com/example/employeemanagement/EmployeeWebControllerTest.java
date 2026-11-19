package com.example.employeemanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Module 6 + 10: các trang Thymeleaf (render thật template nên cũng kiểm tra được lỗi cú pháp template)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        // Trình duyệt gửi Accept: text/html nên Spring Security redirect sang trang /login
        mockMvc.perform(get("/employees/list").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void loginAndRegisterPagesArePublic() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
        mockMvc.perform(get("/register")).andExpect(status().isOk()).andExpect(view().name("register"));
    }

    @Test
    void userSeesEmployeeListAndSearch() throws Exception {
        mockMvc.perform(get("/employees/list").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(view().name("employee-list"))
                .andExpect(content().string(containsString("Nguyen Van An")));

        mockMvc.perform(get("/employees/search").param("name", "binh").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tran Thi Binh")));
    }

    @Test
    void userSeesStatisticsPage() throws Exception {
        mockMvc.perform(get("/employees/statistics").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(view().name("statistics"));
    }

    @Test
    void userCannotOpenAddForm_adminCan() throws Exception {
        mockMvc.perform(get("/employees/add").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/employees/add").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(view().name("employee-form"));
    }

    @Test
    void adminAddFormShowsValidationErrors() throws Exception {
        mockMvc.perform(post("/employees/add").with(httpBasic("admin", "admin123")).with(csrf())
                        .param("name", "")
                        .param("email", "bad")
                        .param("department", "IT")
                        .param("position", "Dev")
                        .param("salary", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee-form"));
    }
}
