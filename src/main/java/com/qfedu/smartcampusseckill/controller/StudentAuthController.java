package com.qfedu.smartcampusseckill.controller;

import com.qfedu.smartcampusseckill.common.Result;
import com.qfedu.smartcampusseckill.service.StudentAuthService;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端登录接口：学号 + 密码（真实姓名由后端返回）。
 */
@RestController
@CrossOrigin(origins = "*")
public class StudentAuthController {

    private final StudentAuthService studentAuthService;

    public StudentAuthController(StudentAuthService studentAuthService) {
        this.studentAuthService = studentAuthService;
    }

    /**
     * 学生登录（推荐）：POST /api/student/login
     * 请求体：{ "studentNo": "...", "password": "..." }
     */
    @PostMapping("/api/student/login")
    public Result<StudentAuthService.LoginResult> login(@RequestBody LoginReq req) {
        return Result.success(studentAuthService.login(req.getStudentNo(), req.getPassword()));
    }

    /**
     * 兼容路径：POST /api/auth/login
     * 便于前端如果已写死该路由，不用改太多。
     */
    @PostMapping("/api/auth/login")
    public Result<StudentAuthService.LoginResult> loginAlias(@RequestBody LoginReq req) {
        return Result.success(studentAuthService.login(req.getStudentNo(), req.getPassword()));
    }

    public static class LoginReq {
        private String studentNo;
        private String password;

        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}

