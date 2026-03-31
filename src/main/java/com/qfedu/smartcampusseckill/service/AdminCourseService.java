package com.qfedu.smartcampusseckill.service;

import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

/**
 * 管理端课程服务：课程录入/修改/分页查询，并同步 Redis 单课程缓存。
 */
@Service
public class AdminCourseService {

    private static final Logger log = LoggerFactory.getLogger(AdminCourseService.class);

    private final CourseRepository courseRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public AdminCourseService(CourseRepository courseRepository,
                              RedisTemplate<String, Object> redisTemplate) {
        this.courseRepository = courseRepository;
        this.redisTemplate = redisTemplate;
    }

    private String courseKey(Long id) {
        return "course:" + id;
    }

    private void cacheCourseSafe(Course course) {
        try {
            redisTemplate.opsForValue().set(courseKey(course.getId()), course, Duration.ofDays(7));
        } catch (Exception e) {
            log.warn("Redis 缓存单课失败（已忽略）: {}", e.toString());
        }
    }

    /**
     * 新增课程并预热缓存（7天）。
     */
    @Transactional
    public Course create(Course course) {
        Course saved = courseRepository.save(course);
        cacheCourseSafe(saved);
        return saved;
    }

    /**
     * 修改课程并同步缓存（7天）。
     */
    @Transactional
    public Course update(Long id, Course update) {
        Course db = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        db.setCourseName(update.getCourseName());
        db.setTeacherName(update.getTeacherName());
        db.setCategory(update.getCategory());
        db.setCredit(update.getCredit());
        db.setClassTime(update.getClassTime());
        db.setLocation(update.getLocation());
        db.setDescription(update.getDescription());
        db.setStock(update.getStock());
        db.setLeftStock(update.getLeftStock());
        db.setStartTime(update.getStartTime());
        db.setEndTime(update.getEndTime());
        db.setCreator(update.getCreator());

        Course saved = courseRepository.save(db);
        cacheCourseSafe(saved);
        return saved;
    }

    /**
     * 分页查询课程，可按课程名（模糊）、分类（精确）、最小剩余名额筛选。
     */
    public Page<Course> page(int page, int size, String courseName, String category, Integer leftStockGte) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String c = (category == null || category.trim().isEmpty()) ? null : category.trim();
        String name = (courseName == null || courseName.trim().isEmpty()) ? null : courseName.trim();
        return courseRepository.pageQuery(c, leftStockGte, name, pageable);
    }

    /**
     * 下拉框：返回数据库中已出现过的课程分类（去重排序）。
     */
    public List<String> listDistinctCategories() {
        return courseRepository.findDistinctCategories();
    }
}

