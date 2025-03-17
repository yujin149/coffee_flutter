package com.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewFormDto {
    @NotEmpty(message = "이름을 입력해주세요.")
    private String name;         // 리뷰 작성자 이름

    @Min(value = 1, message = "별점은 최소 1점이어야 합니다.")
    @Max(value = 5, message = "별점은 최대 5점이어야 합니다.")
    private int rating;          // 리뷰 별점

    @NotEmpty(message = "내용을 입력해주세요.")
    private String content;      // 리뷰 내용
}
