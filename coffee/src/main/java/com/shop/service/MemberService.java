package com.shop.service;


import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    //회원가입
    public Member saveMember(Member member) {
        validateDuplicateMember(member);

        //카카오 우편번호
        member.setPostcode(member.getPostcode()); // 우편번호 저장
        member.setAddress(member.getAddress()); // 주소 저장

        System.out.println(member.getUserid()+"3");
        return memberRepository.save(member); //데이터베이스에 저장을 하라는 명령
    }

    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByUserid(member.getUserid());
        //userid 불러서 객체가 있다고 나오면 이미 가입되어있는 상태이다
        if (findMember != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");//예외 발생
        }
        Member findMemberTel = memberRepository.findByTel(member.getTel());
        if (findMemberTel != null) {
            throw new IllegalStateException(("이미 가입 되어있는 전화번호입니다."));
        }

    }
    // 회원 정보 조회
    public Member getMemberInfo(String userid) {
        return memberRepository.findByUserid(userid);
    }

    //아이디 찾기
    public String findId(String email) {
        // 이메일을 이용해 회원을 찾기
//        Member member = memberRepository.findByEmail(email);
        Optional<Member>  member = memberRepository.findByEmail(email);
        System.out.println(member+"111111");

        // member가 null이면 바로 null 반환
        if (member == null) {
            return null; // 여기에서 null을 바로 반환
        }

        // member가 null이 아니면 userid 출력
        System.out.println(member.map(Member::getUserid)+ "1`11111111111111111111111111");

        try {
            System.out.println(member.map(Member::getUserid) + "222");  // userid 출력
            return member.map(Member::getUserid).orElse(null);
        } catch (Exception e) {
            // 예외가 발생한 경우 로그를 출력하고 null 반환
            e.printStackTrace();
            return null;
        }
    }


    //회원수정 처음 정보 보여주기
    @Transactional(readOnly = true)
    public MemberFormDto getMemberDtl(String MemberUserId) {

        Member member = memberRepository.findByUserid(MemberUserId);
        MemberFormDto memberFormDto = MemberFormDto.of(member);
        return memberFormDto;
    }
    //회원수정
    public String memberUpdate(MemberFormDto memberFormDto) throws Exception {

        Member member = memberRepository.findByUserid(memberFormDto.getUserid());

        if (member == null) {
            System.err.println("회원 정보 없음: " + memberFormDto.getUserid());
            throw new EntityNotFoundException("회원 정보를 찾을 수 없습니다.");
        }

        member.updateMember(memberFormDto, passwordEncoder);

        return member.getUserid();
    }




    //sequrity 검사
    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {

        Member member = memberRepository.findByUserid(userid); //DB에 email정보를 검색

        if (member == null) {
            //UsernameNotFoundException 예외를 발생시켜 Spring Security에 사용자 존재 여부를 알립니다.
            throw new UsernameNotFoundException(userid);
        }
        // Role이 enum 타입이라면, name() 또는 toString()을 사용하여 문자열로 변환
        String role = member.getRole().name();  // getRole()이 Role enum을 반환한다고 가정
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        String loginType = member.getLoginType().name(); // LoginType enum을 문자열로 변환
        GrantedAuthority loginTypeAuthority = new SimpleGrantedAuthority("LOGINTYPE_" + loginType);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(authority);
        authorities.add(loginTypeAuthority);

        //빌더패턴은 객체를 리턴한다 (검사를 하는거는 sequrity 가 한다.)
        return User.builder().username(member.getUserid())
                .password(member.getPassword())
                .authorities(authorities)  // 권한에 따라서 설정
                .build();
    }


    @Transactional(readOnly = true)
    public Member findMemberByUserid(String userid) {
        Member member = memberRepository.findByUserid(userid);
        if (member == null) {
            throw new EntityNotFoundException("Member not found");
        }
        return member;
    }
}
