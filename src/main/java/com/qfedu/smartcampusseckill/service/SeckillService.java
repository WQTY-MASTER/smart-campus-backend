package com.qfedu.smartcampusseckill.service;

import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.entity.SeckillLog;
import com.qfedu.smartcampusseckill.entity.StudentCourse;
import com.qfedu.smartcampusseckill.repository.CourseRepository;
import com.qfedu.smartcampusseckill.repository.SeckillLogRepository;
import com.qfedu.smartcampusseckill.repository.StudentRepository;
import com.qfedu.smartcampusseckill.repository.StudentCourseRepository;
import com.qfedu.smartcampusseckill.util.AgentDebugLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qfedu.smartcampusseckill.service.CourseService.COURSE_LIST_KEY;

/**
 * 抢课服务：封装抢课业务流程、库存扣减、日志记录以及“已选课程”查询。
 */
@Service
public class SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SeckillLogRepository seckillLogRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SeckillConfigService seckillConfigService;

    /**
     * 抢课主流程。
     *
     * @param studentId 学生 ID
     * @param courseId  课程 ID
     * @return 抢课结果文案
     */
    @Transactional
    public String seckill(Long studentId, Long courseId) {
        // #region agent log
        AgentDebugLogger.log(
                "initial",
                "H5",
                "SeckillService.seckill:entry",
                "seckill entry",
                kv("studentId", studentId, "courseId", courseId, "thread", Thread.currentThread().getName())
        );
        // #endregion
        // 0. 校验全局抢课时间与开关
        if (!seckillConfigService.isSeckillAllowedNow()) {
            saveLog(studentId, courseId, 0, "当前不在抢课时间或抢课功能已关闭");
            return "当前不在抢课时间或抢课功能已关闭";
        }

        // 1. 重复选课校验
        if (studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            saveLog(studentId, courseId, 0, "已选该课程");
            return "已选该课程";
        }

        // 2. 乐观锁扣减库存
        int rows = courseRepository.reduceStock(courseId);
        if (rows <= 0) {
            // 扣减失败说明库存不足
            saveLog(studentId, courseId, 0, "库存不足");
            return "名额已满";
        }

        // 3. 生成学生选课记录
        StudentCourse record = new StudentCourse();
        record.setStudentId(studentId);
        record.setCourseId(courseId);
        record.setStatus(1);
        record.setSelectionTime(LocalDateTime.now());
        studentCourseRepository.save(record);

        // 4. 删除课程列表缓存，保证前端剩余名额展示最新
        evictCourseListCache();

        // 5. 写入成功日志
        saveLog(studentId, courseId, 1, "选课成功");
        return "选课成功！";
    }

    /**
     * 抢课（对外学号版）：前端用 studentNo（学号）调用，后端解析成 studentId。
     */
    @Transactional
    public String seckillByStudentNo(String studentNo, Long courseId) {
        Long studentId = studentRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new IllegalArgumentException("学号不存在"))
                .getId();
        return seckill(studentId, courseId);
    }

    /**
     * 查询某个学生已选课程（返回课程详情列表）。
     */
    @Transactional(readOnly = true)
    public List<Course> getSelectedCourses(Long studentId) {
        // #region agent log
        AgentDebugLogger.log(
                "initial",
                "H2",
                "SeckillService.getSelectedCourses:entry",
                "enter getSelectedCourses",
                kv("studentId", studentId, "thread", Thread.currentThread().getName())
        );
        // #endregion
        List<StudentCourse> records = studentCourseRepository.findByStudentId(studentId);
        // #region agent log
        AgentDebugLogger.log(
                "initial",
                "H3",
                "SeckillService.getSelectedCourses:afterFindByStudentId",
                "loaded student_course records",
                kv("studentId", studentId, "recordCount", records.size())
        );
        // #endregion
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> courseIds = records.stream()
                .map(StudentCourse::getCourseId)
                .collect(Collectors.toList());
        List<Course> courses = courseRepository.findAllById(courseIds);
        // #region agent log
        AgentDebugLogger.log(
                "initial",
                "H4",
                "SeckillService.getSelectedCourses:exit",
                "return selected courses",
                kv("studentId", studentId, "courseIdCount", courseIds.size(), "courseCount", courses.size())
        );
        // #endregion
        return courses;
    }

    /**
     * 查询已选课程（对外学号版）。
     */
    @Transactional(readOnly = true)
    public List<Course> getSelectedCoursesByStudentNo(String studentNo) {
        Long studentId = studentRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new IllegalArgumentException("学号不存在"))
                .getId();
        return getSelectedCourses(studentId);
    }

    /**
     * 退课：删除选课记录并恢复课程名额。
     *
     * @param studentId 学生 ID
     * @param courseId  课程 ID
     * @return 退课结果文案
     */
    @Transactional
    public String dropCourse(Long studentId, Long courseId) {
        if (!studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            saveLog(studentId, courseId, 0, "未选该课程");
            return "未选该课程";
        }
        if (!seckillConfigService.isSeckillSwitchOn()) {
            saveLog(studentId, courseId, 0, "抢课功能已关闭，暂不支持退课");
            return "抢课功能已关闭，暂不支持退课";
        }
        if (!seckillConfigService.isDropAllowedNow()) {
            saveLog(studentId, courseId, 0, "已超过退课截止时间");
            return "已超过退课截止时间";
        }
        studentCourseRepository.deleteByStudentIdAndCourseId(studentId, courseId);
        courseRepository.increaseStock(courseId);
        evictCourseListCache();
        saveLog(studentId, courseId, 0, "退课成功");
        return "退课成功";
    }

    /**
     * 退课（对外学号版）。
     */
    @Transactional
    public String dropCourseByStudentNo(String studentNo, Long courseId) {
        Long studentId = studentRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new IllegalArgumentException("学号不存在"))
                .getId();
        return dropCourse(studentId, courseId);
    }

    private void evictCourseListCache() {
        try {
            redisTemplate.delete(COURSE_LIST_KEY);
        } catch (Exception e) {
            log.warn("Redis 删除课程列表缓存失败（已忽略）: {}", e.toString());
        }
    }

    /**
     * 统一日志落库方法。
     */
    private void saveLog(Long studentId, Long courseId, Integer status, String message) {
        SeckillLog row = new SeckillLog();
        row.setStudentId(studentId);
        row.setCourseId(courseId);
        row.setStatus(status);
        row.setMessage(message);
        seckillLogRepository.save(row);
    }

    private Map<String, Object> kv(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }
}

