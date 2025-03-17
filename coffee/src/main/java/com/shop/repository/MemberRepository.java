package com.shop.repository;


import com.shop.entity.Member;
//import com.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByName(String name);

    // 이메일 중복 여부 확인
    boolean existsByEmail(String email);

    boolean existsByUserid(String Userid);

    Optional<Member> findByEmail(String email);

    Member findByTel(String tel);


    Member findByUserid(String userid);//회원 가입이 되어있는지 확인하기 위한 ID 찾기


}
