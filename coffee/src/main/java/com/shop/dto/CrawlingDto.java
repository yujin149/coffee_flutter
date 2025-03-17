package com.shop.dto;

import com.shop.constant.ItemMenu;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class CrawlingDto {
    private Long id;
    private String image;
    private String subject;
    private String price;
    private String url;
    private ItemMenu itemMenu;

    // 기본 생성자 및 매개변수 생성자 추가
    public CrawlingDto(Long id, String image, String subject, String price, String url,ItemMenu itemMenu) {
        this.id=id;
        this.image = image;
        this.subject = subject;
        this.price = price;
        this.url = url;
        this.itemMenu=itemMenu;
    }


}
