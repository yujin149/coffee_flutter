package com.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket 설정 클래스
 * 실시간 채팅을 위한 WebSocket 연결, 메시지 브로커, 보안 설정을 담당
 */
@Configuration  
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커 기능을 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커(메시지 중개자) 설정
     * 메시지 브로커는 발신자로부터 받은 메시지를 수신자에게 전달하는 중간 매개체
     * [사용자A] ──메시지─→ [메시지브로커] ──메시지─→ [사용자B]
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 구독(subscribe)을 위한 prefix 설정
        // 클라이언트가 메시지를 구독할 수 있는 endpoint
        // 예: /topic/admin, /topic/user/123
        config.enableSimpleBroker("/topic");
        
        // 메시지 발행(publish)을 위한 prefix 설정
        // 클라이언트가 메시지를 발행할 때 사용할 endpoint
        // 예: /app/counsel
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * WebSocket 연결을 위한 엔드포인트 설정
     * 클라이언트가 WebSocket 연결을 맺기 위한 진입점 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/counsel")  // WebSocket 연결 주소: ws://도메인/counsel
               .setAllowedOriginPatterns("*")  // CORS 설정: 모든 도메인 허용
               .withSockJS()  // SockJS 지원 활성화 (WebSocket을 지원하지 않는 브라우저를 위한 대체 옵션)
               .setClientLibraryUrl("/webjars/sockjs-client/1.5.1/sockjs.min.js")  
               .setWebSocketEnabled(true)  
               .setSessionCookieNeeded(false);  // 세션 쿠키 사용 안함
    }

    /**
     * WebSocket 메시지 전송 관련 제한 설정
     * 메시지 크기, 버퍼 크기, 전송 시간 제한을 설정하여 서버 리소스 보호
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024)      // 메시지 최대 크기: 128KB
                   .setSendBufferSizeLimit(512 * 1024)    // 버퍼 최대 크기: 512KB
                   .setSendTimeLimit(20000);              // 전송 제한 시간: 20초
    }

    /**
     * 클라이언트로부터 들어오는 메시지 처리 채널 설정
     * 메시지 전송 전 보안 검사 및 사용자 인증 처리
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // STOMP 메시지 헤더에 접근하기 위한 accessor 생성
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                // 클라이언트가 연결을 시도할 때 (CONNECT 명령어)
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Spring Security에서 현재 인증된 사용자 정보를 가져옴
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    // WebSocket 연결에 사용자 인증 정보를 설정
                    accessor.setUser(auth);
                }
                
                return message;
            }
        });
    }
}
