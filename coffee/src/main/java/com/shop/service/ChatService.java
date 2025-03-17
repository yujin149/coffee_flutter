package com.shop.service;

import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class ChatService {

    private final Map<String, List<String>> keyword;

    public ChatService() {
        keyword = new LinkedHashMap<>();


        keyword.put("인기", Arrays.asList(
                "요즘 가장 인기 있는 커피는 아이스 아메리카노 입니다.",
                "많은 분들이 좋아하시는 커피는 아메리카노 입니다.",
                "최근 인기 있는 커피는 아이스 아메리카노 입니다."
        ));
        keyword.put("잘나가", Arrays.asList(
                "요즘 가장 인기 있는 커피는 아이스 아메리카노 입니다.",
                "많은 분들이 좋아하시는 커피는 아이스 아메리카노 입니다.",
                "최근 인기 있는 커피는 아이스 아메리카노 입니다."
        ));
        keyword.put("추천", Arrays.asList(
                "요즘 가장 인기 있는 커피는 아이스 아메리카노 입니다.",
                "많은 분들이 좋아하시는 커피는 아이스 아메리카노 입니다.",
                "최근 인기 있는 커피는 아이스 아메리카노 입니다."
        ));
        keyword.put("종류", Arrays.asList(
                "저희는 커피, 원두, 디저트 등 다양한 종류를 판매하고 있습니다.",
                "다양한 종류를 보유 중입니다: 커피, 원두, 디저트.",
                "저희는 커피와 디저트 그리고 원두를 모두 제공합니다."
        ));
        keyword.put("커피", Arrays.asList(
                "커피 관련 질문이네요. 인기 있는 커피를 찾으시나요?",
                "커피에 대해 알고 싶으시군요! 인기 상품이나 종류를 선택해 보세요.",
                "커피에 대한 더 많은 정보를 원하시면 1:1 문의주세요!"
        ));
        keyword.put("디저트", Arrays.asList(
                "디저트 관련 질문이네요. 인기 있는 디저트 찾으시나요?",
                "디저트에 대해 알고 싶으시군요! 인기 상품이나 종류를 선택해 보세요.",
                "디저트에 대한 더 많은 정보를 원하시면 1:1 문의주세요!"
        ));
        keyword.put("원두", Arrays.asList(
                "원두 관련 질문이네요. 인기 있는 원두를 찾으시나요 원하시나요?",
                "원두에 대해 알고 싶으시군요! 인기 상품이나 종류를 선택해 보세요.",
                "원두에 대한 더 많은 정보를 원하시면 1:1 문의주세요!"
        ));
        keyword.put("매장", Arrays.asList(
                "저희 매장은 인천 부평구에 위치해 있습니다.",
                "인천 부평구에 매장이 있으며, 방문을 환영합니다!",
                "저희 매장은 인천 부평구 에 있습니다. !"
        ));
        keyword.put("위치", Arrays.asList(
                "저희 매장은 인천 부평구 위치해 있습니다.",
                "인천 부평구에 매장이 있으며, 방문을 환영합니다!",
                "저희 매장은 인천 부평구 중심가에 있습니다. 언제든 오세요!"
        ));
        keyword.put("결제", Arrays.asList(
                "결제는 간편결제를 지원합니다.",
                "결제는 다양한 방법을 지원합니다: 간편결제.",
                "결제 방식은 간편, 또는 네이버페이를 사용할 수 있습니다."
        ));
        keyword.put("환불", Arrays.asList(
                "환불을 원하시면 고객센터로 문의하거나, 구매 내역에서 환불 요청 버튼을 눌러주세요.",
                "환불 절차는 간단합니다. 고객센터로 연락해주세요.",
                "주문 내역에서 환불 요청을 진행하거나 고객센터에 문의하세요."
        ));
        keyword.put("안녕", Arrays.asList(
                "안녕하세요! 더조은 커피 입니다.",
                "반가워요 .",
                "안녕하세요! 반갑습니다."
        ));

    }

    public String getAnswer(String userQuestion){
        String lowerQuestion = userQuestion.toLowerCase();
        Random r = new Random();

        // 키워드 매칭
        for (String key : keyword.keySet()) {
            if (lowerQuestion.contains(key)) {
                List<String> responses = keyword.get(key);

                // 랜덤으로 답변 선택
                return responses.get(r.nextInt(responses.size()));
            }
        }

        return "죄송합니다. 질문을 이해하지 못했습니다. 다시 시도해주세요!";
    }

}
