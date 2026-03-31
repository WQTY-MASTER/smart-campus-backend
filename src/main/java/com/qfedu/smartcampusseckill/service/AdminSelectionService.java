package com.qfedu.smartcampusseckill.service;

import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.repository.CourseRepository;
import com.qfedu.smartcampusseckill.repository.StudentCourseRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 管理端选课统计服务。
 */
@Service
public class AdminSelectionService {

    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;

    public AdminSelectionService(StudentCourseRepository studentCourseRepository,
                                 CourseRepository courseRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * 统计所有课程的选课人数与选报率。
     */
    public List<CourseStat> stats() {
        List<Object[]> raw = studentCourseRepository.countSelectedByCourse();
        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : raw) {
            if (row == null || row.length < 2) continue;
            Long courseId = ((Number) row[0]).longValue();
            Long cnt = ((Number) row[1]).longValue();
            countMap.put(courseId, cnt);
        }

        List<Course> courses = courseRepository.findAll();
        List<CourseStat> result = new ArrayList<>();
        for (Course c : courses) {
            CourseStat stat = new CourseStat();
            stat.setCourseId(c.getId());
            stat.setCourseName(c.getCourseName());
            stat.setTeacherName(c.getTeacherName());
            stat.setStock(c.getStock() == null ? 0 : c.getStock());
            long selected = countMap.getOrDefault(c.getId(), 0L);
            stat.setSelectedCount(selected);
            if (stat.getStock() > 0) {
                stat.setSelectedRate(selected * 1.0 / stat.getStock());
            } else {
                stat.setSelectedRate(0.0);
            }
            result.add(stat);
        }
        return result;
    }

    /**
     * 统计 DTO。
     */
    @Data
    public static class CourseStat {
        private Long courseId;
        private String courseName;
        private String teacherName;
        private Integer stock;
        private Long selectedCount;
        /** 0~1 */
        private Double selectedRate;
    }
}

