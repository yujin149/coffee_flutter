package com.shop.service;

import com.shop.dto.*;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String userid){
        /* Item 객체 DB에서 추출 */
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(() -> new EntityExistsException("Item not found with ID: " + cartItemDto.getItemId()));
        /* Member 객체 DB에서 추출 */
        Member member = memberRepository.findByUserid(userid);
        /* 카트 만들기 */
        /*
         null 일시 신규회원이라 새로만들어줌 <-> 현재 로그인 된 Member
         null이 아니면 회원 (이미 카트가 있음)
        */
        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }
        /* Cart Id 와 Item ID 를 넣어서 CartItem 객체 추출 */
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(),item.getId());
        /* 추출 된 CartItem 객체가 있으면 */
        if(savedCartItem != null){
            savedCartItem.addCount(cartItemDto.getCount()); // 있는 객체에 수량 증가
            return savedCartItem.getId();
        }
        // 추출된 CartItem 객체가 없으면
        else{
            // CartItem 객체를 생성하고 Save를 통해 DB에 저장
            CartItem cartItem = CartItem.createCartItem(cart,item,cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String userid){
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByUserid(userid);

        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            return cartDetailDtoList;
        }
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
        return cartDetailDtoList;
    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String userid){
        Member curMember = memberRepository.findByUserid(userid);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityExistsException("CartItem not found with ID: " + cartItemId));

        Member savedMember = cartItem.getCart().getMember();

        if(!StringUtils.equals(curMember.getEmail(),savedMember.getEmail())){
            return false;
        }
        return true;
    }
    public void updateCartItemCount(Long cartItemId,int count){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityExistsException("CartItem not found with ID: " + cartItemId));
        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityExistsException("CartItem not found with ID: " + cartItemId));
        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String userid,int usedMembership ,String  impUid,String merchantUid){
        //카트 적립금 만들기
//        Member member = memberRepository.findByUserid(userid);
//        int totalMembership = cartOrderDtoList.stream()
//                .mapToInt(cartOrderDto -> cartOrderDto.getCount()*getItemPrice(cartOrderDto.getCartItemId()))
//                .sum();
//
//
//        member.membershipUpdate(totalMembership);

        // 주문 DTO List 객체 생성
        List<OrderDto> orderDtoList = new ArrayList<>();
        // 카트 주문 List 에 있는 목록 -> 카트 아이템 객체로 추출
        // 주문 Dto 에 CartItem 정보를 담고
        // 위에 선언된 주문 Dto List 에 추가
        OrderDto orderDto;
        for(CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(() -> new EntityExistsException("CartItem not found with ID: " + cartOrderDto.getCartItemId()));
            orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }
        // 주문 DTO 리스트 현재 로그인 된 이메일 매개변수 넣고
        // 주문 서비스 실행 -> 주문
        Long orderId = orderService.orders(orderDtoList,userid ,usedMembership , impUid , merchantUid);

        // Cart 에서 있던 Item 주문이 되니까 CartItem 모두 삭제
        for(CartOrderDto cartOrderDto : cartOrderDtoList){
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(() -> new EntityExistsException("CartItem not found with ID: " + cartOrderDto.getCartItemId()));
            cartItemRepository.delete(cartItem);
        }
        return  orderId;
    }

    public int getItemPrice(Long cartItemId) {
//        Item item = itemRepository.findById(itemId)
//                .orElseThrow(() -> new EntityExistsException("Item not found with ID: " + itemId));
//        return item.getPrice(); // Item 엔티티에서 가격 정보를 가져옴

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found for ID: " + cartItemId));

        Item item = cartItem.getItem();
        System.out.println("CartItemId: " + cartItemId + ", ItemId: " + item.getId() + ", Price: " + item.getPrice());
        return item.getPrice();
    }

    // 장바구니 결제전 재고확인
    public Long getItemIdByCartItemId(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목 ID가 유효하지 않습니다."))
                .getItem().getId();
    }


    public List<CartItem> findCartItemsByIds(List<Long> cartItemIds) {
        return cartItemRepository.findAllById(cartItemIds);
    }
}
