package com.shop.dto;

import com.shop.entity.GalleryImg;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class GalleryImgDto {
    private Long id;
    private String imgName;
    private String oriImgName;
    private String imgUrl;
    private String repImgYn;

    // ModelMapper는 객체 간의 속성을 자동으로 매핑
    private static ModelMapper modelMapper = new ModelMapper();

    //of() 메서드는 GalleryImg 엔티티를 GalleryImgDto로 변환
    public static GalleryImgDto of(GalleryImg galleryImg){
        return modelMapper.map(galleryImg, GalleryImgDto.class);
    }
}
