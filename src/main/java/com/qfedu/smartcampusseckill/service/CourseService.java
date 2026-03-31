package com.qfedu.smartcampusseckill.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qfedu.smartcampusseckill.entity.Course;
import com.qfedu.smartcampusseckill.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 课程查询相关服务，主要负责课程列表及缓存处理。
 */
@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 课程列表缓存 Key。
     */
    public static final String COURSE_LIST_KEY = "course_list";

    /**
     * 全库课程分类去重（学生端筛选下拉用）。勿用筛选后的课程列表推导分类，否则筛完下拉只剩当前类。
     */
    @Transactional(readOnly = true)
    public List<String> listDistinctCategories() {
        return courseRepository.findDistinctCategories();
    }

    /**
     * 获取课程列表（旁路缓存策略）。
     * <p>注意：Redis 使用 JSON 反序列化时，List 元素常为 {@link java.util.LinkedHashMap} 而非 {@link Course}。
     * 无筛选时直接序列化给前端可能仍像正常 JSON；有筛选时在 stream 里调用 {@code getCategory()} 会 ClassCastException。
     * JSON 反序列化后元素多为 Map：用 {@link ObjectMapper#convertValue} 转成 {@link List}&lt;{@link Course}&gt; 再参与筛选。
     */
    @SuppressWarnings("unchecked")
    public List<Course> findAllCourses() {
        List<Course> courseList = null;
        try {
            Object raw = redisTemplate.opsForValue().get(COURSE_LIST_KEY);
            if (raw instanceof List) {
                List<?> lst = (List<?>) raw;
                if (lst.isEmpty()) {
                    // 空列表视为未命中，重新拉库
                } else if (lst.get(0) instanceof Course) {
                    courseList = (List<Course>) raw;
                } else if (lst.get(0) instanceof Map) {
                    try {
                        courseList = objectMapper.convertValue(
                                raw,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Course.class));
                    } catch (Exception conv) {
                        log.warn("Redis 课程列表 convertValue 失败，已删键重载: {}", conv.toString());
                        try {
                            redisTemplate.delete(COURSE_LIST_KEY);
                        } catch (Exception delEx) {
                            log.warn("删除课程列表缓存失败: {}", delEx.toString());
                        }
                    }
                } else {
                    log.warn("课程列表缓存元素类型未知 {}，已删键重载", lst.get(0).getClass().getName());
                    try {
                        redisTemplate.delete(COURSE_LIST_KEY);
                    } catch (Exception delEx) {
                        log.warn("删除课程列表缓存失败: {}", delEx.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Redis 读取课程列表失败，已降级直连数据库: {}", e.toString());
        }
        if (courseList != null && !courseList.isEmpty()) {
            return courseList;
        }

        courseList = courseRepository.findAll();

        try {
            redisTemplate.opsForValue().set(COURSE_LIST_KEY, courseList, 60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis 写入课程列表缓存失败（已忽略）: {}", e.toString());
        }

        return courseList;
    }

    /**
     * 学生端课程列表（可选服务端筛选）。无任一筛选条件时与 {@link #findAllCourses()} 相同（走缓存）。
     * 有筛选条件时在缓存/全量结果上内存过滤，避免为筛选单独建缓存键。
     */
    public List<Course> findCoursesForStudent(String category,
                                              Double creditGte,
                                              Integer leftStockGte,
                                              String classTimeKeyword) {
        String cat = trimToNull(category);
        String kw = trimToNull(classTimeKeyword);
        // 0 表示「不设下限」，与前端步进器默认 0 一致，避免误当成有效筛选（尤其学分 null 被整表滤掉）
        if (creditGte != null && creditGte <= 0) {
            creditGte = null;
        }
        if (leftStockGte != null && leftStockGte <= 0) {
            leftStockGte = null;
        }
        boolean filtered = cat != null
                || creditGte != null
                || leftStockGte != null
                || kw != null;
        if (!filtered) {
            return findAllCourses();
        }
        List<Course> list = findAllCourses();
        final String catF = cat;
        final Double creditF = creditGte;
        final Integer stockF = leftStockGte;
        final String kwLower = kw == null ? null : kw.toLowerCase(Locale.ROOT);
        return list.stream()
                .filter(c -> catF == null || catF.equals(c.getCategory()))
                .filter(c -> creditF == null || (c.getCredit() != null && c.getCredit() >= creditF))
                .filter(c -> stockF == null || (c.getLeftStock() != null && c.getLeftStock() >= stockF))
                .filter(c -> kwLower == null || (c.getClassTime() != null
                        && c.getClassTime().toLowerCase(Locale.ROOT).contains(kwLower)))
                .collect(Collectors.toList());
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.isEmpty() ? null : v;
    }
}