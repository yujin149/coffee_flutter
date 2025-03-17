package com.shop.controller;

import com.shop.constant.ItemSellStatus;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.repository.MemberRepository;
import com.shop.service.CartService;
import com.shop.service.ItemService;
import com.shop.service.MemberService;
import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final ItemService itemService;

    /**
     * 장바구니에 상품 추가
     * @param cartItemDto 추가할 상품 정보
     * @param bindingResult 유효성 검사 결과
     * @param principal 현재 로그인된 사용자 정보
     * @return 추가된 장바구니 상품 ID
     */
    @PostMapping(value = "/cart")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto,
                                              BindingResult bindingResult , Principal principal){
        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for(FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        String userid = principal.getName();
        Long cartItemId;
        try{
            cartItemId = cartService.addCart(cartItemDto, userid);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(cartItemId,HttpStatus.OK);
    }

    /**
     * 장바구니 목록 조회
     * @param principal 현재 로그인된 사용자 정보
     * @param model 뷰로 데이터를 전달하기 위한 모델 객체
     * @return 장바구니 페이지 경로
     */
    @GetMapping(value = "/cart")
    public String orderHist(Principal principal, Model model){
        List<CartDetailDto> cartDetailDtoList = cartService.getCartList(principal.getName());
        model.addAttribute("cartItems" , cartDetailDtoList);
        return "cart/cartList";
    }

    /**
     * 장바구니 상품 수량 업데이트
     * @param cartItemId 업데이트할 장바구니 상품 ID
     * @param count 변경할 수량
     * @param principal 현재 로그인된 사용자 정보
     * @return 업데이트된 장바구니 상품 ID
     */
    @PatchMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       int count, Principal principal){
        if(count <= 0 ){
            return new ResponseEntity<>("최소 1개 이상 담아주세요",HttpStatus.BAD_REQUEST);
        }else if(!cartService.validateCartItem(cartItemId,principal.getName())){
            return new ResponseEntity<>("수정권한이 없습니다.",HttpStatus.FORBIDDEN);
        }
        cartService.updateCartItemCount(cartItemId,count);
        return new ResponseEntity<>(cartItemId,HttpStatus.OK);
    }

    /**
     * 장바구니 상품 삭제
     * @param cartItemId 삭제할 장바구니 상품 ID
     * @param principal 현재 로그인된 사용자 정보
     * @return 삭제된 장바구니 상품 ID
     */
    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       Principal principal){
        if(!cartService.validateCartItem(cartItemId,principal.getName())){
            return new ResponseEntity<>("수정권한이 없습니다.",HttpStatus.FORBIDDEN);
        }
        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<>(cartItemId,HttpStatus.OK);
    }

    /**
     * 장바구니 상품 주문
     * @param cartOrderDto 장바구니 주문 정보
     * @param principal 현재 로그인된 사용자 정보
     * @return 주문된 주문 ID
     */
    @PostMapping("/cart/orders")
    public @ResponseBody ResponseEntity<?> orderCartItems(@RequestBody CartOrderDto cartOrderDto, Principal principal) {
        System.out.println("Received DTO: " + cartOrderDto);

        String impUid = cartOrderDto.getImpUid();
        String merchantUid = cartOrderDto.getMerchantUid();

        System.out.println("ImpUid : " + impUid);
        System.out.println("MerchantUid : " + merchantUid);

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        if (cartOrderDtoList == null || cartOrderDtoList.isEmpty()) {
            return ResponseEntity.badRequest().body("주문할 상품을 선택해주세요.");
        }


        for (CartOrderDto cartOrder : cartOrderDtoList) {
            if (cartOrder.getCartItemId() == null) {
                return ResponseEntity.badRequest().body("잘못된 요청: 상품 ID가 비어 있습니다.");
            }

            if (!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("주문 권한이 없습니다.");
            }
        }

        int calculatedAmount = cartOrderDtoList.stream()
            .mapToInt(item -> item.getCount() * cartService.getItemPrice(item.getCartItemId()))
            .sum();
        cartOrderDtoList.forEach(item -> {
            System.out.println("CartItemId: " + item.getCartItemId() + ", Count: " + item.getCount());
        });


        int usedMembership = cartOrderDto.getUsedMembership();
        int expectedPaidAmount = calculatedAmount - usedMembership;

        // 중복 에러 잡는 디버깅 확인용
        System.out.println("=== Debugging Start ===");
        cartOrderDtoList.forEach(item -> {
            int price = cartService.getItemPrice(item.getCartItemId());
            System.out.println("CartItemId: " + item.getCartItemId() +
                ", Count: " + item.getCount() +
                ", Price: " + price +
                ", Subtotal: " + (item.getCount() * price));
        });



        if (expectedPaidAmount != cartOrderDto.getPaidAmount()) {
            System.out.println("여기로 빠지나??");
            return ResponseEntity.badRequest().body("결제 금액이 일치하지 않습니다.");
        }

        try {
            Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName(), usedMembership, impUid , merchantUid);

            //적립금
            Member member = memberRepository.findByUserid(principal.getName());
            int memberShip = member.getMembershipSave();
            System.out.println("cartMemberShip 2 :"+memberShip);
            Map<String, Object> response = new HashMap<>();
            response.put("orderId",orderId);
            response.put("memberShip",memberShip);
            member.setMembership(member.getMembership() - usedMembership);
            memberRepository.save(member);
            System.out.println("cartMemberShip 1 : "+member.getMembership());
            return ResponseEntity.ok(response);
//            return ResponseEntity.ok(orderId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * 구매자 정보 조회
     * @param principal 현재 로그인된 사용자 정보
     * @return 구매자 이름과 이메일 정보를 담은 Map 객체
     */
    @GetMapping("/cart/buyer-info")
    public ResponseEntity<Map<String, String>> getBuyerInfo(Principal principal) {
        String email = principal.getName();
        Member member = memberService.getMemberInfo(email);

        Map<String, String> buyerInfo = new HashMap<>();
        buyerInfo.put("name", member.getName());
        buyerInfo.put("email", member.getEmail());
        buyerInfo.put("phone", ""); // 전화번호가 없으므로 기본값으로 빈 문자열 제공

        return ResponseEntity.ok(buyerInfo);
    }


    //장바구니 결제 전 재고 확인
    @PostMapping("/cart/checkstock")
    public ResponseEntity checkStock(@RequestBody List<CartOrderDto> cartOrderDtoList, Principal principal) {
        System.out.println("Received CartOrderDtoList: " + cartOrderDtoList);

        for (CartOrderDto cartOrder : cartOrderDtoList) {
            System.out.println("Checking stock for CartItemId: " + cartOrder.getCartItemId());

            try {
                Long itemId = cartService.getItemIdByCartItemId(cartOrder.getCartItemId());
                Item item = itemService.getItem(itemId);

                if (item.getItemSellStatus() == ItemSellStatus.SOLD_OUT) {
                    return ResponseEntity.ok(Map.of(
                        "status","FAIL",
                        "message","\n"+item.getItemNm() + "' 품절된 상품입니다."
                    ));
                }

                if (cartOrder.getCount() > item.getStockNumber()) {
                    return ResponseEntity.ok(Map.of(
                        "status", "FAIL",
                        "message", "\n"+item.getItemNm() + "'의 재고가 부족합니다.\n 현재 재고: " + item.getStockNumber()
                    ));
                }
            } catch (Exception e) {
                System.out.println("Error during stock check: " + e.getMessage());
                return ResponseEntity.ok(Map.of(
                    "status", "FAIL",
                    "message", "잘못된 요청입니다: " + cartOrder.getCartItemId()
                ));
            }
        }
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @PostMapping("/cart/order")
    public String orderCartItems(@RequestParam List<Long> cartItemIds, Principal principal, Model model) {
        String userid = principal.getName();

        // 선택된 장바구니 항목 조회
        List<CartItem> cartItems = cartService.findCartItemsByIds(cartItemIds);

        // 주문 정보 계산
        int totalAmount = cartItems.stream()
            .mapToInt(cartItem -> cartItem.getItem().getPrice() * cartItem.getCount())
            .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);

        return "orderTotal"; // 주문 페이지로 이동
    }
}
