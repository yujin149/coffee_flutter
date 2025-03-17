package com.shop.controller;

import com.shop.dto.GalleryDto;
import com.shop.dto.GalleryFormDto;
import com.shop.entity.Gallery;
import com.shop.entity.GalleryImg;
import com.shop.repository.GalleryImgRepository;
import com.shop.service.GalleryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class GalleryController {
    private final GalleryService galleryService;
    private final GalleryImgRepository galleryImgRepository;

    // 새 갤러리 글 작성 페이지로 이동
    @GetMapping(value = "/gallery/new")
    public String galleryForm(Model model){
        model.addAttribute("galleryFormDto", new GalleryFormDto());  
        // 오늘 날짜를 모델에 추가 (시작일/종료일 기본값으로 사용)
        model.addAttribute("currentDate", LocalDate.now());
        return "gallery/galleryWrite";
    }

    // 새 갤러리 글 저장 처리
    @PostMapping(value = "/gallery/new")
    public String galleryNew(@Valid GalleryFormDto galleryFormDto, 
                           BindingResult bindingResult, 
                           Model model, 
                           @RequestParam("galleryImgFile")List<MultipartFile> galleryImgFileList){
        // 폼 데이터 유효성 검증 실패 시
        if(bindingResult.hasErrors()){
            return "gallery/galleryWrite";
        }
        // 첫 번째 이미지(대표 이미지) 필수 체크
        if(galleryImgFileList.get(0).isEmpty() && galleryFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 이미지는 필수 입력 값입니다.");
            return "gallery/galleryWrite";
        }
        try {
            galleryService.saveGallery(galleryFormDto, galleryImgFileList);  // 갤러리 정보와 이미지 저장
        }catch (Exception e){
            model.addAttribute("errorMessage", "이미지 등록 중 에러가 발생했습니다.");
            return "gallery/galleryWrite";
        }
        return "gallery/gallery";
    }

    // 갤러리 글 수정 페이지로 이동
    @GetMapping(value = "/gallery/galleryModify/{galleryId}")
    public String galleryDtl(@PathVariable("galleryId") Long galleryId, Model model){
        try{
            // 수정할 갤러리 정보 조회
            GalleryFormDto galleryFormDto = galleryService.getGalleryDtl(galleryId);
            model.addAttribute("galleryFormDto", galleryFormDto);
        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage","존재하지 않는 상품입니다.");
            model.addAttribute("galleryFormDto", new GalleryFormDto());
            return "gallery/galleryModify";
        }
        return "gallery/galleryModify";
    }

    // 갤러리 상세 보기 페이지
    @GetMapping("/gallery/{galleryId}")
    public String viewGallery(@PathVariable Long galleryId, Model model) {
        try {
            GalleryFormDto galleryFormDto = galleryService.getGalleryDtl(galleryId);
            
            // 이미지가 없는 경우 체크
            if (galleryFormDto.getGalleryImgDtoList() == null || galleryFormDto.getGalleryImgDtoList().isEmpty()) {
                model.addAttribute("errorMessage", "이미지가 없는 게시글입니다.");
                return "redirect:/gallery";
            }
            
            // 이전글, 다음글 정보 조회
            Gallery prevGallery = galleryService.getPreviousGallery(galleryId);
            Gallery nextGallery = galleryService.getNextGallery(galleryId);
            
            model.addAttribute("gallery", galleryFormDto);
            model.addAttribute("prevGallery", prevGallery);
            model.addAttribute("nextGallery", nextGallery);
            
            return "gallery/galleryView";
        } catch (EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 게시글입니다.");
            return "redirect:/gallery";
        }
    }

    // 갤러리 글 삭제 처리 (AJAX 요청 처리)
    @PostMapping("/gallery/delete/{galleryId}")
    @ResponseBody
    public String deleteGallery(@PathVariable Long galleryId) {
        try {
            galleryService.deleteGallery(galleryId);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    // 갤러리 글 수정 처리
    @PostMapping(value = "/gallery/galleryModify/{galleryId}")
    public String galleryUpdate(@Valid GalleryFormDto galleryFormDto, 
                              BindingResult bindingResult, 
                              @RequestParam("galleryImgFile") List<MultipartFile> galleryImgFileList, 
                              Model model){
        // 폼 데이터 유효성 검증
        if (bindingResult.hasErrors()){
            return "gallery/galleryModify";
        }
        // 대표 이미지 필수 체크
        if (galleryImgFileList.get(0).isEmpty() && galleryFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 이미지는 필수 입력 값입니다.");
            return "gallery/galleryModify";
        }

        try {
            galleryService.updateGallery(galleryFormDto, galleryImgFileList);  // 갤러리 정보 업데이트
        }catch (Exception e){
            model.addAttribute("errorMessage", "수정 중 에러가 발생하였습니다.");
            return "gallery/galleryModify";
        }
        return "gallery/gallery";
    }

    // 갤러리 목록 페이지로 이동
    @GetMapping("/gallery")
    public String galleryList(Model model) {
        return "gallery/gallery";
    }

    // 갤러리 목록 데이터 조회 (AJAX 요청 처리)
    @GetMapping("/gallery/list")
    @ResponseBody
    public List<GalleryDto> getGalleryList(
            @RequestParam(required = false) String keyword,          // 검색어
            @RequestParam(required = false, defaultValue = "all") String searchType,  // 검색 유형
            @RequestParam(required = false, defaultValue = "0") int page,            // 페이지 번호
            @RequestParam(required = false, defaultValue = "12") int size) {         // 페이지당 게시글 수
        
        // 페이징 정보 생성
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        
        // 검색어 유무에 따른 갤러리 목록 조회
        Page<Gallery> galleryPage;
        if (keyword != null && !keyword.isEmpty()) {
            galleryPage = galleryService.searchGalleriesByKeyword(keyword, searchType, pageRequest);
        } else {
            galleryPage = galleryService.getAllGalleriesSorted(pageRequest);
        }
        
        // Entity를 DTO로 변환하고 대표 이미지가 있는 게시글만 필터링
        List<GalleryDto> galleryDtoList = galleryPage.getContent().stream()
                .map(this::convertToDto)
                .filter(dto -> dto.getRepImgUrl() != null && !dto.getRepImgUrl().isEmpty())
                .collect(Collectors.toList());

        // ID 기준 내림차순 정렬 (최신글이 위로)
        galleryDtoList.sort((a, b) -> b.getId().compareTo(a.getId()));
        
        return galleryDtoList;
    }

    // Gallery 엔티티를 GalleryDto로 변환하는 private 메서드
    private GalleryDto convertToDto(Gallery gallery) {
        GalleryDto dto = new GalleryDto();
        dto.setId(gallery.getId());
        dto.setTitle(gallery.getTitle());
        dto.setContent(gallery.getContent());
        dto.setStartTime(gallery.getStartTime());
        dto.setEndTime(gallery.getEndTime());
        dto.setGalleryStatus(gallery.getGalleryStatus());
        
        // 대표 이미지 URL 설정
        List<GalleryImg> images = galleryImgRepository.findByGalleryIdOrderByIdAsc(gallery.getId());
        if (!images.isEmpty()) {
            dto.setRepImgUrl("/images/gallery/" + images.get(0).getImgName());
        }
        
        return dto;
    }
}
