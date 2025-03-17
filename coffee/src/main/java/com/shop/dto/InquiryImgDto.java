package com.shop.dto;

import com.shop.entity.InquiryImg;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class InquiryImgDto {
    private Long id;
    private String imgName; //파일명
    private String oriImgName; //원본이미지 파일명
    private String imgUrl; //이미지 조회 경로

    private static ModelMapper modelMapper = new ModelMapper();

    public static InquiryImgDto of(InquiryImg inquiryImg){
        return modelMapper.map(inquiryImg, InquiryImgDto.class);
    }

}
