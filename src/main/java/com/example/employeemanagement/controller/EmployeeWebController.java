package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequestDTO;
import com.example.employeemanagement.dto.EmployeeResponseDTO;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Module 6: Spring Boot Web 
@Controller
@RequestMapping("/employees")
public class EmployeeWebController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final EmployeeService employeeService;

    public EmployeeWebController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public String index() {
        return "redirect:/employees/list";
    }

    // Lab 6: danh sách nhân viên từ DB (kèm tìm kiếm theo tên / phòng ban)
    @GetMapping({"/list", "/search"})
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) String department,
                       Authentication authentication,
                       Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees(name, department));
        model.addAttribute("departments", employeeService.getAllDepartments());
        model.addAttribute("searchName", name);
        model.addAttribute("searchDepartment", department);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", hasAdminRole(authentication));
        return "employee-list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new EmployeeRequestDTO());
        model.addAttribute("departments", employeeService.getAllDepartments());
        return "employee-form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String add(@Valid @ModelAttribute("employee") EmployeeRequestDTO request,
                      BindingResult bindingResult,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                employeeService.createEmployee(request);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên thành công");
                return "redirect:/employees/list";
            } catch (DuplicateResourceException ex) {
                bindingResult.rejectValue("email", "duplicate", "Email đã tồn tại");
            }
        }
        model.addAttribute("departments", employeeService.getAllDepartments());
        return "employee-form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        EmployeeResponseDTO current = employeeService.getEmployeeById(id);
        model.addAttribute("employee", new EmployeeRequestDTO(current.getName(), current.getEmail(),
                current.getDepartment(), current.getPosition(), current.getSalary()));
        model.addAttribute("employeeId", id);
        model.addAttribute("departments", employeeService.getAllDepartments());
        return "employee-form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("employee") EmployeeRequestDTO request,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                employeeService.updateEmployee(id, request);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công");
                return "redirect:/employees/list";
            } catch (DuplicateResourceException ex) {
                bindingResult.rejectValue("email", "duplicate", "Email đã tồn tại");
            }
        }
        model.addAttribute("employeeId", id);
        model.addAttribute("departments", employeeService.getAllDepartments());
        return "employee-form";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa nhân viên thành công");
        return "redirect:/employees/list";
    }

    // Lab 10: trang thống kê /employees/statistics
    @GetMapping("/statistics")
    public String statistics(Authentication authentication, Model model) {
        model.addAttribute("totalEmployees", employeeService.countTotalEmployees());
        model.addAttribute("departmentStats", employeeService.getDepartmentStatistics());
        model.addAttribute("salaryAnalytics", employeeService.getSalaryAnalytics());
        model.addAttribute("username", authentication.getName());
        return "statistics";
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }
}
