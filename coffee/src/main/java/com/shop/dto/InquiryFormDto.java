package com.shop.dto;

import com.shop.entity.Inquiry;
import com.shop.constant.InquiryStatus;
import com.shop.entity.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InquiryFormDto {
    private Long id;

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 값입니다.")
    private String content;

    private boolean secret;

    private List<InquiryImgDto> inquiryImgDtoList = new ArrayList<>();
    private List<Long> inquiryImgIds = new ArrayList<>();

    private static ModelMapper modelMapper = new ModelMapper();

    @NotNull(message = "상품을 선택해주세요.")
    private Long itemId;

    private Item item;

    private InquiryStatus inquiryStatus;

    private String writer;

    private String writerId;

    public Inquiry createInquiry(){
        Inquiry inquiry = modelMapper.map(this, Inquiry.class);
        inquiry.setInquiryStatus(InquiryStatus.QUESTION);

        Item item = new Item();
        item.setId(this.itemId);
        inquiry.setItem(item);

        return inquiry;
    }

    public static InquiryFormDto of(Inquiry inquiry){
        return modelMapper.map(inquiry, InquiryFormDto.class);
    }

}
