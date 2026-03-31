package com.qfedu.smartcampusseckill.repository;

import com.qfedu.smartcampusseckill.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学生选课记录仓储接口，对应 student_course 表。
 */

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    /**
     * 判断某个学生是否已经选过某门课程。
     */
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * 查询某个学生的所有选课记录。
     */
    java.util.List<StudentCourse> findByStudentId(Long studentId);

    /**
     * 按学生 ID 和课程 ID 删除选课记录（退课用）。
     */
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * 查询某门课程的已选记录（status=1）。
     */
    List<StudentCourse> findByCourseIdAndStatus(Long courseId, Integer status);

    /**
     * 统计：每门课程的已选人数（status=1）。
     * 返回 Object[]: [0]=courseId, [1]=count
     */
    @Query("SELECT sc.courseId, COUNT(sc.id) FROM StudentCourse sc WHERE sc.status = 1 GROUP BY sc.courseId")
    List<Object[]> countSelectedByCourse();
}
