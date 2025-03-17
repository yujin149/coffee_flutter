package com.shop.service;

import com.shop.dto.GalleryFormDto;
import com.shop.dto.GalleryImgDto;
import com.shop.entity.Gallery;
import com.shop.entity.GalleryImg;
import com.shop.repository.GalleryImgRepository;
import com.shop.repository.GalleryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final GalleryImgService galleryImgService;
    private final GalleryImgRepository galleryImgRepository;

    // 갤러리 게시글 저장 기능
    public Long saveGallery(GalleryFormDto galleryFormDto, List<MultipartFile> galleryImgFileList) throws Exception{
        // DTO를 엔티티로 변환하여 갤러리 정보 저장
        Gallery gallery = galleryFormDto.createGallery();
        galleryRepository.save(gallery);

        // 이미지 등록 처리
        for (int i = 0; i < galleryImgFileList.size(); i++){
            GalleryImg galleryImg = new GalleryImg();
            galleryImg.setGallery(gallery);
            // 첫 번째 이미지는 대표 이미지로 설정
            if (i == 0){
                galleryImg.setRepimgYn("Y");
            }else {
                galleryImg.setRepimgYn("N");
            }
            galleryImgService.saveGalleryImg(galleryImg, galleryImgFileList.get(i));
        }
        return gallery.getId();
    }

    @Transactional(readOnly= true)
    public GalleryFormDto getGalleryDtl(Long galleryId){
        // 해당 갤러리의 모든 이미지 조회
        List<GalleryImg> galleryImgList = galleryImgRepository.findByGalleryIdOrderByIdAsc(galleryId);
        List<GalleryImgDto> galleryImgDtoList = new ArrayList<>();
        
        // 이미지 엔티티를 DTO로 변환
        for(GalleryImg galleryImg : galleryImgList){
            GalleryImgDto galleryImgDto = GalleryImgDto.of(galleryImg);
            galleryImgDtoList.add(galleryImgDto);
        }

        // 갤러리 정보 조회 및 DTO 변환
        Gallery gallery = galleryRepository.findById(galleryId)
            .orElseThrow(EntityNotFoundException::new);
        GalleryFormDto galleryFormDto = GalleryFormDto.of(gallery);
        galleryFormDto.setGalleryImgDtoList(galleryImgDtoList);
        return galleryFormDto;
    }

    // 갤러리 정보 수정
    public Long updateGallery(GalleryFormDto galleryFormDto, List<MultipartFile> galleryImgFileList) throws Exception{
        // 갤러리 정보 수정
        Gallery gallery = galleryRepository.findById(galleryFormDto.getId())
            .orElseThrow(EntityNotFoundException::new);
        gallery.updateGallery(galleryFormDto);

        List<Long> galleryImgIds = galleryFormDto.getGalleryImgIds();

        // 이미지 정보 수정
        for(int i = 0; i < galleryImgFileList.size(); i++){
            galleryImgService.updateGalleryImg(galleryImgIds.get(i), galleryImgFileList.get(i));
        }
        return gallery.getId();
    }

    // 갤러리 상태에 따라 정렬된 목록 반환
    @Transactional(readOnly = true)
    public Page<Gallery> getAllGalleriesSorted(Pageable pageable) {
        return galleryRepository.findAllSortedByStatusAndId(pageable);
    }

    // 제목이나 내용으로 검색된 갤러리 목록 반환
    @Transactional(readOnly = true)
    public Page<Gallery> searchGalleriesByKeyword(String keyword, String searchType, Pageable pageable) {
        return galleryRepository.searchGalleryByKeyword(keyword, searchType, pageable);
    }

    // 특정 id로 갤러리 가져오기
    public Gallery getGalleryById(Long id) {
        return galleryRepository.findById(id).orElse(null);  // id가 없으면 null 반환
    }

    // 이전 게시글 찾기
    public Gallery getPreviousGallery(Long currentId) {
        return galleryRepository.findFirstByIdLessThanOrderByIdDesc(currentId);
    }

    // 다음 게시글 찾기
    public Gallery getNextGallery(Long currentId) {
        return galleryRepository.findFirstByIdGreaterThanOrderByIdAsc(currentId);
    }

    // 갤러리 삭제
    @Transactional
    public void deleteGallery(Long galleryId) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(EntityNotFoundException::new);
        
        // 이미지 파일 삭제
        List<GalleryImg> galleryImgList = galleryImgRepository.findByGalleryIdOrderByIdAsc(galleryId);
        for (GalleryImg galleryImg : galleryImgList) {
            galleryImgRepository.delete(galleryImg);
        }
        
        // 갤러리 삭제
        galleryRepository.delete(gallery);
    }

}
