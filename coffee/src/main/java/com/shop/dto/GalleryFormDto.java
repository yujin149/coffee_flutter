package com.shop.dto;

import com.shop.constant.GalleryStatus;
import com.shop.entity.Gallery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class GalleryFormDto {

    private Long id;

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    private String title;

    //@NotBlank(message = "내용은 필수 입력 값입니다.")
    private String content;

    private GalleryStatus galleryStatus;

    @NotNull(message = "이벤트 시작일을 입력해주세요.")
    private LocalDate startTime; //이벤트시작일
    
    @NotNull(message = "이벤트 종료일을 입력해주세요.")
    private LocalDate endTime; //이벤트 종료일

    private List<GalleryImgDto> galleryImgDtoList = new ArrayList<>();
    private List<Long> galleryImgIds = new ArrayList<>();

    private static ModelMapper modelMapper = new ModelMapper();

    public Gallery createGallery(){
        return modelMapper.map(this, Gallery.class);
    }

    public static GalleryFormDto of(Gallery gallery){
        return modelMapper.map(gallery,GalleryFormDto.class);
    }


}
