package com.shop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 장바구니 주문 정보를 담는 DTO
 */
@Getter
@Setter
public class CartOrderDto {
    private Long cartItemId;  // 장바구니 항목 ID
    private int count;        // 수량
    private int price;        // 단가 (클라이언트에서 전달된 가격)
    private int totalprice;
    private int finalprice;
    private String imgUrl;
    private String itemNm;
    private int usedMembership;

    // 결제 관련 필드
    @JsonProperty("imp_uid")
    private String impUid;       // 아임포트 결제 고유 ID
    @JsonProperty("merchant_uid")
    private String merchantUid;  // 주문 번호

    private int paidAmount;      // 결제된 총 금액

    // 추가적으로 필요한 필드
    private String buyerName;    // 구매자 이름
    private String buyerEmail;   // 구매자 이메일

    @JsonProperty("selectedProducts")
    private List<CartOrderDto> cartOrderDtoList; // 선택된 상품 리스트


    @Override
    public String toString() {
        return "CartOrderDto{" +
                "cartItemId=" + cartItemId +
                ", count=" + count +
                ", price=" + price +
                ", totalprice=" + totalprice +
                ", finalprice=" + finalprice +
                ", imgUrl='" + imgUrl + '\'' +
                ", itemNm='" + itemNm + '\'' +
                ", usedMembership=" + usedMembership +
                ", impUid='" + impUid + '\'' +
                ", merchantUid='" + merchantUid + '\'' +
                ", paidAmount=" + paidAmount +
                ", buyerName='" + buyerName + '\'' +
                ", buyerEmail='" + buyerEmail + '\'' +
                ", cartOrderDtoList=" + cartOrderDtoList +
                '}';
    }
}
