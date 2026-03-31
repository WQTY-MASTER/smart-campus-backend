package com.qfedu.smartcampusseckill.service;

import com.qfedu.smartcampusseckill.entity.Student;
import com.qfedu.smartcampusseckill.repository.StudentRepository;
import com.qfedu.smartcampusseckill.util.Md5Util;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 学生端登录服务：使用 学号 + 密码 登录。
 * 真实姓名用于展示/导出，从 student.username 读取（你的库里 username 存真实姓名）。
 */
@Service
public class StudentAuthService {

    private final StudentRepository studentRepository;

    public StudentAuthService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public LoginResult login(String studentNo, String passwordPlain) {
        String no = trimToNull(studentNo);
        String pw = passwordPlain == null ? "" : passwordPlain;

        if (no == null) {
            throw new IllegalArgumentException("学号不能为空");
        }

        Student student = studentRepository.findByStudentNo(no)
                .orElseThrow(() -> new IllegalArgumentException("学号或密码错误"));

        Integer status = student.getStatus();
        if (status != null && status == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        String stored = student.getPassword() == null ? "" : student.getPassword().trim();
        String inputMd5 = Md5Util.md5(pw);
        boolean ok = stored.equalsIgnoreCase(inputMd5) || stored.equals(pw);
        if (!ok) {
            throw new IllegalArgumentException("学号或密码错误");
        }

        LoginResult result = new LoginResult();
        result.setToken(UUID.randomUUID().toString().replace("-", ""));
        result.setStudentId(student.getId());
        result.setStudentNo(student.getStudentNo());
        result.setName(student.getUsername()); // 真实姓名
        return result;
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }

    public static class LoginResult {
        private String token;
        private Long studentId;
        private String studentNo;
        private String name;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}

