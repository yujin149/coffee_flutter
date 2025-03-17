package com.shop.service;

import com.shop.entity.InquiryImg;
import com.shop.repository.InquiryImgRepository;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryImgService {
    @Value("${inquiryImgLocation}")
    private String inquiryImgLocation;

    private final InquiryImgRepository inquiryImgRepository;

    private final FileService fileService;

    public void saveInquiryImg(InquiryImg inquiryImg, MultipartFile inquiryImgFile) throws Exception{
        String oriImgName = inquiryImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        //파일업로드
        if(!StringUtils.isEmpty(oriImgName)){
            imgName = fileService.uploadFile(inquiryImgLocation, oriImgName, inquiryImgFile.getBytes());
            imgUrl = "/images/inquiry/" + imgName;
        }

        //이미지정보 저장
        inquiryImg.updateInquiryImg(oriImgName, imgName, imgUrl);
        inquiryImgRepository.save(inquiryImg);
    }

    //이미지 수정
    public void updateInquiryImg(Long inquiryImgId, MultipartFile inquiryImgFile) throws Exception{
        if(!inquiryImgFile.isEmpty()){
            InquiryImg savedInquiryImg = inquiryImgRepository.findById(inquiryImgId).orElseThrow(EntityExistsException::new);

            //기존 이미지 파일 삭제
            if(!StringUtils.isEmpty(savedInquiryImg.getImgName())){
                fileService.deleteFile(inquiryImgLocation+"/"+savedInquiryImg.getImgName());
            }
            String oriImgName = inquiryImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(inquiryImgLocation, oriImgName, inquiryImgFile.getBytes());
            String imgUrl = "/images/inquiry/" + imgName;
            savedInquiryImg.updateInquiryImg(oriImgName, imgName, imgUrl);
        }
    }
}
