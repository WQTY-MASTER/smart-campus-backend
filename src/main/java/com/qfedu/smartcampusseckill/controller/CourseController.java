// com.qfedu.smartcampusseckill.controller.CourseController (修改后)
package com.qfedu.smartcampusseckill.controller;

// 导入 Result 类
import com.qfedu.smartcampusseckill.common.Result;
import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.service.CourseService;
import com.qfedu.smartcampusseckill.service.SeckillConfigService;
import com.qfedu.smartcampusseckill.service.SeckillService;
import com.qfedu.smartcampusseckill.util.AgentDebugLogger;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允许前端跨域访问
public class CourseController {

    private final CourseService courseService;
    private final SeckillService seckillService;
    private final SeckillConfigService seckillConfigService;

    public CourseController(CourseService courseService,
                            SeckillService seckillService,
                            SeckillConfigService seckillConfigService) {
        this.courseService = courseService;
        this.seckillService = seckillService;
        this.seckillConfigService = seckillConfigService;
    }

    /**
     * 学生端只读：抢课全局配置（与管理端同源）。
     * 字段：seckill_global_start、seckill_global_end、seckill_switch、drop_deadline，
     * 以及驼峰别名 seckillGlobalStart、seckillGlobalEnd（与下划线键同值）；时间为 yyyy-MM-dd HH:mm:ss。
     */
    @GetMapping("/config/seckill")
    public Result<Map<String, String>> seckillConfigForStudent() {
        return Result.success(seckillConfigService.getSeckillConfig());
    }

    /**
     * 学生端：课程分类去重列表（筛选「类型」下拉数据源）。与筛选结果无关，避免筛一次后另一种类型从下拉消失。
     */
    @GetMapping("/courses/categories")
    public Result<List<String>> courseCategories() {
        return Result.success(courseService.listDistinctCategories());
    }

    /**
     * 获取课程列表。可选查询参数（与前端本地筛选对齐，大数据量时可改走服务端）：
     * category、creditGte、leftStockGte、classTimeKeyword（上课时间包含匹配，忽略大小写）。
     */
    @GetMapping("/courses")
    public Result<List<Course>> list(HttpServletRequest request,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) Double creditGte,
                                     @RequestParam(required = false) Integer leftStockGte,
                                     @RequestParam(required = false) String classTimeKeyword) {
        // #region agent log
        AgentDebugLogger.log(
                "initial",
                "H1",
                "CourseController.list",
                "courses endpoint hit",
                kv("uri", request.getRequestURI(), "thread", Thread.currentThread().getName())
        );
        // #endregion
        List<Course> courseList = courseService.findCoursesForStudent(
                category, creditGte, leftStockGte, classTimeKeyword);
        return Result.success(courseList);
    }

    /**
     * 抢课接口（POST 请求），内部调用 SeckillService.seckill 完成业务逻辑和日志记录。
     */
    @PostMapping("/seckill")
    public Result<String> seckill(@RequestParam(required = false) Long studentId,
                                  @RequestParam(required = false) String studentNo,
                                  @RequestParam Long courseId) {
        try {
            String msg;
            if (studentId != null) {
                msg = seckillService.seckill(studentId, courseId);
            } else if (studentNo != null && !studentNo.trim().isEmpty()) {
                msg = seckillService.seckillByStudentNo(studentNo.trim(), courseId);
            } else {
                return Result.error("缺少 studentId 或 studentNo");
            }
            if ("选课成功！".equals(msg)) {
                return Result.success(msg);
            } else {
                return Result.error(msg);
            }
        } catch (Exception e) {
            return Result.error("系统繁忙，请重试");
        }
    }

    /**
     * 查询某个学生已选课程列表。
     */
    @GetMapping("/my-courses")
    public Result<List<Course>> myCourses(@RequestParam(required = false) Long studentId,
                                          @RequestParam(required = false) String studentNo,
                                          HttpServletRequest request) {
        // #region agent log
        AgentDebugLogger.log(
                "initial",
                "H1",
                "CourseController.myCourses",
                "my-courses endpoint hit",
                kv("uri", request.getRequestURI(), "studentId", studentId, "thread", Thread.currentThread().getName())
        );
        // #endregion
        List<Course> selected;
        if (studentId != null) {
            selected = seckillService.getSelectedCourses(studentId);
        } else if (studentNo != null && !studentNo.trim().isEmpty()) {
            selected = seckillService.getSelectedCoursesByStudentNo(studentNo.trim());
        } else {
            return Result.error("缺少 studentId 或 studentNo");
        }
        return Result.success(selected);
    }

    /**
     * 退课接口（POST）：删除选课记录并恢复课程名额。
     */
    @PostMapping("/drop")
    public Result<String> drop(@RequestParam(required = false) Long studentId,
                               @RequestParam(required = false) String studentNo,
                               @RequestParam Long courseId) {
        try {
            String msg;
            if (studentId != null) {
                msg = seckillService.dropCourse(studentId, courseId);
            } else if (studentNo != null && !studentNo.trim().isEmpty()) {
                msg = seckillService.dropCourseByStudentNo(studentNo.trim(), courseId);
            } else {
                return Result.error("缺少 studentId 或 studentNo");
            }
            if ("退课成功".equals(msg)) {
                return Result.success(msg);
            }
            return Result.error(msg);
        } catch (Exception e) {
            return Result.error("系统繁忙，请重试");
        }
    }

    private Map<String, Object> kv(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }
}