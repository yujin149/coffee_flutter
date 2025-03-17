package com.shop.controller;

import com.shop.service.OrderService;
import com.shop.service.VisitService;
import com.shop.constant.ItemMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.PageRequest;
import java.net.URLEncoder;

@Controller
@RequiredArgsConstructor
public class OrderStatsController {
    
    private final OrderService orderService;
    private final VisitService visitService;
    private final HttpServletRequest request;

    @GetMapping("/admin/orders/stats/monthly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMonthlyOrderStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        Long monthlyTotal = orderService.getMonthlyOrderTotal(year, month);
        
        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("total", monthlyTotal);
        
        return ResponseEntity.ok(response);
    }

    //일별 방문자 수 조회
    @GetMapping("/admin/visits/daily")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDailyVisits(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day) {
        
        if (year == null || month == null || day == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
            day = now.getDayOfMonth();
        }

        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0);
        Long visitCount = visitService.getDailyVisitCount(date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("date", day);
        response.put("count", visitCount);
        
        return ResponseEntity.ok(response);
    }

    //월별 방문자 수 조회
    @GetMapping("/admin/visits/monthly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMonthlyVisits(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        LocalDateTime date = LocalDateTime.of(year, month, 1, 0, 0);
        Long visitCount = visitService.getMonthlyVisitCount(date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("count", visitCount);
        
        return ResponseEntity.ok(response);
    }

    //일별 카테고리별 매출 조회
    @GetMapping("/admin/orders/stats/daily")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDailyOrderStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day) {
        
        if (year == null || month == null || day == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
            day = now.getDayOfMonth();
        }

        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0);
        Map<String, Long> salesByCategory = orderService.getDailySalesByCategory(date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("date", day);
        response.put("sales", salesByCategory);
        
        return ResponseEntity.ok(response);
    }

    //상품 판매 순위 조회
    @GetMapping("/admin/orders/stats/top-items")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTopSellingItems() {
        List<Map<String, Object>> topItems = orderService.getTopSellingItems();
        return ResponseEntity.ok(topItems);
    }

    @GetMapping("/admin/orders/stats/daily-details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDailyOrderDetails(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam String category,
            @RequestParam(defaultValue = "1") int page) {
        
        LocalDateTime date = LocalDateTime.of(year, month, day, 0, 0);
        ItemMenu itemMenu = "TOTAL".equals(category) ? null : ItemMenu.valueOf(category);
        PageRequest pageRequest = PageRequest.of(page - 1, 5);
        Page<Map<String, Object>> orders = orderService.getDailyOrderDetails(date, itemMenu, pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders.getContent());
        response.put("totalPages", orders.getTotalPages());
        response.put("currentPage", page);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/orders/stats/daily-details/excel")
    @ResponseBody
    public ResponseEntity<ByteArrayResource> downloadDailyOrderDetailsExcel(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestParam String category) {
        
        System.out.println("Excel download requested - year: " + year + ", month: " + month + ", day: " + day + ", category: " + category);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("주문내역");
            
            LocalDateTime orderDate = LocalDateTime.of(year, month, day, 0, 0);
            List<Map<String, Object>> orders = orderService.getDailyOrderDetailsForExcel(orderDate, category);
            
            System.out.println("Retrieved orders size: " + orders.size());
            if (orders.isEmpty()) {
                System.out.println("No orders found for the given date and category");
            } else {
                System.out.println("Sample order: " + orders.get(0));
            }

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {"번호", "상품번호", "상품명", "단가", "수량", "총 금액", "주문자", "전화번호", "주소", "주문일"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 데이터 입력
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (Map<String, Object> order : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(((Number) order.get("itemId")).longValue());
                row.createCell(2).setCellValue((String) order.get("itemNm"));
                row.createCell(3).setCellValue(((Number) order.get("price")).intValue());
                row.createCell(4).setCellValue(((Number) order.get("count")).intValue());
                row.createCell(5).setCellValue(((Number) order.get("totalPrice")).intValue());
                row.createCell(6).setCellValue((String) order.get("orderName"));
                row.createCell(7).setCellValue((String) order.get("phone"));
                row.createCell(8).setCellValue((String) order.get("address"));
                row.createCell(9).setCellValue(((LocalDateTime) order.get("orderDate")).format(formatter));
            }
            
            // 열 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            String fileName = String.format("주문내역_%d%02d%02d.xlsx", year, month, day);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(new ByteArrayResource(outputStream.toByteArray()));
            
        } catch (Exception e) {
            System.err.println("Excel download error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 