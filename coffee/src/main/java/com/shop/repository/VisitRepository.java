package com.shop.repository;

import com.shop.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    @Query("SELECT COUNT(v) FROM Visit v WHERE v.visitDate BETWEEN :startDate AND :endDate")
    Long countVisitsByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    //월별 방문자 수 조회 (일별 방문자수 합산)
    @Query("SELECT SUM(daily.count) FROM " +
           "(SELECT COUNT(v) as count FROM Visit v " +
           "WHERE v.visitDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(v.visitDate)) daily")
    Long countMonthlyVisits(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 