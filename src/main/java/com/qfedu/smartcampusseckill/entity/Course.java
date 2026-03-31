package com.qfedu.smartcampusseckill.entity;

// 核心修正：Spring Boot 2.x 用 javax.persistence 而非 jakarta.persistence
import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程实体，对应 PostgreSQL 中的 course 表。
 * 使用驼峰命名通过 JPA 策略自动映射为下划线字段（如 classTime -> class_time）。
 */
@Data
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 课程名称 */
    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    /** 授课教师 */
    private String teacherName;

    /** 课程分类，如“公共课”“专业课”等 */
    private String category;

    /** 学分 */
    private Double credit;

    /** 上课时间描述，如“周一 1-2 节” */
    private String classTime;

    /** 上课地点，如“教学楼 A201” */
    private String location;

    /** 课程简介 */
    private String description;

    /** 创建人（教务端录入人） */
    private String creator;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间（数据库也可通过触发器维护） */
    private LocalDateTime updateTime;

    /** 总名额 */
    private Integer stock;

    /** 剩余名额 */
    private Integer leftStock;

    /** 抢课开始时间 */
    private LocalDateTime startTime;

    /** 抢课结束时间 */
    private LocalDateTime endTime;

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