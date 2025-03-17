package com.shop.entity;

import com.shop.constant.ItemMenu;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "item")
@Getter
@Setter
@ToString
@NoArgsConstructor // 기본 생성자 추가
public class Item extends BaseEntity{
    @Id
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // 상품코드

    @Column(nullable = true,length = 500)
    private String itemNm; // 상품명

    @Column(name = "price", nullable = false)
    private int price; // 가격

    @Column(nullable = true)
    private int stockNumber; // 수량

    @Column(nullable = true)
    private String image;

    @Column(name = "url",length = 10000)
    private String url;
    @Lob
    @Column(nullable = true)
    private String itemDetail; // 상품상세설명

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; // 상품판매 상태

    private Boolean isCrawled; // 크롤링 여부를 저장

    @Enumerated(EnumType.STRING)
    private ItemMenu itemMenu; // 아이템 메뉴

    @Column(name = "click_count", nullable = false)
    private int clickCount = 0; // 클릭 수 (초기값 0)

    @Column(columnDefinition = "integer default 0")
    private int orderCount = 0;  // 주문 수량 기본값 0으로 설정

    //private LocalDateTime regTime; // 등록 시간
    //private LocalDateTime updateTime; // 수정 시간

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ItemImg> images;

    // 클릭 수 증가 메서드
    public void increaseClickCount() {
        this.clickCount++;
    }

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "meber_item",
//            joinColumns = @JoinColumn(name="member_id"),
//            inverseJoinColumns = @JoinColumn(name = "item_id")
//    )
//    private List<Member> member;

    // 상품 업데이트 메서드
    public void updateItem(ItemFormDto itemFormDto) {
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
        this.itemMenu = itemFormDto.getItemMenu();
    }



    // 재고 감소 메서드
    public void removeStock(int stockNumber){
        int restStock = this.stockNumber - stockNumber; // 10, 5 / 10 , 20
        if(restStock < 0){
            throw new OutOfStockException("상품의 재고가 부족합니다.(현재 재고 수량: " + this.stockNumber+")");
        }
        this.stockNumber = restStock; // 5
    }
    // 재고 추가 메서드
    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;
    }

    public Item(Long id) {
        this.id = id;
    }
    public String getRepresentativeImageUrl() {
        if (images != null) {
            for (ItemImg img : images) {
                if ("Y".equals(img.getRepImgYn())) {
                    return img.getImgUrl();
                }
            }
        }
        // 대표 이미지가 없으면 기본 이미지 반환
        return "/images/default.png";
    }

    public String getImgUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImgUrl();
        }
        return "";
    }

}