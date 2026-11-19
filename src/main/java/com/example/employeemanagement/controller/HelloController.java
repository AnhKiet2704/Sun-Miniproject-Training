package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Module 1 (Lab 1): API /hello 
@RestController
public class HelloController {

    @GetMapping({"/hello", "/api/hello"})
    public ResponseEntity<ApiResponse<String>> hello() {
        return ResponseEntity.ok(ApiResponse.success("Employee Management API is running successfully!", "Hello World"));
    }
}
