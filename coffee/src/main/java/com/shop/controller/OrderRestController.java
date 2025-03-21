package com.shop.controller;


import com.shop.dto.OrderHistDto;
import com.shop.entity.Order;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderRestController {


    private final OrderService orderService;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getOrders(
            @PathVariable("userId") String userId,
            @RequestParam("page") Optional<Integer> page,  // 페이지 번호 (기본값 0)
            @PageableDefault(size = 10, sort = "orderId", direction = Sort.Direction.DESC) Pageable pageable) {

        System.out.println("플러터 맵핑확인 orderHist");
        System.out.println("userid : " + userId);
        System.out.println("플러터 맵핑확인 orderHist");



        try {
            // 페이지 번호가 있을 경우, pageable을 수정하여 페이징
            int currentPage = page.orElse(0); // 기본 페이지 번호는 0
//            Pageable pageRequest = PageRequest.of(currentPage, 10, Sort.by(Sort.Direction.DESC, "orderId"));
            Pageable pageRequest = PageRequest.of(currentPage, 10, Sort.by(Sort.Direction.DESC, "orderDate"));
            int count = 1;
            // 사용자 주문 내역 조회 (페이징 처리)
            Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(userId, pageRequest,count);
            System.out.println("컨트롤 orderHistDtoList : " + orderHistDtoList);

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderHistDtoList.getContent());
            response.put("totalPages", orderHistDtoList.getTotalPages());
            response.put("totalElements", orderHistDtoList.getTotalElements());
            response.put("currentPage", orderHistDtoList.getNumber());

            System.out.println("return 하기 전에 response: " + response);
            System.out.println("orderHistDtoList :  " +orderHistDtoList);

            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "*")
                    .body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            System.out.println("errorResponse : "+errorResponse);
            System.out.println("errorResponse : "+errorResponse);
            System.out.println("errorResponse : "+errorResponse);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}