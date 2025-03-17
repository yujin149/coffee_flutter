package com.shop.service;

import com.shop.entity.Visit;
import com.shop.repository.VisitRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import jakarta.servlet.http.HttpSession;

@Service
@Transactional
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;

    public void recordVisit(HttpServletRequest request) {
        String visitorIp = getClientIp(request);
        Visit visit = new Visit();
        visit.setVisitorIp(visitorIp);
        visit.setVisitDate(LocalDateTime.now());
        visitRepository.save(visit);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public Long getDailyVisitCount(LocalDateTime date) {
        LocalDateTime startOfDay = date.with(LocalTime.MIN);
        LocalDateTime endOfDay = date.with(LocalTime.MAX);
        return visitRepository.countVisitsByDate(startOfDay, endOfDay);
    }

    public Long getMonthlyVisitCount(LocalDateTime date) {
        LocalDateTime startOfMonth = date.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = date.withDayOfMonth(date.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);
        return visitRepository.countMonthlyVisits(startOfMonth, endOfMonth);
    }
} 