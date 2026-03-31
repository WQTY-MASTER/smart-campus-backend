package com.qfedu.smartcampusseckill.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.entity.Student;
import com.qfedu.smartcampusseckill.entity.StudentCourse;
import com.qfedu.smartcampusseckill.repository.CourseRepository;
import com.qfedu.smartcampusseckill.repository.StudentRepository;
import com.qfedu.smartcampusseckill.repository.StudentCourseRepository;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理端：导出选课名单（Excel）。
 *
 * 说明：当前项目未接入 Student 实体，因此导出内容以 student_id 为主；
 * 如需导出学生姓名/学院/班级等信息，可在后续补齐 Student 实体与联表查询。
 */
@Service
public class AdminSelectionExportService {

    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public AdminSelectionExportService(StudentCourseRepository studentCourseRepository,
                                       CourseRepository courseRepository,
                                       StudentRepository studentRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 按课程导出已选名单（status=1）。
     */
    public void exportByCourse(Long courseId, HttpServletResponse response) throws Exception {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        // 统一使用一个导出时间，避免每行不一致
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String exportTime = LocalDateTime.now().format(fmt);

        List<StudentCourse> records = studentCourseRepository.findByCourseIdAndStatus(courseId, 1);
        List<SelectionRow> rows = new ArrayList<>();

        // 批量加载学生信息，避免循环查库
        Map<Long, Student> studentMap = new HashMap<>();
        if (!records.isEmpty()) {
            List<Long> studentIds = new ArrayList<>();
            for (StudentCourse sc : records) {
                if (sc.getStudentId() != null) {
                    studentIds.add(sc.getStudentId());
                }
            }
            for (Student s : studentRepository.findAllById(studentIds)) {
                studentMap.put(s.getId(), s);
            }
        }

        for (StudentCourse sc : records) {
            SelectionRow row = new SelectionRow();
            row.setCourseId(courseId);
            row.setCourseName(course.getCourseName());
            row.setCategory(safe(course.getCategory()));
            row.setTeacherName(safe(course.getTeacherName()));
            Student stu = studentMap.get(sc.getStudentId());
            row.setStudentId(sc.getStudentId());
            row.setStudentNo(stu == null ? "" : safe(stu.getStudentNo()));
            row.setStudentName(stu == null ? "" : safe(stu.getUsername()));
            row.setCollege(stu == null ? "" : safe(stu.getCollege()));
            row.setMajor(stu == null ? "" : safe(stu.getMajor()));
            row.setClazz(stu == null ? "" : safe(stu.getClazz()));
            row.setStatus(sc.getStatus());
            row.setSelectionTime(sc.getSelectionTime() == null ? "" : sc.getSelectionTime().format(fmt));
            row.setExportTime(exportTime);
            rows.add(row);
        }

        String fileName = URLEncoder.encode("选课名单-" + course.getCourseName(), StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), SelectionRow.class)
                .sheet("选课名单")
                .doWrite(rows);
    }

    /**
     * Excel 行模型。
     */
    public static class SelectionRow {
        @ExcelProperty("课程ID")
        private Long courseId;

        @ExcelProperty("课程名称")
        private String courseName;

        @ExcelProperty("课程分类")
        private String category;

        @ExcelProperty("授课教师")
        private String teacherName;

        @ExcelProperty("学生ID(内部)")
        private Long studentId;

        @ExcelProperty("学号")
        private String studentNo;

        @ExcelProperty("姓名")
        private String studentName;

        @ExcelProperty("学院")
        private String college;

        @ExcelProperty("专业")
        private String major;

        @ExcelProperty("班级")
        private String clazz;

        @ExcelProperty("选课状态")
        private Integer status;

        @ExcelProperty("选课时间")
        private String selectionTime;

        @ExcelProperty("导出时间")
        private String exportTime;

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        public String getClazz() { return clazz; }
        public void setClazz(String clazz) { this.clazz = clazz; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }

        public String getSelectionTime() {
            return selectionTime;
        }

        public void setSelectionTime(String selectionTime) {
            this.selectionTime = selectionTime;
        }

        public String getExportTime() {
            return exportTime;
        }

        public void setExportTime(String exportTime) {
            this.exportTime = exportTime;
        }
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}

