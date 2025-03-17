package com.shop.service;

import com.shop.entity.GalleryImg;
import com.shop.repository.GalleryImgRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class GalleryImgService {
    
    // application.properties에서 설정한 이미지 저장 경로를 가져옴
    @Value("${galleryImgLocation}")
    private String galleryImgLocation;

    private final GalleryImgRepository galleryImgRepository;

    private final FileService fileService;

    /**
     * 갤러리 이미지를 저장하는 메서드
     * @param galleryImg 저장할 이미지 엔티티
     * @param galleryImgFile 실제 이미지 파일
     */

    public void saveGalleryImg(GalleryImg galleryImg, MultipartFile galleryImgFile) throws Exception{
        String oriImgName = galleryImgFile.getOriginalFilename();  // 원본 이미지 파일명
        String imgName = "";    // 서버에 저장될 이미지 파일명
        String imgUrl = "";     // 이미지 조회 경로

        // 파일이 존재하는 경우에만 업로드 처리
        if(!StringUtils.isEmpty(oriImgName)){
            // FileService를 통해 파일을 서버에 저장하고 저장된 파일명을 받아옴
            imgName = fileService.uploadFile(galleryImgLocation, oriImgName, galleryImgFile.getBytes());
            // 이미지 조회 경로 설정
            imgUrl = "/images/gallery/" + imgName;
        }

        // 갤러리 이미지 엔티티 정보 업데이트 및 저장
        galleryImg.updateGalleryImg(oriImgName, imgName, imgUrl);
        galleryImgRepository.save(galleryImg);
    }

    /**
     * 갤러리 이미지를 수정하는 메서드
     * @param galleryImgId 수정할 이미지 ID
     * @param galleryImgFile 새로운 이미지 파일
     */
    public void updateGalleryImg(Long galleryImgId, MultipartFile galleryImgFile) throws Exception{
        // 새로운 이미지 파일이 있는 경우에만 처리
        if (!galleryImgFile.isEmpty()){
            // 기존 이미지 정보 조회
            GalleryImg savedGalleryImg = galleryImgRepository.findById(galleryImgId)
                .orElseThrow(EntityExistsException::new);

            // 기존 이미지 파일이 있으면 삭제
            if(!StringUtils.isEmpty(savedGalleryImg.getImgName())){
                fileService.deleteFile(galleryImgLocation+"/"+savedGalleryImg.getImgName());
            }

            // 새로운 이미지 파일 업로드
            String oriImgName = galleryImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(galleryImgLocation, oriImgName, galleryImgFile.getBytes());
            String imgUrl = "/images/gallery/"+imgName;
            
            // 갤러리 이미지 정보 업데이트
            savedGalleryImg.updateGalleryImg(oriImgName, imgName, imgUrl);
        }
    }
}
