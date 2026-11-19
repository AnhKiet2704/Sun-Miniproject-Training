package com.example.employeemanagement.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

// Module 2: Bean tạo bằng @Service (format chuỗi, tạo mã nhân viên tự động)
@Service
public class UtilityService {

    public String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return Arrays.stream(name.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT)
                        + word.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    
    public String formatDepartment(String department) {
        if (department == null || department.isBlank()) {
            return "GENERAL";
        }
        return department.trim().toUpperCase(Locale.ROOT);
    }

    /** Mã nhân viên sinh từ id (duy nhất vì id là khóa chính): 7 -> "EMP00007" */
    public String generateEmployeeCode(Long id) {
        return String.format("EMP%05d", id);
    }
}
