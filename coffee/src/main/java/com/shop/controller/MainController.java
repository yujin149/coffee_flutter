package com.shop.controller;

import com.shop.dto.CrawlingDto;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.service.CrawlingService;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {


    private final CrawlingService crawlingService;
    private final ItemService itemService;

    /**
     * 메인 페이지 요청 처리
     * @param itemSearchDto 상품 검색 조건 DTO
     * @param page 현재 페이지 번호 (Optional)
     * @param model 뷰로 데이터를 전달하기 위한 모델 객체
     * @return 메인 페이지 경로 (main.html)
     */
    @GetMapping(value = "/")
    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {
        System.out.println("********메인페이지********");

        // 클릭 기반 추천 상품
        List<Item> recommendedItems = itemService.getTopClickedItems(8);

        // 페이지 요청 정보 생성: 기본값은 0페이지이며, 한 페이지에 8개씩 표시
        Pageable pageable = PageRequest.of(page.orElse(0), 8);

        // 검색 조건에 따라 상품 리스트 조회
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        // 크롤링 데이터 조회
        List<CrawlingDto> crawlingItems = crawlingService.getCrawlingItems(); // 크롤링 서비스 메서드 호출


        // 디버깅용 로그 출력: 현재 페이지 번호 및 총 페이지 수
        System.out.println(items.getNumber() + "!!!!!!!!!!"); // 현재 페이지 번호
        System.out.println(items.getTotalPages() + "#########"); // 총 페이지 수

        // 뷰로 전달할 데이터 추가
        model.addAttribute("items", items); // 조회된 상품 리스트
        model.addAttribute("itemSearchDto", itemSearchDto); // 검색 조건
        model.addAttribute("crawlingItems", crawlingItems); // 크롤링 데이터 추가
        model.addAttribute("maxPage", 5); // 페이지 네비게이션 최대 개수
        model.addAttribute("recommended", recommendedItems); // 클릭된 상품 데이터 추가

        return "main"; // 메인 페이지 경로 반환
    }
}
