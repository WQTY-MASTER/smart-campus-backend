package com.qfedu.smartcampusseckill.entity;

// 核心修正：Spring Boot 2.x 用 javax.persistence 而非 jakarta.persistence
import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Lombok注解：自动生成get/set等方法
@Entity // 标记为JPA实体，对应数据库表
@Table(name = "course") // 指定映射的数据库表名
public class Course {
    @Id // 主键注解
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL自增主键
    private Long id;

    private String courseName; // 课程名称
    private String teacherName; // 授课老师
    private Integer stock; // 总库存
    private Integer leftStock; // 剩余库存

    private LocalDateTime startTime; // 秒杀开始时间
    private LocalDateTime endTime; // 秒杀结束时间
}