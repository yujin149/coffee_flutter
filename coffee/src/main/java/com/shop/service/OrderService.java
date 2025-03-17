package com.shop.service;

import com.shop.constant.OrderStatus;
import com.shop.constant.ItemMenu;
import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.*;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;



    public Long order(OrderDto orderDto, String userid){
        // 로그 추가
        System.out.println("OrderDto 데이터: " + orderDto);
        System.out.println("userid: " + userid);

        // 필드 확인
        if (orderDto.getItemId() == null || orderDto.getCount() <= 0) {
            throw new IllegalArgumentException("필수 데이터가 누락되었습니다.");
        }

        // 상품 조회
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 사용자 정보 조회
        Member member = memberRepository.findByUserid(userid);

        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem);


        int usedMembership = orderDto.getUsedMembership();
        int finalPrice = orderDto.getPaidAmount();

        System.out.println("일반주문 서비스 확인 usedMembership: "+usedMembership);
        System.out.println("일반주문 서비스 확인 finalPrice: "+finalPrice);

        int price = orderDto.getPaidAmount();
        int goMembership = member.membershipUpdate(price);

        // 주문 생성
        Order order = Order.createOrder(member,orderItemList, usedMembership, finalPrice , goMembership);
        // 결제 정보 추가 (필요하다면 Order 엔티티 수정 필요)
        order.setImpUid(orderDto.getImpUid());
        order.setMerchantUid(orderDto.getMerchantUid());
        order.setPaidAmount(orderDto.getPaidAmount());
        order.setGoMembership(goMembership);

        //적립금




        // 주문 저장
        orderRepository.save(order);

        return order.getId();
    }
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String userid, Pageable pageable){
        List<Order> orders = orderRepository.findOrders(userid,pageable); // 주문 리스트
        Long totalCount = orderRepository.countOrder(userid); // 총 주문 수
        List<OrderHistDto> orderHistDtos = new ArrayList<>(); // 주문 히스토리 리스트

        for(Order order : orders){ // 주문 틀 -> 주문
            OrderHistDto orderHistDto = new OrderHistDto(order); // 주문 히스토리 객체생성
            List<OrderItem> orderItems = order.getOrderItems(); // 주문 -> 주문 아이템들
            for(OrderItem orderItem : orderItems){ // 주문 아이템들 -> 주문 아이템
                // 주문 아이템 -> item id를 추출 해서 대표이미지를 받습니다. ItemImg
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(),
                        "Y");
                // 주문 아이템, 대표이미지 URL을 이용해서 OrderItemDto 객체를 생성
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                // 주문 히스토리 -> 주문 아이템 리스트에 추가
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            // 주문 히스트리스트에 주문히스토리를 추가
            orderHistDtos.add(orderHistDto);
        }
        // pageImpl 주문히스토리 리스트, 페이지 세팅, 총 갯수
        return new PageImpl<OrderHistDto>(orderHistDtos,pageable,totalCount);
    }

    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId,String userid){
        Member curMember = memberRepository.findByUserid(userid);
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        Member savedMember = order.getMember();

        if(!StringUtils.equals(curMember.getUserid(),savedMember.getUserid())){
            return false;
        }
        return true;
    }
    public void cancelOrder(Long orderId){

        System.out.println("환불 재고수정 서비스");
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        System.out.println("환불 재고수정 서비스 2: " + order);
        order.cancelOrder();
    }

    public Long orders(List<OrderDto> orderDtoList, String userid, int usedMembership ,String  impUid,String merchantUid){
        // Member 엔티티 객체 추출
        Member member = memberRepository.findByUserid(userid);
        // 주문 Item 리스트 객체 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        int totalOrderPrice = 0;

        // 주문 Dto List에 있는 객체만큼 반복
        for(OrderDto orderDto : orderDtoList){
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);
            // 주문 Item 생성
            OrderItem orderItem = OrderItem.createOrderItem(item,orderDto.getCount());
            // 주문 Item List에 추가
            orderItemList.add(orderItem);
            totalOrderPrice += orderItem.getTotalPrice();
        }
        int finalPrice = totalOrderPrice-usedMembership;

        int goMembership = member.membershipUpdate(finalPrice);


///////////////// 주문 Item List 가 완성 ////////////////////////
        // 주문 Item 리스트 , Member 를 매개변수로 넣고
        // 주문서 생성
        Order order = Order.createOrder(member,orderItemList,usedMembership,finalPrice,goMembership , impUid , merchantUid);
        // 주문서 저장
        orderRepository.save(order);

        return order.getId();
    }

    //단일 결제 재고 확인
    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품 ID가 유효하지 않습니다."));
    }

    //토탈 주문 확인
    public Page<Order> getOrderPage(int page, String searchBy, String searchQuery) {
        Pageable pageable = PageRequest.of(page, 5);

        if (searchBy == null || searchQuery == null || searchQuery.isEmpty()) {
            return orderRepository.findAllByOrderByIdDesc(pageable);
        }
        switch (searchBy) {
            case "orderId":
                try {
                    return orderRepository.findByIdOrderByIdDesc(Long.parseLong(searchQuery), pageable);
                } catch (NumberFormatException e) {
                    return Page.empty(pageable);
                }
            case "userid":
                return orderRepository.findByMember_UseridContainingOrderByIdDesc(searchQuery, pageable);
            case "name":
                return orderRepository.findByMember_NameContainingOrderByIdDesc(searchQuery, pageable);
            case "status":
                try {
                    Map<String, String> statusMap = Map.of(
                            "주문", "ORDER",
                            "취소", "CANCEL",
                            "요청", "CANCEL_REQUEST"
                    );
                    String englishStatus = statusMap.getOrDefault(searchQuery, searchQuery.toUpperCase());
                    return orderRepository.findByOrderStatusOrderByIdDesc(OrderStatus.valueOf(englishStatus), pageable);
                } catch (IllegalArgumentException e) {
                    return Page.empty(pageable);
                }
            case "orderDate":
                try {
                    // 날짜를 문자로 검색
                    if (!searchQuery.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                        return orderRepository.findByOrderDateContainingOrderByIdDesc(searchQuery, pageable);
                    }

                    LocalDateTime orderDate = LocalDateTime.parse(searchQuery, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    System.out.println("orderDate : "+orderDate);
                    return orderRepository.findByOrderDateOrderByIdDesc(orderDate, pageable);
                } catch (DateTimeParseException e) {
                    return Page.empty(pageable);
                }
            default:
                return orderRepository.findAllByOrderByIdDesc(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByIdDesc(pageable); // 페이징된 주문 리스트 반환
    }

    @Transactional(readOnly = true)
    public OrderHistDto getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));

        OrderHistDto orderHistDto = new OrderHistDto(order);
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem orderItem : orderItems) {
            ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(), "Y");
            OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
            orderHistDto.addOrderItemDto(orderItemDto);
        }

        return orderHistDto;
    }

    @Transactional
    public void requestCancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다."));

        order.setOrderStatus(OrderStatus.CANCEL_REQUEST);
        orderRepository.save(order);
    }

    private String getIamportAccessToken() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> request = Map.of(
                    "imp_key", "4841603528840108",
                    "imp_secret", "3K0zLN3vBIRMxLBVOpqzQ8U2LuZgpQqlp8SssIPoEO3a9Ut6DJUS4szAczaA9sqX7kecpsrSA7fwC9Dy"
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.iamport.kr/users/getToken", request, Map.class);

            System.out.println("아임포트 토큰 응답: " + response.getBody()); // 응답 확인

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody().get("response");
                if (responseBody != null) {
                    return (String) responseBody.get("access_token");
                }
            }
            throw new IllegalStateException("아임포트 토큰 발급 실패: 응답이 올바르지 않습니다.");
        } catch (Exception e) {
            System.err.println("아임포트 토큰 발급 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("아임포트 토큰 발급 실패", e);
        }
    }


    private void processRefundWithIamport(String impUid, int amount) throws IOException {
        System.out.println("impUid: " + impUid); // 로그로 impUid 출력
        System.out.println("amount: " + amount); // 로그로 amount 출력
        // 아임포트 액세스 토큰 발급
        String accessToken = getIamportAccessToken();
        System.out.println("Access Token: " + accessToken); // 토큰 출력

        // 환불 요청 API 호출
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "imp_uid", impUid,
                "amount", amount,
                "reason", "취소 요청 승인"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.iamport.kr/payments/cancel", request, Map.class);

        // 응답 출력
        System.out.println("아임포트 API 응답: " + response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("환불 처리 실패: " + response.getBody());
        }
    }


    public void approveCancellation(Long orderId) {

        System.out.println("환불요청 서비스");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다."));

        // 상태 검증
        if (order.getOrderStatus() != OrderStatus.CANCEL_REQUEST) {
            throw new IllegalStateException("취소 요청 상태가 아닌 주문은 승인할 수 없습니다.");
        }

        // 아임포트 환불 처리
        try {
            processRefundWithIamport(order.getImpUid(), order.getPaidAmount());
        } catch (Exception e) {
            throw new IllegalStateException("아임포트 환불 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        orderRepository.save(order);
    }

    //월별 주문 총액 조회
    public Long getMonthlyOrderTotal(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        
        return orderRepository.findTotalAmountByDateBetween(startOfMonth, endOfMonth);
    }

    public Map<String, Long> getDailySalesByCategory(LocalDateTime date) {
        // 디버깅을 위한 상세 주문 정보 출력
        List<Object[]> orderDetails = orderRepository.findDailyOrderDetails(date);
        System.out.println("\n=== Daily Order Details ===");
        for (Object[] detail : orderDetails) {
            System.out.println(String.format(
                "Menu: %s, Item: %s, Price: %d, Count: %d, Date: %s",
                detail[0], detail[1], detail[2], detail[3], detail[4]
            ));
        }
        System.out.println("========================\n");

        Map<String, Long> salesByCategory = new HashMap<>();
        salesByCategory.put("TOTAL", 0L);
        salesByCategory.put("COFFEE", 0L);
        salesByCategory.put("BEAN", 0L);
        salesByCategory.put("DESERT", 0L);
        
        List<Object[]> results = orderRepository.findDailySalesByCategory(date);
        System.out.println("Query results size: " + results.size());
        Long total = 0L;
        
        for (Object[] result : results) {
            ItemMenu menu = (ItemMenu) result[0];
            String category = menu.name();
            Long amount = ((Number) result[1]).longValue();
            
            System.out.println("Category: " + category + ", Amount: " + amount);
            
            switch(category) {
                case "COFFEE":
                    salesByCategory.put("COFFEE", amount);
                    break;
                case "BEAN":
                    salesByCategory.put("BEAN", amount);
                    break;
                case "DESERT":
                    salesByCategory.put("DESERT", amount);
                    break;
                default:
                    System.out.println("Unknown category: " + category);
                    break;
            }
            total += amount;
        }
        salesByCategory.put("TOTAL", total);
        
        System.out.println("Final sales by category: " + salesByCategory);
        return salesByCategory;
    }

    // 상품 판매 순위 조회
    public List<Map<String, Object>> getTopSellingItems() {
        PageRequest pageRequest = PageRequest.of(0, 15); // 상위 15개 상품
        List<Object[]> results = orderRepository.findTopSellingItems(pageRequest);
        
        List<Map<String, Object>> topItems = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("itemId", result[0]);    // 상품 ID
            item.put("itemNm", result[1]);    // 상품명
            item.put("orderCount", result[2]); // 주문 수
            topItems.add(item);
        }
        
        return topItems;
    }

    // 일별 카테고리별 주문 내역 조회
    public Page<Map<String, Object>> getDailyOrderDetails(LocalDateTime date, ItemMenu category, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Object[]> results = orderRepository.findDailyOrderDetails(date, category, pageRequest);
        
        return results.map(result -> {
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", result[0]);
            order.put("itemId", result[1]);
            order.put("itemNm", result[2]);
            order.put("price", result[3]);
            order.put("count", result[4]);
            order.put("totalPrice", result[5]);
            order.put("orderName", result[6]);
            order.put("phone", result[7]);
            order.put("address", result[8]);
            order.put("orderDate", result[9]);
            order.put("imgUrl", result[10]);
            return order;
        });
    }

    // 엑셀 다운로드용 데이터 조회
    public List<Map<String, Object>> getDailyOrderDetailsForExcel(LocalDateTime date, String category) {
        ItemMenu itemMenu = null;
        if (!"TOTAL".equals(category)) {
            itemMenu = ItemMenu.valueOf(category);
        }
        
        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Object[]> results = orderRepository.findDailyOrderDetails(date, itemMenu, pageRequest);
        
        return results.getContent().stream().map(result -> {
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", result[0]);
            order.put("itemId", result[1]);
            order.put("itemNm", result[2]);
            order.put("price", result[3]);
            order.put("count", result[4]);
            order.put("totalPrice", result[5]);
            order.put("orderName", result[6]);
            order.put("phone", result[7]);
            order.put("address", result[8]);
            order.put("orderDate", result[9]);
            order.put("imgUrl", result[10]);
            return order;
        }).collect(Collectors.toList());
    }
}
