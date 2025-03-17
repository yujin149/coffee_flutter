package com.shop.config;



import com.shop.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.net.URLEncoder;

@Configuration  //config 를 설정하는 어노테이션
@EnableWebSecurity// 웹 보안을 가능하게 하는 어노테이션
public class SecurityConfig {

    //싱글턴의 장점은 문제가 생기면 한군데만 찾으면 되지만 속도가 느리다.
    @Autowired
    MemberService memberService;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    //컨테이너 올라가있으면 AutoWired로 불러서 사용할수있다
    @Bean //(Bean 객체) 스프링 컨테이너에 올라가는 객체 / 빌더패턴으로 바꿈
    //SecurityFilterChain: Spring Security의 필터 체인을 나타내는 인터페이스입니다. 요청이 들어올 때, 여러 보안 필터가 이 체인을 통해 동작합니다.
    //HttpSecurity :Spring Security의 보안 설정을 정의할 수 있는 API
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        //로그인을 Security한테 물어봄
        http
                .authorizeHttpRequests(auth -> auth
                        //security가  이 url에 있으면 확인을 안하고 통과 시켜줌
                        //permitAll() 을 사용하면 해당 조건들(patterns)에 들어오면 접근을 가능하게 해준다
                        .requestMatchers("/","/members/**","/item/**","/images/**","/api/**").permitAll()
                        .requestMatchers("/css/**","/js/**","/img/**","/favicon.ico","/error").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/members/sendEmail", "/members/checkCode").permitAll()
                        .requestMatchers("/","/members/**","/item/**","/images/**").permitAll()
                        // "/admin/**" 패턴은 ADMIN 권한 필요
                        .requestMatchers("/admin/**").hasRole("ADMIN")// /admin/** admin 밑에 있는 하위 어떤 url 포함 됩니다.
                        .requestMatchers("/admin/orders/stats/daily-details/excel").hasRole("ADMIN") //엑셀 다운로드 권한 필요
                        .requestMatchers("/mypage/**").permitAll()
                        .requestMatchers("/chat").permitAll()
                        .requestMatchers("/order/total").permitAll()
                        // WebSocket 엔드포인트 허용
                        .requestMatchers("/counsel/**").permitAll()
                        .requestMatchers("/topic/**").permitAll()
                        .requestMatchers("/app/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        .requestMatchers("/debug/**").permitAll()  //디버그 확인용
                        .requestMatchers("/all/**").permitAll()  // 전체 메뉴 페이지 접근 허용
                        .requestMatchers("/coffee/**").permitAll()  // 커피 페이지 접근 허용
                        .requestMatchers("/bean/**").permitAll()  // 원두 페이지 접근 허용
                        .requestMatchers("/desert/**").permitAll()  // 디저트 페이지 접근 허용
                        .requestMatchers("/store/**").permitAll()  // 매장 안내 페이지 접근 허용
                        .requestMatchers("/board/**").permitAll()  // 공지사항 관련 URL 접근 허용
                        .requestMatchers("/gallery/**").permitAll()  // 갤러리 관련 URL 접근 허용
                        .requestMatchers("/inquiry/**").permitAll()  // 문의하기 관련 URL 접근 허용
                        .requestMatchers("/search/**").permitAll()
                        .anyRequest().authenticated()// 위에를 제외한 모든 url 맵핑은 모두 로그인이 되어야 접속 가능
                )
                .formLogin(formLogin -> formLogin // form 로그인 경우 여기로 온다. 로그인을 누르면 여기로 온다
                        .loginPage("/members/login")    // 로그인 페이지는 /members/login (url) 데이터를 받는다
                        .successHandler(new CustomAuthenticationSuccessHandler()) // 성공 핸들러 등록 아래 주석은 보내주기만 하는데
//                        .defaultSuccessUrl("/") // 로그인 성공하면 "/"(url) 여기서 보내준다
                        .usernameParameter("userid") // 로그인에 필요한 파라미터("email")
                        .failureUrl("/members/login/error") // 실패시 이동 url /members/login/error

                ).logout(logout -> logout
                                //로그아웃을 누르면 여기로 온다 //로그아웃 실행
                                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                                .logoutSuccessUrl("/") // 로그아웃 성공시 실행
                        //구글,네이버,카카오 로그인 성공하면
                )
                //.csrf( csrf -> {})
                //.csrf(csrf -> csrf.ignoringRequestMatchers("/cart/**", "/api/**"))
                .csrf(csrf -> csrf
                    ///members/**와 /images/**를 CSRF 예외 목록에 추가
                    .ignoringRequestMatchers(
                        "/cart/**",
                        "/api/**",
                        "/order/**",
                        "/counsel/**",
                        "/topic/**",
                        "/app/**",
                        "/ws/**",
                        "/webjars/**",
                        "/members/**",
                        "/images/**",
                        "/admin/orders/stats/daily-details/excel"
                    )

                )
                .oauth2Login(oauthLogin -> oauthLogin
                        .defaultSuccessUrl("/") // 로그인 성공하면 "/"(url) 여기서 보내준다
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                //customOAuth2UserService
                                .userService(customOAuth2UserService))
                );
        //exceptionHandling 예외처리 핸들링 예외처리가 발생하면 CustomAuthenticationEntryPoint 클래스 위임
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
        );
        return http.build();
    }

    @Bean //패스워드 암호화 해주는 객체 만약 1234가 비밀번호이면 암호화해서 입력
    //패스워드 인코드 하는 객체
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Autowired
    //AuthenticationManagerBuilder -> UserDetailService를 구현 하고 그 객체 MemberService 지정과
    //동시에 비밀번호를 암호화
    public void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }
}
