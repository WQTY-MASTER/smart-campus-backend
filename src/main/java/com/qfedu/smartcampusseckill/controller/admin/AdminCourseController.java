package com.qfedu.smartcampusseckill.controller.admin;

import com.qfedu.smartcampusseckill.common.Result;
import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.service.AdminCourseService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端：课程管理接口。
 */
@RestController
@RequestMapping("/admin/courses")
@CrossOrigin(origins = "*")
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    public AdminCourseController(AdminCourseService adminCourseService) {
        this.adminCourseService = adminCourseService;
    }

    /**
     * 课程分类下拉数据：GET /admin/courses/categories
     */
    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(adminCourseService.listDistinctCategories());
    }

    /**
     * 分页查询课程：
     * GET /admin/courses?page=0&size=10&courseName=...&category=...&leftStockGte=...
     */
    @GetMapping
    public Result<Page<Course>> page(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String courseName,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) Integer leftStockGte) {
        return Result.success(adminCourseService.page(page, size, courseName, category, leftStockGte));
    }

    /**
     * 新增课程：POST /admin/courses
     */
    @PostMapping
    public Result<Course> create(@RequestBody Course course) {
        return Result.success("新增成功", adminCourseService.create(course));
    }

    /**
     * 修改课程：PUT /admin/courses/{id}
     */
    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Long id, @RequestBody Course course) {
        return Result.success("修改成功", adminCourseService.update(id, course));
    }
}

