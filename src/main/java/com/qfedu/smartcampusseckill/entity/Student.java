package com.qfedu.smartcampusseckill.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 学生表，对应 PostgreSQL 的 student。
 * 约定：对外使用 studentNo（学号）作为登录/导出展示主键；id 仍作为内部主键。
 */
@Data
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录账号（可保留用于兼容），唯一 */
    private String username;

    /** 登录密码（建议存哈希，当前项目兼容明文） */
    private String password;

    /** 学号（唯一），用于登录与导出展示 */
    private String studentNo;

    private String college;
    private String major;
    private String clazz;

    /** 状态：1=正常，0=禁用 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

