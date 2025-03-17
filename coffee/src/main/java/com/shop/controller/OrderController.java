package com.shop.controller;


import com.shop.constant.ItemSellStatus;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.entity.*;
import com.shop.repository.*;
import com.shop.service.ItemService;
import com.shop.service.MemberService;
import com.shop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 주문 관련 요청을 처리하는 컨트롤러
 */
@Controller
@RequiredArgsConstructor // final 필드에 대해 생성자 자동 생성
public class OrderController {
    private final OrderService orderService; // 주문 관련 비즈니스 로직 처리 서비스
    private final MemberService memberService;
    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final MemberRepository memberRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;


    /**
     * 단일 상품 주문 처리
     *
     * @param orderDto 주문 정보를 담은 객체
     * @param bindingResult 유효성 검사 결과를 담는 객체
     * @param principal 현재 로그인된 사용자 정보
     * @return 주문 ID 또는 에러 메시지를 포함한 ResponseEntity
     */
    @PostMapping(value = "/order")
    public @ResponseBody ResponseEntity<?> order(@RequestBody @Valid OrderDto orderDto,
                                                 BindingResult bindingResult,
                                                 Principal principal) {



        // 유효성 검사 실패 시 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        // 현재 로그인된 사용자 이메일 가져오기
        String userid = principal.getName();
        Long orderId;
        int membership;

        try {
            // 주문 처리 서비스 호출
            orderId = orderService.order(orderDto, userid);
            Member member = memberService.findMemberByUserid(userid);
            membership = member.getMembershipSave();
            int usedMembership = orderDto.getUsedMembership();
            member.setMembership(member.getMembership() - usedMembership);
            memberRepository.save(member);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("orderId",orderId);
        response.put("membership",membership);

        // 성공적으로 생성된 주문 ID 반환
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 주문 내역 조회
     *
     * @param page      요청한 페이지 번호 (Optional)
     * @param principal 현재 로그인된 사용자 정보
     * @param model     뷰에 데이터를 전달하기 위한 객체
     * @return 주문 내역 페이지 이름
     */
    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, Principal principal, Model model) {
        // 페이징 설정
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);

        // 로그인된 사용자의 주문 내역 조회
        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        // 뷰에 데이터 전달
        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);
        return "order/orderHist"; // 주문 내역 페이지 반환
    }

    /**
     * 주문 취소 처리
     *
     * @param orderId   취소할 주문 ID
     * @param principal 현재 로그인된 사용자 정보
     * @return 성공 또는 실패 상태를 포함한 ResponseEntity
     */
    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId, Principal principal) {
        // 주문 취소 권한 확인(관리자가 취소를 하는거라서 주석처리함)
//        if (!orderService.validateOrder(orderId, principal.getName())) {
//            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
//        }

        // 주문 취소 처리
        orderService.cancelOrder(orderId);
        return new ResponseEntity<Long>(orderId, HttpStatus.OK); // 취소된 주문 ID 반환
    }


    @PostMapping("/order/checkstock")
    public ResponseEntity checkStock(@RequestBody OrderDto orderDto) {
        System.out.println("OrderDto: " + orderDto);
        System.out.println("Item ID: " + orderDto.getItemId());
        System.out.println("Count: " + orderDto.getCount());
        try {
            Item item = itemService.getItem(orderDto.getItemId());
            System.out.println(item);

            if (item == null) {
                System.out.println("여기야?");
                return ResponseEntity.ok(Map.of(
                    "status", "FAIL",
                    "message", "상품 ID " + orderDto.getItemId() + "이(가) 존재하지 않습니다."
                ));
            }

            if (item.getItemSellStatus() == ItemSellStatus.SOLD_OUT) {
                return ResponseEntity.ok(Map.of(
                    "status","FAIL",
                    "message","\n"+item.getItemNm() + "' 품절된 상품입니다."
                ));
            }

            // 재고 확인
            if (orderDto.getCount() > item.getStockNumber()) {
                System.out.println("아님 여기야??");
                return ResponseEntity.ok(Map.of(
                    "status", "FAIL",
                    "message", "\n"+item.getItemNm() + "'의 재고가 부족합니다.\n 현재 재고: " + item.getStockNumber()
                ));
            }

            System.out.println("Found Item: " + item.getItemNm() + ", Stock: " + item.getStockNumber());
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (Exception e) {
            System.out.println("Error during stock check: " + e.getMessage());
            return ResponseEntity.ok(Map.of(
                "status", "FAIL",
                "message", "잘못된 요청입니다: " + orderDto.getItemId()
            ));
        }
    }


    // 통합결제 창 만들기
    @PostMapping("/order/total")
    @ResponseBody
    public ResponseEntity<?> showOrderPage(
        @RequestBody Map<String, Object> requestBody,
        Principal principal,
        HttpSession session) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"로그인이 필요합니다.\"}");
        }

        try {
            String userid = principal.getName();
            Member member = memberRepository.findByUserid(userid);

            session.setAttribute("member", member);

            if (requestBody.containsKey("itemId") && requestBody.containsKey("count")) {


                // 단일 상품 주문 처리
                Long itemId = Long.valueOf(requestBody.get("itemId").toString());
                Integer count = Integer.valueOf(requestBody.get("count").toString());


                System.out.println(principal.getName());
                System.out.println("itemId : " + itemId);
                System.out.println("count : " + count);

                if (count <= 0) {
                    throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
                }

                Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                int totalAmount = item.getPrice() * count;
                String imgUrl = itemImgRepository.findByItemIdAndRepImgYn(itemId, "Y").getImgUrl();

                OrderDto orderDto = new OrderDto();
                orderDto.setItemId(itemId);
                orderDto.setCount(count);
                orderDto.setTotalPrice(totalAmount);
                orderDto.setFinalPrice(orderDto.getTotalPrice());
                orderDto.setImgUrl(imgUrl);
                orderDto.setItemNm(item.getItemNm());
                session.setAttribute("orderType", "single");
                session.setAttribute("orderDto", orderDto);
                session.setAttribute("membership", member.getMembership());

                System.out.println(session);
                // 장바구니 주문 처리
            } else if (requestBody.containsKey("cartOrderDtoList")) {

                List<Map<String, Object>> cartOrderDtoList =
                    (List<Map<String, Object>>) requestBody.get("cartOrderDtoList");

                System.out.println("cartOrderDtoList : " + cartOrderDtoList);


                List<CartOrderDto> cartOrderDtos = new ArrayList<>();

                for (Map<String, Object> cartOrder : cartOrderDtoList) {
                    Long cartItemId = Long.valueOf(cartOrder.get("cartItemId").toString());
                    Integer count = Integer.valueOf(cartOrder.get("count").toString());

                    CartItem cartItem = cartItemRepository.findById(cartItemId)
                        .orElseThrow(() -> new IllegalArgumentException("장바구니 항목이 존재하지 않습니다."));
                    int price = cartItem.getItem().getPrice();
                    int itemTotalPrice = price * count;
                    String imgUrl = itemImgRepository.findByItemIdAndRepImgYn(cartItem.getItem().getId(), "Y").getImgUrl();


                    CartOrderDto dto = new CartOrderDto();
                    dto.setCartItemId(cartItemId);
                    dto.setCount(count);
                    dto.setPrice(price);
                    dto.setTotalprice(itemTotalPrice);
                    dto.setFinalprice(dto.getTotalprice());
                    dto.setImgUrl(imgUrl);
                    dto.setItemNm(cartItem.getItem().getItemNm());

                    cartOrderDtos.add(dto);
                }

                session.setAttribute("orderType", "cart");
                session.setAttribute("cartOrderDtoList", cartOrderDtos);
                session.setAttribute("membership", member.getMembership());


                System.out.println(session);

            } else {
                throw new IllegalArgumentException("유효한 데이터가 제공되지 않았습니다.");
            }

            return ResponseEntity.ok(Map.of("message", "Success"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 처리 중 문제가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/order/orderTotal")
    public String showOrderTotalPage(HttpSession session, Model model) {
        String orderType = (String) session.getAttribute("orderType");
        int totalAmount = 0;


        if ("single".equals(orderType)) {
            OrderDto orderDto = (OrderDto) session.getAttribute("orderDto");
            totalAmount = orderDto.getTotalPrice();
            model.addAttribute("orderDto", orderDto);
            model.addAttribute("totalPrice", orderDto.getTotalPrice());
            model.addAttribute("count", session.getAttribute("count"));
        } else if ("cart".equals(orderType)) {
            List<CartOrderDto> cartOrderDtoList =
                (List<CartOrderDto>) session.getAttribute("cartOrderDtoList");
            totalAmount = cartOrderDtoList.stream()
                .mapToInt(CartOrderDto::getTotalprice)
                .sum();

            model.addAttribute("cartOrderDtoList", cartOrderDtoList);

        }
        Member member = (Member) session.getAttribute("member");


        model.addAttribute("member", member);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("membership", session.getAttribute("membership"));

        return "order/orderTotal";
    }

    @PostMapping("/order/updateBuyerInfo")
    public ResponseEntity<?> updateBuyerInfo(
        @RequestParam String name,
        @RequestParam String postcode,
        @RequestParam String tel,
        @RequestParam String address,
        Principal principal) {

        try {
            System.out.println("구매자 정보 수정 요청: " + name + ", " + tel + ", " + postcode + ", " + address);
            String userid = principal.getName();
            Member member = memberRepository.findByUserid(userid);
            member.setName(name);
            member.setTel(tel);
            member.setPostcode(postcode);
            member.setAddress(address);
            memberRepository.save(member);

            System.out.println("회원 정보 수정 성공");
            return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            System.err.println("회원 정보 수정 중 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 정보 수정 중 오류가 발생했습니다.");
        }
    }

    //토탈 주문확인
    @GetMapping("/admin/orders")
    public String allOrders(@RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "searchBy", required = false) String searchBy,
                            @RequestParam(value = "searchQuery", required = false) String searchQuery,
                            Model model) {


        Page<Order> ordersFilter = orderService.getOrderPage(page, searchBy, searchQuery);

        model.addAttribute("orders", ordersFilter); // 검색 결과 또는 전체 데이터를 저장
        model.addAttribute("searchBy", searchBy);
        model.addAttribute("searchQuery", searchQuery);


        int currentPage = Math.min(Math.max(page, 0), ordersFilter.getTotalPages() - 1);

        int maxPage = 5;
        int start = Math.max(1, currentPage - (maxPage - 1) / 2);
        int end = Math.min(start + (maxPage - 1), ordersFilter.getTotalPages());


        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("page", page);

        return "order/orderList";
    }
    //토탈주문에서 해당 주문건
    @GetMapping("/admin/orders/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        try {
            OrderHistDto orderHistDto = orderService.getOrderDetail(orderId);
            model.addAttribute("order", orderHistDto);
            return "order/orderDtl";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "주문 상세 정보를 불러오는 중 오류가 발생했습니다.");
            return "/";
        }
    }

    //주문 취소 요청
    @PostMapping("/order/{orderId}/cancel-request")
    public @ResponseBody ResponseEntity<?> requestCancelOrder(@PathVariable Long orderId, Principal principal) {
        try {

            if (!orderService.validateOrder(orderId, principal.getName())) {
                return new ResponseEntity<>("주문 취소 요청 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }

            orderService.requestCancelOrder(orderId);
            return new ResponseEntity<>("주문 취소 요청이 접수되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("주문 취소 요청 처리 중 오류가 발생했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }
    @PostMapping("/admin/order/{orderId}/approve-cancellation")
    public @ResponseBody ResponseEntity<?> approveCancellation(@PathVariable Long orderId) {
        try {
            System.out.println("환불 요청 시도 컨트롤러");
            orderService.approveCancellation(orderId);
            orderService.cancelOrder(orderId);
            return new ResponseEntity<>("환불 처리가 완료되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //주문 통계
    @GetMapping("/admin/stats")
    public String orderStats(Model model) {
        return "order/orderStats";
    }
}