package com.shop.config;



import com.shop.dto.SessionUser;
import com.shop.entity.Member;
//import com.shop.entity.User;
import com.shop.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Principal;
import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

//    @Autowired
//    private UserRepository userRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HttpSession httpSession;

    // 중요
    // 인증된 유저의 정보가 여기로 온다OAuth2UserRequest
    // 함수는 것밖에 없다.
    // OAuth2UserRequest 요청
    // oAuth2UserRequest , OAuth2User  여기에 정보가 들어가있다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException{
        // 우리 세팅시켜서 가저온 정보들은 연결
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(oAuth2UserRequest);



        //등록된 아이디  = 인증온  (naver, kakao, google id)
        // registrationId = ( kakao , google , naver)
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        System.out.println("registrationId = "+registrationId);

//      userNameAttributeName =( id , sub , response )
        //써야되서 name을 뺴놓는다.
        String userNameAttributeName = oAuth2UserRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        System.out.println("userNameAttributeName = "+userNameAttributeName);

        // 소셜 로그인 사용자 정보를 객체로 변환 (플랫폼이 어딘지 나옴)
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName
                , oAuth2User.getAttributes());

        System.out.println("attributes : " + attributes);




        try {
            Member member = saveOrUpdate(attributes, registrationId);

            httpSession.setAttribute("user", new SessionUser(member));
        } catch (IllegalStateException e) {
            System.out.println("로드 에러 로그인");
            OAuth2Error error = new OAuth2Error("invalid_request", e.getMessage(), null);
            httpSession.invalidate();

            throw new OAuth2AuthenticationException(error, e.getMessage());
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 기본 권한
        authorities.add(new SimpleGrantedAuthority("LOGINTYPE_SOCIAL")); // 소셜 로그인 권한 추가


//         여기서 싱글턴방식으로 사용한다 여기서 ROLE_USER로 보내서 user로 등급이 정해진다.
        return new DefaultOAuth2User(
                authorities
                , attributes.getAttributes()
//                , attributes.getNameAttributeKey());
                ,"email");

    }

    //Db에 저장시킨다.
    private Member saveOrUpdate(OAuthAttributes attributes, String provider) {
        System.out.println("OAuthAttributes: " + attributes);
        String email = attributes.getEmail();
        String principalId = attributes.getPrincipalId();
        System.out.println("Email: " + email);
        System.out.println("PrincipalId: " + principalId);



        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("소셜 계정에 이메일 정보가 없습니다. 이메일은 필수입니다.");
        }


        Optional<Member> existingMember = memberRepository.findByEmail(email);
        System.out.println("DB 조회 조건 - email: " + email);
        System.out.println("existingMember : "+existingMember);
        if (existingMember.isPresent()) {
            System.out.println("조회 성공 - Member: " + existingMember.get());
            Member member = existingMember.get();
            if (!member.getProvider().equals(provider)) {
                System.out.println("동일 로그인 접속 시도");
                System.out.println("member provider : " + member.getProvider());
                System.out.println("가입하려는 provider : " + provider);
                throw new IllegalStateException("이미 등록된 이메일입니다. 다른 소셜 계정을 사용해주세요.");
            }

            // 기존 소셜 계정 사용자 업데이트
            member.update(attributes.getName(), attributes.getPicture());
            return memberRepository.save(member);
        }

        // 새로운 사용자 생성
        try {
            Member newMember = Member.createSocialMember(email, attributes.getName(), attributes.getPicture(), provider);
            System.out.println("New Member created: " + newMember);
            System.out.println("Saving Member - userid: " + newMember.getUserid());
            System.out.println("Saving Member - email: " + newMember.getEmail());
            System.out.println("Saving Member - name: " + newMember.getName());

            return memberRepository.save(newMember);
        } catch (Exception e) {
            throw new IllegalArgumentException("로그인 중에 에러메세지가 발생했습니다.");
        }

    }
}
