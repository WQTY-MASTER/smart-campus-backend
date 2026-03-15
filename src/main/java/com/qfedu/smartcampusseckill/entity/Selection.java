package com.qfedu.smartcampusseckill.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "selection")
public class Selection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId; // 学生ID
    private Long courseId; // 课程ID
    private LocalDateTime selectionTime; // 选课时间
}