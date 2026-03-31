package com.qfedu.smartcampusseckill.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 抢课日志记录表，对应 PostgreSQL 中的 seckill_log 表。
 * 每次抢课（无论成功或失败）都会写一条记录，便于审计和问题排查。
 */
@Data
@Entity
@Table(name = "seckill_log")
public class SeckillLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 学生 ID */
    private Long studentId;

    /** 课程 ID */
    private Long courseId;

    /**
     * 状态：1=成功，0=失败。
     */
    private Integer status;

    /**
     * 结果描述，例如“抢课成功”“库存不足”“已选该课程”等。
     */
    private String message;

    /** 日志创建时间 */
    private LocalDateTime createTime;

    /**
     * 在持久化之前自动填充创建时间。
     */
    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}

