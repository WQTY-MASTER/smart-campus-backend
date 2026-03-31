package com.qfedu.smartcampusseckill.repository;

import com.qfedu.smartcampusseckill.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 学生表仓储。
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNo(String studentNo);

    Optional<Student> findByUsername(String username);
}

