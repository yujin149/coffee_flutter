package com.shop.controller;

import com.shop.dto.*;
import com.shop.entity.Member;
import com.shop.entity.Review;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.ReviewRepository;
import com.shop.service.CrawlingService;
import com.shop.service.ItemService;
import com.shop.entity.Item;
import com.shop.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CrawlingService crawlingService;
    private final ItemRepository itemRepository;
    private final ItemImgRepository itemImgRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    /**
     * 상품 등록 화면 반환
     * @param model 뷰로 데이터를 전달하기 위한 모델 객체
     * @return 상품 등록 페이지 경로
     */
    @GetMapping(value = "/admin/item/new")
    public String itemForm(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "item/itemForm";
    }

    /**
     * 상품 등록 처리
     * @param itemFormDto 상품 정보를 담은 DTO
     * @param bindingResult 유효성 검사 결과
     * @param model 모델 객체
     * @param itemImgFileList 상품 이미지 리스트
     * @return 성공 시 메인 페이지로 리다이렉트
     */
    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) {
        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }
        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }
        return "redirect:/";
    }


    /**
     * 특정 상품 상세 정보 조회
     * @param itemId 조회할 상품 ID
     * @param model 모델 객체
     * @return 상품 상세 페이지 경로
     */
    @GetMapping(value = "/admin/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
            return "item/itemForm";
        }
        return "item/itemForm";
    }


    /**
     * 상품 수정 처리
     * @param itemFormDto 수정할 상품 정보를 담은 DTO
     * @param bindingResult 유효성 검사 결과
     * @param itemImgFileList 상품 이미지 리스트
     * @param model 모델 객체
     * @return 성공 시 메인 페이지로 리다이렉트
     */
    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력값입니다.");
            return "item/itemForm";
        }
        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
//            e.printStackTrace(); // 디버깅용 로그
            return "item/itemForm";
        }
        return "redirect:/";
    }


    /**
     * 관리자 상품 관리 페이지 조회
     * @param itemSearchDto 검색 조건
     * @param page 페이지 번호
     * @param model 모델 객체
     * @return 상품 관리 페이지 경로
     */
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable("page") Optional<Integer> page,
                             Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 5);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);
        model.addAttribute("page", page.orElse(0));
        return "item/itemMng";
    }


    /**
     * 특정 상품 상세 페이지 조회
     * @param itemId 조회할 상품 ID
     * @param model 모델 객체
     * @param principal 로그인한 사용자 정보
     * @return 상품 상세 페이지 경로
     */
    @GetMapping("/item/{itemId}")
    public String itemDtl(
            @PathVariable("itemId") Long itemId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            Model model) {
        CrawlingDto crawlingDto = crawlingService.getCrawlingItemById(itemId);

        // 클릭 수 증가
        itemService.incrementClickCount(itemId);
        // 상품 정보 가져오기
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
        if (crawlingDto != null) {
            model.addAttribute("item", itemFormDto);
            model.addAttribute("isCrawled",true);
        } else {
            try {
                // 일반 상품 데이터를 모델에 추가
                itemFormDto = itemService.getItemDtl(itemId);
                model.addAttribute("item", itemFormDto);
                model.addAttribute("isCrawled", false);
            } catch (EntityNotFoundException e) {
                model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
                return "error/404";
            }
        }
        // 리뷰 목록 가져오기
        Page<Review> reviews = reviewService.getReviewsByItemId(itemId, PageRequest.of(page, 5));

        // 날짜 포맷터 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 리뷰 DTO로 변환
        List<ReviewDto> reviewDtos = reviews.getContent().stream()
                .map(review -> new ReviewDto(
                        review.getReviewerName(),
                        review.getCreatedDate() != null ? review.getCreatedDate().format(formatter) : null,
                        review.getContent(),
                        review.getRating()
                ))
                .toList();

        // 뷰로 전달
        model.addAttribute("reviews", reviewDtos);
        model.addAttribute("page", reviews.getNumber());
        model.addAttribute("maxPage", reviews.getTotalPages());
        return "item/itemDtl";
    }



    /**
     * 상품 삭제
     * @param itemId 삭제할 상품 ID
     * @return 삭제 결과 메시지
     */
    @DeleteMapping("/admin/item/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId) {
        try {
            itemService.deleteItem(itemId);
            return ResponseEntity.ok().body("상품이 성공적으로 삭제되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 삭제 중 오류가 발생했습니다.");
        }
    }

    /**
     * 리뷰 작성 폼 페이지 반환
     * @param itemId 리뷰를 작성할 상품 ID
     * @param model 모델 객체
     * @return 리뷰 작성 페이지 경로
     */
    @GetMapping("/item/{itemId}/review/new")
    public String reviewForm(@PathVariable("itemId") Long itemId, Model model) {
        model.addAttribute("itemId", itemId);
        model.addAttribute("reviewFormDto", new ReviewFormDto());
        return "item/reviewForm";
    }

    /**
     * 리뷰 저장 처리
     * @param itemId 리뷰를 작성할 상품 ID
     * @param reviewFormDto 리뷰 내용을 담은 DTO
     * @param bindingResult 유효성 검사 결과
     * @return 성공 시 상품 상세 페이지로 리다이렉트
     */
    @PostMapping("/item/{itemId}/review/new")
    public String submitReview(@PathVariable("itemId") Long itemId,
                               @ModelAttribute ReviewFormDto reviewFormDto,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "item/reviewForm";
        }

        Review review = new Review();
        review.setReviewerName(reviewFormDto.getName());
        review.setRating(reviewFormDto.getRating());
        review.setContent(reviewFormDto.getContent());
        review.setItem(new Item(itemId));

        reviewRepository.save(review);
        return "redirect:/item/" + itemId;
    }

    @GetMapping("/items/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page) {
        
        Pageable pageable = PageRequest.of(page - 1, 5);
        Page<Item> itemPage = itemService.getItemsForSearch(keyword, pageable);
        
        List<Map<String, Object>> items = itemPage.getContent().stream()
            .map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("itemNm", item.getItemNm());
                itemMap.put("price", item.getPrice());
                itemMap.put("imgUrl", item.getImgUrl());
                return itemMap;
            })
            .collect(Collectors.toList());
            
        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("totalPages", itemPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/item/{productId}/exists")
    public ResponseEntity<Boolean> checkItemExists(@PathVariable Long productId) {
        boolean exists = itemService.existsById(productId);
        return ResponseEntity.ok(exists);
    }

}
