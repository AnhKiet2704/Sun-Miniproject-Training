package com.example.employeemanagement.dto;

// Module 10: kết quả thống kê số nhân viên theo phòng ban (dùng làm constructor expression trong @Query)
public class DepartmentStatDTO {

    private String department;
    private Long count;

    public DepartmentStatDTO() {
    }

    public DepartmentStatDTO(String department, Long count) {
        this.department = department;
        this.count = count;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
