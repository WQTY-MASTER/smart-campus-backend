package com.qfedu.smartcampusseckill.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统全局配置表，对应 PostgreSQL 的 system_config。
 */
@Data
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 配置键，例如 seckill_global_start / seckill_global_end / seckill_switch */
    private String configKey;

    /** 配置值，统一用字符串保存 */
    private String configValue;

    /** 配置描述 */
    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createTime == null) {
            this.createTime = now;
        }
        if (this.updateTime == null) {
            this.updateTime = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}

