package com.qfedu.smartcampusseckill.repository;

// 核心修正：替换为你项目实际的Course实体包路径
import com.qfedu.smartcampusseckill.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository // 标记为数据访问组件，确保Spring能扫描并创建Bean
public interface CourseRepository extends JpaRepository<Course, Long> {

    // 【高并发核心】乐观锁减库存
    // 只有当课程ID匹配且剩余库存>0时，才扣减1个库存
    // 返回值：受影响行数（1=扣减成功，0=库存不足）
    @Modifying // 标记为修改操作（UPDATE/DELETE），JPA必须加
    @Transactional // 开启事务，保证SQL执行原子性
    @Query("UPDATE Course c SET c.leftStock = c.leftStock - 1 WHERE c.id = :id AND c.leftStock > 0")
    int decreaseStock(Long id);
}