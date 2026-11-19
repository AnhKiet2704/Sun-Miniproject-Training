package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.RegisterRequestDTO;
import com.example.employeemanagement.model.Role;
import com.example.employeemanagement.model.User;
import com.example.employeemanagement.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

// Module 9: trang đăng nhập / đăng ký 
@Controller
public class AuthWebController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthWebController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/employees/list";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequestDTO request,
                           BindingResult bindingResult) {
        if (!bindingResult.hasErrors() && userRepository.existsByUsername(request.getUsername())) {
            bindingResult.rejectValue("username", "duplicate", "Tên đăng nhập đã được sử dụng");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }
        userRepository.save(new User(null, request.getUsername(),
                passwordEncoder.encode(request.getPassword()), Role.ROLE_USER));
        return "redirect:/login?registered";
    }
}
