package com.shop.controller;

import com.shop.dto.CrawlingDto;
import com.shop.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CrawlingController {
    private final CrawlingService crawlingService;



    // 크롤링 데이터를 화면에 표시
    @GetMapping("/admin/crawling")
    public String displayCrawlingData(Model model){
        log.info("Starting crawling process...");           // db를 조회하는 메서드
        List<CrawlingDto> crawlingDtoList = crawlingService.getSavedData();
        model.addAttribute("crawlingDtoList", crawlingDtoList);
        log.info("Saving data : {}", crawlingDtoList);
        return "crawling/start";
    }


    @GetMapping("admin/crawling/update")
    public ResponseEntity<String> handleGetRequestForUpdate() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("GET 요청은 지원되지 않습니다. POST 요청만 가능합니다.");
    }

    //    // 크롤링 데이터를 업데이트 (POST 요청)
//    @PostMapping("/crawling/update")
//    public ResponseEntity<String> updateCrawlingData() {
//        try {
//            log.info("Starting the crawling update process...");
//            List<CrawlingDto> crawlingDtoList = crawlingService.getCrawlingData(); // 크롤링 실행
//            crawlingService.saveCrawlingData(crawlingDtoList); // 크롤링 데이터 DB에 저장
//            log.info("Crawling data updated successfully.");
//            return ResponseEntity.ok("크롤링이 성공적으로 업데이트되었습니다.");
//        } catch (Exception e) {
//            log.error("Error during crawling update: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("크롤링 업데이트 중 오류가 발생했습니다.");
//        }
// 크롤링 데이터를 업데이트
    @PostMapping("/admin/crawling/update")
    public String updateCrawlingData(Model model) {
        log.info("Starting crawling process for update...");
        List<CrawlingDto> crawlingDtoList = crawlingService.getCrawlingData(); // 크롤링 실행
        crawlingService.saveCrawlingData(crawlingDtoList); // DB 업데이트
        model.addAttribute("crawlingDtoList", crawlingDtoList);
        log.info("Crawling and update completed.");
        return "redirect:/"; // 업데이트 후 목록 페이지로 리다이렉트
    }
}



// 크롤링 데이터를 업데이트 (POST 요청)
//    @PostMapping("/crawling/update")
//    public String saveCrawlingData(Model model) {
//        try {
//            List<CrawlingDto> crawlingDtoList = crawlingService.getCrawlingData();
//            crawlingService.saveCrawlingData(crawlingDtoList);
//            model.addAttribute("crawlingDtoList", crawlingDtoList);
//            log.info("Crawling data saved successfully.");
//        } catch (Exception e) {
//            log.error("Unexpected error during crawling update: {}", e.getMessage(), e);
//            model.addAttribute("errorMessage", "업데이트 도중 예상치 못한 오류가 발생했습니다.");
//        }
//
//        return "crawling/updateForm";
//    }

