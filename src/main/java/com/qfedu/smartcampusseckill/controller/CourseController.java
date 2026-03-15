// com.qfedu.smartcampusseckill.controller.CourseController (修改后)
package com.qfedu.smartcampusseckill.controller;

// 导入 Result 类
import com.qfedu.smartcampusseckill.common.Result;
import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允许前端跨域访问
public class CourseController {

    // 重点：移除字段注入，使用构造器注入
    private final CourseService courseService;

    // 构造器注入（Spring 推荐的规范方式）
    // Spring 会自动扫描到这个构造函数，并注入 CourseService 的实例
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 获取课程列表
     * 返回统一格式的 Result 对象
     */
    @GetMapping("/courses")
    public Result<List<Course>> list() {
        List<Course> courseList = courseService.findAllCourses();
        // 使用 Result.success() 包装数据
        return Result.success(courseList);
    }

    /**
     * 抢课接口（POST 请求）
     * 返回统一格式的 Result 对象
     */
    @PostMapping("/seckill")
    public Result<String> seckill(@RequestParam Long studentId, @RequestParam Long courseId) {
        try {
            String msg = courseService.selectCourse(studentId, courseId);
            // 根据业务逻辑返回不同的 Result
            if ("抢课成功".equals(msg)) {
                return Result.success(msg); // 成功时包装成功消息
            } else {
                return Result.error(msg); // 失败时包装错误消息
            }
        } catch (Exception e) {
            // 捕获未知异常，返回通用的错误提示
            return Result.error("系统繁忙，请重试：" + e.getMessage()); // 可以在生产环境中去掉 e.getMessage()
        }
    }
}