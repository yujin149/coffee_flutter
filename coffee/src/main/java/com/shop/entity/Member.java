package com.shop.entity;


import com.shop.constant.LoginType;
import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import com.shop.repository.MemberRepository;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Entity
@Table(name = "member") //테이블 명
@Getter
@Setter
@ToString
public class Member {

    @Id
    @Column(name = "member_id") //
    @GeneratedValue(strategy = GenerationType.AUTO) //데이터증가시 1씩증가
    private Long id;
    //알아서 설정


    @Column(nullable = false, unique = true , name = "userid")
    private String userid;


    private String name;


    //소셜로그인 다른 플랫폼 중복 이메일 방지
    @Column(nullable = false)
    private String provider = "NORMAL";

    //중복 허용x
    @Column(nullable = false, unique = true)
    private String email;
    //알아서
    private String password;
    //알아서
    private String postcode; // 우편번호
    private String address;

    @Column(unique = true)
    private String tel;

    private String birthdate;

    private Integer membership =0;

    private Integer membershipSave;


    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    private String picture;



    //뷰쪽에 오는 모델을 그대로 db로 바꾸기 위한 메소드(중간다리역할)
    //static 바로 메모리
    //PasswordEncoder autoWired로 불러서 맵핑시켜준다.
    public static Member createMember(MemberFormDto memberFormDto,
                                      PasswordEncoder passwordEncoder,
                                      MemberRepository memberRepository) {

        Member member = new Member();
        member.setUserid(memberFormDto.getUserid());
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setPostcode(memberFormDto.getPostcode());
        member.setAddress(memberFormDto.getAddress());
        member.setTel(memberFormDto.getTel());
        member.setBirthdate(memberFormDto.getBirthdate());
        member.setLoginType(LoginType.NORMAL);
        //여기서 패스워드를 암호화 해준다.
        String password= passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setProvider("NORMAL");

        long memberCount = memberRepository.count();
        if (memberCount == 0) {
            member.setRole(Role.ADMIN); //권한주기
        } else {
            member.setRole(Role.USER);
        }

        return member;
    }

    //비밀번호 변경,비밀번호찾기,개인정보 수정 3군데 사용하고 있어서 해당 폼에 없는 컬럼은
    //null값이 넘어올수 있음 그래서 null이 아니거나 띄어쓰기 확인후 정보 수정을 함
    public void updateMember(MemberFormDto memberFormDto,PasswordEncoder passwordEncoder) {

        //비밀번호
        if (memberFormDto.getPassword() != null && !memberFormDto.getPassword().isEmpty()) {
            this.password = passwordEncoder.encode(memberFormDto.getPassword());
        } else {
            System.out.println("비밀번호 미수정: 기존 비밀번호 유지");
        }
        //이름
        if (memberFormDto.getName() != null && !memberFormDto.getName().isEmpty()) {
            this.name = memberFormDto.getName();
        }
        this.email = memberFormDto.getEmail();
        // 우편번호
        if (memberFormDto.getPostcode() != null && !memberFormDto.getPostcode().isEmpty()) {
            this.postcode = memberFormDto.getPostcode();
        }

        // 주소
        if (memberFormDto.getAddress() != null && !memberFormDto.getAddress().isEmpty()) {
            this.address = memberFormDto.getAddress();
        }

        // 전화번호
        if (memberFormDto.getTel() != null && !memberFormDto.getTel().isEmpty()) {
            this.tel = memberFormDto.getTel();
        }

        // 생년월일
        if (memberFormDto.getBirthdate() != null && !memberFormDto.getBirthdate().isEmpty()) {
            this.birthdate = memberFormDto.getBirthdate();
        }
    }

    public void update(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    //소셜로그인 저장
    public static Member createSocialMember(String email, String name, String picture , String provider) {

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("소셜 로그인에 이메일 정보가 필요합니다.");
        }

        Member member = new Member();
        member.setEmail(email);
        member.setName(name);
        member.setPicture(picture);
//        member.setPostcode("");
//        member.setAddress("");
//        member.setTel("");
//        member.setBirthdate("");
        member.setPostcode(null); // 빈 문자열 대신 null
        member.setAddress(null); // 빈 문자열 대신 null
        member.setTel(null);     // 빈 문자열 대신 null
        member.setBirthdate(null);
        member.setProvider(provider);
        member.setLoginType(LoginType.SOCIAL);
        member.setRole(Role.USER);

        if (email != null) {
            member.setUserid(email); // Principal ID를 userid로 설정
        } else {
            member.setUserid("social_" + UUID.randomUUID().toString()); // 기본값
        }
        System.out.println("소셜 로그인 userid: " + member.getUserid());
        System.out.println("createSocialMember - Member: " + member);
        return member;
    }

    public int membershipUpdate(int total) {
        this.membershipSave = Math.round(total*0.01f);
        this.membership += this.membershipSave;
        return this.membershipSave;
    }


}
