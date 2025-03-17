package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.shop.service.VisitService;

//설정
@Configuration  // 이 클래스가 스프링의 Java 기반 설정 클래스임을 나타낸다.
// uploadPath = "C:/shop에 있지만 images로 나온다.
// WebMvcConfig 클래스는 WebMvcConfigurer 인터페이스를 구현하는 클래스이다.
// 이 클래스에는 웹 애플리케이션의 MVC 구성을 설정한다.
public class WebMvcConfig implements WebMvcConfigurer {
    //apprication.properties에 있는 값을 여기로 받는다
    // @Value 어노테이션을 사용하여 uploadPath라는 프로퍼티 값을 읽어온다.
    @Value("${uploadPath}") //application.properties 설정한 uploadPath
    String uploadPath;
    //uploadPath = "C:/shop
    // images/item/xxx.jpg

    //방문자 수 조회
    private final VisitService visitService;

    public WebMvcConfig(VisitService visitService) {
        this.visitService = visitService;
    }

    @Override
    //Resource 추가 "/images/**"  ,// 정적 리소스에 대한 핸들러를 등록하는 역할을 한다.
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**") //이게 uploadPath랑 동기시켜서 images로 나옴
                .addResourceLocations("classpath:/static/images/") // 여기서 images 를 uploadPath로 c:로 연결을 한 상태라 이걸 추가 안하면 /images 패키지에 올라온거는 무시가 됨
                // /images로 시작하는 경우 uploadPath에 설정한 폴더를 기준으로 파일을
                //읽어 오도록 설정
                .addResourceLocations(uploadPath);// 로컬 컴퓨터에서 root 경로를 설정
    }

    //방문자 수 조회
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new VisitInterceptor(visitService))
            .addPathPatterns("/**")
            .excludePathPatterns("/css/**", "/js/**", "/img/**", "/images/**", "/error/**");
    }

    //  CORS 설정 추가
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:8080", "http://10.0.2.2:8080", "http://localhost", "http://10.0.2.2")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
            .allowedHeaders("*")
            .exposedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600);
    }

}
