package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ApiResponse;
import com.example.employeemanagement.dto.DepartmentStatDTO;
import com.example.employeemanagement.dto.EmployeeRequestDTO;
import com.example.employeemanagement.dto.EmployeeResponseDTO;
import com.example.employeemanagement.dto.ReportSummaryDTO;
import com.example.employeemanagement.dto.SalaryAnalyticsDTO;
import com.example.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Module 3, 4, 5, 10: REST API quản lý nhân viên
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công",
                employeeService.getAllEmployees(name, department)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhân viên thành công",
                employeeService.getEmployeeById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> create(@Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm nhân viên thành công", employeeService.createEmployee(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody EmployeeRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhân viên thành công",
                employeeService.updateEmployee(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhân viên thành công", null));
    }

    // ---------- Module 8 + 10: báo cáo & thống kê ----------

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Long>> count() {
        return ResponseEntity.ok(ApiResponse.success("Tổng số nhân viên (cache 1 phút)",
                employeeService.countTotalEmployees()));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<DepartmentStatDTO>>> statistics() {
        return ResponseEntity.ok(ApiResponse.success("Thống kê nhân viên theo phòng ban",
                employeeService.getDepartmentStatistics()));
    }

    @GetMapping("/reports/summary")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReportSummaryDTO>> summary() {
        return ResponseEntity.ok(ApiResponse.success("Báo cáo tổng hợp", employeeService.getReportSummary()));
    }

    @GetMapping("/analytics/salary")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SalaryAnalyticsDTO>>> salaryAnalytics() {
        return ResponseEntity.ok(ApiResponse.success("Phân tích lương theo phòng ban",
                employeeService.getSalaryAnalytics()));
    }
}
