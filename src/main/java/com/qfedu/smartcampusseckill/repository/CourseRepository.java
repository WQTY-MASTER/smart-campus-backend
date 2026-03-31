package com.qfedu.smartcampusseckill.repository;

// 核心修正：替换为你项目实际的Course实体包路径
import com.qfedu.smartcampusseckill.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 课程仓储接口，对应 course 表。
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, CourseRepositoryCustom {

    /**
     * 乐观锁减库存：
     * 在剩余名额大于 0 的前提下，将 leftStock 减 1。
     *
     * @param courseId 课程 ID
     * @return 受影响的行数（1=成功，0=库存不足）
     */
    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.leftStock = c.leftStock - 1 WHERE c.id = :courseId AND c.leftStock > 0")
    int reduceStock(@Param("courseId") Long courseId);

    /**
     * 退课时恢复名额：将 leftStock 加 1。
     *
     * @param courseId 课程 ID
     * @return 受影响的行数（1=成功）
     */
    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.leftStock = c.leftStock + 1 WHERE c.id = :courseId")
    int increaseStock(@Param("courseId") Long courseId);

    /**
     * 已有课程分类去重列表（管理端下拉框用）。
     */
    @Query("SELECT DISTINCT c.category FROM Course c " +
            "WHERE c.category IS NOT NULL AND TRIM(c.category) <> '' ORDER BY c.category")
    List<String> findDistinctCategories();
}