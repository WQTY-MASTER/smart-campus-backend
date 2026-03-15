package com.qfedu.smartcampusseckill.service;

import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.entity.Selection;
import com.qfedu.smartcampusseckill.repository.CourseRepository;
import com.qfedu.smartcampusseckill.repository.SelectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SelectionRepository selectionRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 定义缓存 Key
    private static final String COURSE_LIST_KEY = "course_list";

    /**
     * 获取课程列表 (高并发优化版)
     * 策略：Cache Aside Pattern (旁路缓存)
     */
    public List<Course> findAllCourses() {
        // 1. 先查 Redis
        List<Course> courseList = (List<Course>) redisTemplate.opsForValue().get(COURSE_LIST_KEY);

        if (courseList != null && !courseList.isEmpty()) {
            System.out.println("查询 Redis 缓存命中！");
            return courseList;
        }

        // 2. Redis 没有，查数据库
        System.out.println("查询数据库...");
        courseList = courseRepository.findAll();

        // 3. 写入 Redis (设置过期时间 60 秒，防止数据一直不更新)
        // 这里的 60秒 是为了让选课期间的列表能稍微更新一下，或者你可以配合管理员接口删除缓存
        redisTemplate.opsForValue().set(COURSE_LIST_KEY, courseList, 60, TimeUnit.SECONDS);

        return courseList;
    }

    /**
     * 抢课逻辑 (数据库乐观锁)
     */
    @Transactional
    public String selectCourse(Long studentId, Long courseId) {
        // 1. 简单校验
        if (selectionRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            return "您已经选过这门课了";
        }

        // 2. 扣减库存 (乐观锁)
        int rows = courseRepository.decreaseStock(courseId);

        if (rows > 0) {
            // 3. 生成记录
            Selection selection = new Selection();
            selection.setStudentId(studentId);
            selection.setCourseId(courseId);
            selection.setSelectionTime(LocalDateTime.now());
            selectionRepository.save(selection);

            // 【重要】因为库存变了，为了保证列表显示的剩余名额准确，这里应该删除缓存
            // 下次有人查列表时，就会重新从数据库加载最新库存
            redisTemplate.delete(COURSE_LIST_KEY);

            return "抢课成功";
        } else {
            return "名额已满";
        }
    }
}