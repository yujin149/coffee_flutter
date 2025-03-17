package com.shop.config;

//import com.shop.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    private String principalId;

    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name,
                           String email, String picture, String principalId) {
        this.attributes = new HashMap<>(attributes);
//        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        // 이거 왜 안됨
        this.principalId = principalId;

        if (email != null) {
            this.attributes.put("email", email);
        }
    }

    public OAuthAttributes() {
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {
        if (registrationId.equals("kakao")) {
            return ofKakao(userNameAttributeName, attributes);
        }
        if (registrationId.equals("naver")) {
            return ofNaver(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName,
                                           Map<String, Object> attributes) {
        //네이버에서 받은 데이터에서 프로필 정보가 담긴 response 값을 꺼낸다.
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        System.out.println("Naver Response: " + response);

        if (response == null || response.get("email") == null) {
            throw new IllegalStateException("Naver API 응답에 'id' 필드가 없습니다: " + attributes);
        }

        String id = (String) response.get("id");
        String email = (String) response.get("email");

        System.out.println("Naver Response: " + response);

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) response.get("name"),
                email,
                (String) response.get("profile_image"),
                id
        );
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName,
                                           Map<String, Object> attributes) {
        //카카오로 받은 데이터에서 계정 정보가 담긴 Kakao_account 값을 꺼낸다.
        Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");
        //마찬가지로 profile(nickname, image_url.. 등)정보가 담긴 값을 꺼낸다. (JSON 정리)
        Map<String, Object> profile = (Map<String, Object>) kakao_account.get("profile");


        if (kakao_account == null || kakao_account.get("email") == null) {
            throw new IllegalStateException("Kakao API 응답에 이메일 정보가 없습니다. 이메일은 필수입니다.");
        }

        String id = String.valueOf(attributes.get("id"));
        String email = (String) kakao_account.get("email");

        if (email == null) {
            System.out.println("카카오 이메일 없다고 나옴");
            email = "kakao_" + UUID.randomUUID().toString(); // 기본 이메일 생성
            System.out.println("카카오 API 응답에 이메일이 없어 기본값으로 설정: " + email);
        }

        System.out.println("Kakao Attributes: " + attributes);
        System.out.println("kakao id : " + id);
        System.out.println("kakao email : " + email);

        return new OAuthAttributes(
                attributes,
                userNameAttributeName,
                (String) profile.get("nickname"),
                email,
                (String) profile.get("profile_image_url"),
                id
        );
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName,
                                            Map<String, Object> attributes) {
        return new OAuthAttributes(attributes,
                userNameAttributeName,
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("picture"),
                (String) attributes.get("sub"));
    }

//
//    public User toEntity() {
//        // 객체를 만든다.
//        return new User(name, email, picture);
//    }

}
