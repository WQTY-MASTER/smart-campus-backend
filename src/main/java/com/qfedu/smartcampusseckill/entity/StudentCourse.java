package com.qfedu.smartcampusseckill.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生选课记录，对应 PostgreSQL 中的 student_course 表。
 * 通过唯一约束保证同一学生对同一课程只能选一次。
 */
@Data
@Entity
@Table(
        name = "student_course",
        uniqueConstraints = @UniqueConstraint(columnNames = {"studentId", "courseId"})
)
public class StudentCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 学生 ID */
    private Long studentId;

    /** 课程 ID */
    private Long courseId;

    /**
     * 状态：1=成功，0=失败/取消等。
     * 默认 1，表示正常选课成功。
     */
    private Integer status = 1;

    /** 选课时间 */
    private LocalDateTime selectionTime;
}

