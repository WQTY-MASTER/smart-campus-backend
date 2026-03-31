package com.qfedu.smartcampusseckill.service;

import com.qfedu.smartcampusseckill.entity.Admin;
import com.qfedu.smartcampusseckill.repository.AdminRepository;
import com.qfedu.smartcampusseckill.util.Md5Util;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 管理员登录服务。
 */
@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;

    public AdminAuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * 登录校验，返回 token。
     * 兼容两种情况：库里存 MD5 / 库里存明文（便于迁移阶段排查）。
     */
    public String login(String username, String passwordPlain) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));

        String stored = admin.getPassword() == null ? "" : admin.getPassword().trim();
        String inputMd5 = Md5Util.md5(passwordPlain == null ? "" : passwordPlain);

        boolean ok = stored.equalsIgnoreCase(inputMd5) || stored.equals(passwordPlain);
        if (!ok) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        return UUID.randomUUID().toString().replace("-", "");
    }
}

