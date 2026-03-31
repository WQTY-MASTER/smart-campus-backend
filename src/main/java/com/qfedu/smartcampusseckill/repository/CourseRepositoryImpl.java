package com.qfedu.smartcampusseckill.repository;

import com.qfedu.smartcampusseckill.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class CourseRepositoryImpl implements CourseRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public Page<Course> pageQuery(String category, Integer leftStockGte, String courseName, Pageable pageable) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (category != null) {
            where.append(" AND c.category = :category");
        }
        if (leftStockGte != null) {
            where.append(" AND c.left_stock >= :leftStockGte");
        }
        if (courseName != null) {
            where.append(" AND CAST(c.course_name AS varchar(100)) ILIKE '%' || :courseName || '%'");
        }
        String fromWhere = " FROM course c" + where;
        String countSql = "SELECT count(*)" + fromWhere;
        String dataSql = "SELECT c.*" + fromWhere + " ORDER BY c.id DESC";

        Query countQuery = entityManager.createNativeQuery(countSql);
        Query dataQuery = entityManager.createNativeQuery(dataSql, Course.class);
        if (category != null) {
            countQuery.setParameter("category", category);
            dataQuery.setParameter("category", category);
        }
        if (leftStockGte != null) {
            countQuery.setParameter("leftStockGte", leftStockGte);
            dataQuery.setParameter("leftStockGte", leftStockGte);
        }
        if (courseName != null) {
            countQuery.setParameter("courseName", courseName);
            dataQuery.setParameter("courseName", courseName);
        }

        long total = ((Number) countQuery.getSingleResult()).longValue();
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<Course> content = dataQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }
}
