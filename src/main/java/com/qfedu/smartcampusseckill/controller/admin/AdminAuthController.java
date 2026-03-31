package com.qfedu.smartcampusseckill.controller.admin;

import com.qfedu.smartcampusseckill.common.Result;
import com.qfedu.smartcampusseckill.service.AdminAuthService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端登录接口。
 */
@RestController
@RequestMapping("/admin/auth")
@CrossOrigin(origins = "*")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 登录验证：POST /admin/auth/login
     * 请求体：{ "username": "...", "password": "..." }
     * 返回：{ code/msg/data: { token: "..." } }
     */
    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody LoginReq req) {
        String token = adminAuthService.login(req.getUsername(), req.getPassword());
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        return Result.success(resp);
    }

    @Data
    public static class LoginReq {
        private String username;
        private String password;
    }

    @Data
    public static class LoginResp {
        private String token;
    }
}

