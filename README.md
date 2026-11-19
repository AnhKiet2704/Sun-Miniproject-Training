# Employee Management System - Mini Project



## Các tính năng chính theo Modules
- **Module 1 (Lab 1):** Khởi tạo project `employee-management`, API `GET /hello`.
- **Module 2 (Lab 2):** `UtilityService` (`@Service`: chuẩn hóa tên, định dạng phòng ban, sinh mã nhân viên), custom bean `ModelMapper` trong `@Configuration` (`AppConfig`), tiêm phụ thuộc bằng Constructor Injection.
- **Module 3 (Lab 3):** REST API với `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, `ResponseEntity`. (Bản in-memory ở Lab 3 được thay bằng dữ liệu thật từ DB ở Lab 4.)
- **Module 4 (Lab 4):** Spring Data JPA + MySQL: bảng `employee`, `department` (quan hệ Many-to-One qua `department_id`), CRUD, tìm kiếm theo tên hoặc phòng ban.
- **Module 5 (Lab 5):** Validation (`@NotBlank`, `@Size`, `@Email`, `@PositiveOrZero`, `@Valid`, `BindingResult`) và `@RestControllerAdvice` (404 Not Found, 400 khi dữ liệu sai định dạng, 409 khi trùng email).
- **Module 6 (Lab 6):** Thymeleaf MVC: `/employees/list`, `/employees/add`, `/employees/edit/{id}`, tìm kiếm theo tên/phòng ban.
- **Module 7 (Lab 7):** Logging SLF4J + Logback (console + file `logs/employee-management.log`), log khi thêm/sửa/xóa; cấu hình DB theo profile `dev` / `prod`.
- **Module 8 (Lab 8):** Actuator (`/actuator/health`, `/actuator/metrics`), `@Scheduled` log "System running" mỗi 30 giây, `@Cacheable` cho API tổng số nhân viên (TTL 1 phút, Caffeine).
- **Module 9 (Lab 9):** Spring Security: `User` entity (`ROLE_USER`, `ROLE_ADMIN`), đăng ký + đăng nhập (form web và JWT cho REST). USER chỉ được xem, ADMIN được CRUD.
- **Module 10 (Lab 10):** Thống kê bằng `@Query`: số nhân viên theo phòng ban, tổng số nhân viên, phân tích lương (TB / nhỏ nhất / lớn nhất); REST API + trang `/employees/statistics`.

## Yêu cầu môi trường
- Java 17+
- Maven Wrapper đi kèm (`mvnw`), không cần cài Maven


## Hướng dẫn chạy dự án
```bash
./mvnw clean test        
./mvnw spring-boot:run       
```

## Tài khoản mẫu mặc định
- **Admin**: `admin` /pass: `admin123` 
- **User**: `user` / pass:`user123` 

## Các đường dẫn Web UI (Thymeleaf)
- `http://localhost:8080/login`: Đăng nhập
- `http://localhost:8080/register`: Đăng ký
- `http://localhost:8080/employees/list`: Danh sách nhân viên + tìm kiếm
- `http://localhost:8080/employees/search?name=...&department=...`: Tìm kiếm theo tên / phòng ban
- `http://localhost:8080/employees/add`: Thêm nhân viên (ADMIN)
- `http://localhost:8080/employees/edit/{id}`: Sửa nhân viên (ADMIN)
- `http://localhost:8080/employees/statistics`: Thống kê

## Danh sách REST API
| Method | Endpoint | Quyền |
|---|---|---|
| GET | `/hello` | Công khai |
| POST | `/api/auth/register` | Công khai |
| POST | `/api/auth/login` | Công khai (trả JWT) |
| GET | `/actuator/health` | Công khai |
| GET | `/actuator/metrics` | ADMIN |
| GET | `/api/employees?name=&department=` | USER, ADMIN |
| GET | `/api/employees/{id}` | USER, ADMIN |
| POST | `/api/employees` | ADMIN |
| PUT | `/api/employees/{id}` | ADMIN |
| DELETE | `/api/employees/{id}` | ADMIN |
| GET | `/api/employees/count` | USER, ADMIN (cache 1 phút) |
| GET | `/api/employees/statistics` | USER, ADMIN |
| GET | `/api/employees/reports/summary` | USER, ADMIN |
| GET | `/api/employees/analytics/salary` | USER, ADMIN |





