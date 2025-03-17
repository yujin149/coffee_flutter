package com.shop.entity;


import com.shop.dto.InquiryFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.shop.constant.InquiryStatus;

@Entity
@Table(name="inquiry")
@Getter
@Setter
@ToString
public class Inquiry extends BaseEntity{
    @Id
    @Column(name="inquiry_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title; //제목

    @Column(nullable = false)
    private String content; //내용

    private boolean secret; //비밀글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 상품 정보

    @Enumerated(EnumType.STRING)
    private InquiryStatus inquiryStatus; // 문의 상태

    @Column(nullable = false)
    private String writer; // 작성자

    @Column(nullable = false)
    private String writerId; // 작성자 아이디

    //수정하기
    public void upadateInquiry(InquiryFormDto inquiryFormDto){
        this.title = inquiryFormDto.getTitle();
        this.content = inquiryFormDto.getContent();
        this.writer = inquiryFormDto.getWriter();
        this.writerId = inquiryFormDto.getWriterId();
        this.secret = inquiryFormDto.isSecret();

        // item 업데이트
        if(inquiryFormDto.getItemId() != null) {
            Item item = new Item();
            item.setId(inquiryFormDto.getItemId());
            this.item = item;
        }

        // inquiryStatus 유지 (null이 아닌 경우에만 업데이트)
        if(inquiryFormDto.getInquiryStatus() != null) {
            this.inquiryStatus = inquiryFormDto.getInquiryStatus();
        }
    }

    public void updateInquiry(InquiryFormDto inquiryFormDto) {
        this.title = inquiryFormDto.getTitle();
        this.content = inquiryFormDto.getContent();
        this.secret = inquiryFormDto.isSecret();
        if (inquiryFormDto.getItemId() != null) {
            Item item = new Item();
            item.setId(inquiryFormDto.getItemId());
            this.item = item;
        }
    }
}
