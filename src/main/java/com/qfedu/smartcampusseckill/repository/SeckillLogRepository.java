package com.qfedu.smartcampusseckill.repository;

import com.qfedu.smartcampusseckill.entity.SeckillLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 抢课日志仓储接口，对应 seckill_log 表。
 */
@Repository
public interface SeckillLogRepository extends JpaRepository<SeckillLog, Long> {
}

