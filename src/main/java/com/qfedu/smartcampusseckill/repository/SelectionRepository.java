package com.qfedu.smartcampusseckill.repository;

import com.qfedu.smartcampusseckill.entity.Selection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelectionRepository extends JpaRepository<Selection, Long> {
    // 可以在这里加一个查询方法，用于后续检查用户是否已选过
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}