package com.shop.controller;

import com.shop.dto.GalleryDto;
import com.shop.entity.Gallery;
import com.shop.service.GalleryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GalleryRestController {

    private final GalleryService galleryService;

    @GetMapping("/gallery")
    public ResponseEntity<Map<String, Object>> getGalleryList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String searchType,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "12") int size) {
        
        try {
            log.info("Gallery API called with params - keyword: {}, searchType: {}, page: {}, size: {}", 
                    keyword, searchType, page, size);
            
            // 페이지가 0이하인 경우 0으로 설정
            page = Math.max(0, page);
            
            // 페이징 정보 생성
            PageRequest pageRequest = PageRequest.of(page, size);
            
            // 검색어 유무에 따른 갤러리 목록 조회
            Page<Gallery> galleryPage;
            if (keyword != null && !keyword.isEmpty()) {
                galleryPage = galleryService.searchGalleriesByKeyword(keyword, searchType, pageRequest);
            } else {
                galleryPage = galleryService.getAllGalleriesSorted(pageRequest);
            }
            
            log.info("Found {} galleries", galleryPage.getContent().size());
            
            // Entity를 DTO로 변환
            List<GalleryDto> galleryDtoList = galleryPage.getContent().stream()
                    .map(this::convertToDto)
                    .filter(dto -> dto.getRepImgUrl() != null && !dto.getRepImgUrl().isEmpty())
                    .collect(Collectors.toList());

            log.info("Converted to {} DTOs", galleryDtoList.size());

            Map<String, Object> response = new HashMap<>();
            response.put("galleries", galleryDtoList);
            response.put("hasMore", galleryPage.hasNext());
            response.put("totalPages", galleryPage.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "*")
                .body(response);
        } catch (Exception e) {
            log.error("Error in getGalleryList", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private GalleryDto convertToDto(Gallery gallery) {
        GalleryDto dto = new GalleryDto();
        dto.setId(gallery.getId());
        dto.setTitle(gallery.getTitle());
        dto.setContent(gallery.getContent());
        dto.setStartTime(gallery.getStartTime());
        dto.setEndTime(gallery.getEndTime());
        dto.setGalleryStatus(gallery.getGalleryStatus());
        
        // 대표 이미지 URL 설정
        if (!gallery.getGalleryImgList().isEmpty()) {
            dto.setRepImgUrl("/images/gallery/" + gallery.getGalleryImgList().get(0).getImgName());
        } else {
            dto.setRepImgUrl("");
        }
        
        // 갤러리 이미지 목록 설정 (대표 이미지를 포함한 모든 이미지)
        List<String> galleryImgList = gallery.getGalleryImgList().stream()
            .map(img -> "/images/gallery/" + img.getImgName())
            .collect(Collectors.toList());
        dto.setGalleryImgList(galleryImgList);
        
        return dto;
    }
} 