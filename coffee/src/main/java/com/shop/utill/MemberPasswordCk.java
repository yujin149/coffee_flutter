package com.shop.utill;


import com.shop.dto.MemberFormDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

//비밀번호체크
@Component
public class MemberPasswordCk implements Validator {

    // 어떤 클래스 타입을 지원할지 명시
    @Override
    public boolean supports(Class<?> clazz) {
        return MemberFormDto.class.isAssignableFrom(clazz);
    }

    // 실제 검증 로직 작성
    @Override
    public void validate(Object target, Errors errors) {
        MemberFormDto memberFormDto = (MemberFormDto) target;

        String password = memberFormDto.getPassword();
        String passwordCk = memberFormDto.getPasswordCk();
        if (passwordCk == null) {
            passwordCk = "";
        }

        if (!password.equals(passwordCk)) {
            errors.rejectValue("passwordCk","비밀번호 불일치","비밀번호가 일치하지 않습니다.");
        }
    }
}
