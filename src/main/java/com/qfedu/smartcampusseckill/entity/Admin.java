package com.qfedu.smartcampusseckill.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 管理员账号表，对应 PostgreSQL 的 admin 表。
 */
@Data
@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 管理员用户名 */
    private String username;

    /** 管理员密码（数据库通常存 MD5 后的字符串） */
    private String password;

    /** 角色，例如 ADMIN */
    private String role;

    /** 创建时间 */
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}

