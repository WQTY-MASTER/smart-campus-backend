package com.qfedu.smartcampusseckill.repository;

import com.qfedu.smartcampusseckill.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * {@link CourseRepository} 扩展：管理端分页等需动态条件、避免原生 @Query 重复命名参数绑定错位的场景。
 */
public interface CourseRepositoryCustom {

    Page<Course> pageQuery(String category, Integer leftStockGte, String courseName, Pageable pageable);
}
