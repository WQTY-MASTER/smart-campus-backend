package com.qfedu.smartcampusseckill.repository;

import com.qfedu.smartcampusseckill.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 管理员账号仓储接口。
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);
}

