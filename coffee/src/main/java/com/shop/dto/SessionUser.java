package com.shop.dto;


import com.shop.entity.Member;
//import com.shop.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
// 직렬화 자바 시스템에서 사용 할 수 있도록 바이트 스트림 형태로 연속 적인 데이터로
// 포멧 변환 기술
// Java Objec, Date -> Byte Stream
// 역직렬화 바이트 스트림 -> 자바 Object Date
public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String picture;



    public SessionUser(Member member) {
        this.name = member.getName();
        this.email = member.getEmail();
        this.picture = member.getPicture();
    }


}
