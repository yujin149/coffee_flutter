package com.shop.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    //Optional(list와 비슷한대) 클래스는 아래와 같은 value에 값을 저장하기 때문에 값이
    //(list와 다르게) null이더라도 바로 NullPointError가 발생하지 않으며,
    //클래스이기 때문에 각종 메소드를 제공

    @Override
    public Optional<String> getCurrentAuditor() { //로그인한 상태여야 된다.
        //Sequrity 한테 인증을 받고 authentication 여기에 넣고
        Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();
        String userId = "";
        if (authentication != null) {
            //현재 로그인 한 사용자의 정보를 조회하여 사용자의 이름을 등록자와 수정자로 지정
            userId = authentication.getName(); //여기에 이메일이 들어간다
        }
        return Optional.of(userId);
    }

}
