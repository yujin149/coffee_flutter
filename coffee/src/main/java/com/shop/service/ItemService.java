package com.shop.service;

import com.shop.constant.ItemMenu;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.*;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.entity.Member;
import com.shop.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;
    private final ItemImgRepository itemImgRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService; // OrderService 추가
    private final MemberRepository memberRepository;

    /**
     * 상품 저장 메서드: 상품과 이미지 데이터를 저장
     */
    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        // 상품 등록
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        // 이미지 등록
        for (int i = 0; i < itemImgFileList.size(); i++) {
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);
            if (i == 0)
                itemImg.setRepImgYn("Y"); // 첫 번째 이미지는 대표 이미지
            else
                itemImg.setRepImgYn("N");
            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }
        return item.getId();
    }

    /**
     * 상품 상세 조회 메서드: 특정 상품의 상세 정보와 이미지를 반환
     */
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId) {
        // 상품 이미지 조회
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        for (ItemImg itemImg : itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
        }

        // 상품 엔티티 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // DTO 생성
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        return itemFormDto;
    }

    /**
     * 상품 수정 메서드: 상품 정보 및 이미지를 업데이트
     */
    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        // 상품 수정
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);

        // 2. 엔티티 필드 업데이트
        item.setItemNm(itemFormDto.getItemNm());
        item.setPrice(itemFormDto.getPrice());
        item.setStockNumber(itemFormDto.getStockNumber());
        item.setItemMenu(itemFormDto.getItemMenu()); // 상품 종류 업데이트
        item.setItemDetail(itemFormDto.getItemDetail());

        // 이미지 수정
        List<Long> itemImgIds = itemFormDto.getItemImgIds();
        for (int i = 0; i < itemImgFileList.size(); i++) {
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }
        return item.getId();
    }

    /**
     * 관리자 상품 리스트 조회 메서드
     */
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        // 검색 결과를 가져올 때 이미지도 함께 페치 조인
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    // 문의하기 검색 기능
    @Transactional(readOnly = true)
    public Page<Item> getItemsForSearch(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return itemRepository.findByItemSellStatus(ItemSellStatus.SELL, pageable);
        }
        return itemRepository.findByItemNmContainingAndItemSellStatus(keyword, ItemSellStatus.SELL, pageable);
    }

    /**
     * 메인 페이지 상품 리스트 조회 메서드
     */
    @Transactional
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getCoffeeItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getCoffeeItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getBeanItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getBeanItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getDesertItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getDesertItemPage(itemSearchDto, pageable);
    }

    /**
     * 상품 삭제 메서드: 상품 및 관련 데이터를 삭제
     */
    public void deleteItem(Long itemId) {
        // 상품 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. ID: " + itemId));

        // 연관 데이터 삭제
        orderItemRepository.deleteByItemId(itemId); // 주문 아이템 삭제
        cartItemRepository.deleteByItemId(itemId); // 장바구니 아이템 삭제

        // 이미지 삭제
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        for (ItemImg itemImg : itemImgList) {
            itemImgService.deleteItemImg(itemImg.getImgName());
        }
        itemImgRepository.deleteByItemId(itemId);

        // 상품 삭제
        itemRepository.delete(item);
    }

    /**
     * 상품 ID로 상품 조회 메서드 (리뷰 등에서 사용)
     */
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid itemId: " + itemId));
    }

    /**
     * 단일 상품 결제 및 주문 저장 메서드
     */
    public Long purchaseItem(Long itemId, int count, String email, String impUid, String merchantUid, int paidAmount) {
        // 상품 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 회원 조회
//        Member member = memberRepository.findByEmail(email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을수 없습니다."));

        // 재고 체크 및 감소
        if (item.getStockNumber() < count) {
            throw new IllegalStateException("상품 재고가 부족합니다.");
        }
        item.removeStock(count);

        // 주문 DTO 생성
        OrderDto orderDto = new OrderDto();
        orderDto.setItemId(itemId);
        orderDto.setCount(count);
        orderDto.setImpUid(impUid);
        orderDto.setMerchantUid(merchantUid);
        orderDto.setPaidAmount(paidAmount);

        // 주문 생성 (OrderService 호출)
        return orderService.order(orderDto, email);
    }

    // 문의하기 페이지 상품 조회 - 판매중인 상품만 조회
    public List<Item> getItems() {
        return itemRepository.findByItemSellStatus(ItemSellStatus.SELL);
    }


    public Item getItem(Long itemId) {
        System.out.println("getItem 1 :" + itemId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    System.out.println("Item not found for ID: " + itemId);
                    return new IllegalArgumentException("상품 ID가 유효하지 않습니다.");
                });

    }

    // 클릭 수 증가 메서드
    @Transactional
    public void incrementClickCount(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. ID: " + itemId));
        item.increaseClickCount(); // 클릭 수 증가
        itemRepository.save(item); // 변경 사항 저장
    }

    /* 144줄에서 조회하는 메서드 */



    // 클릭 상위 상품 가져오기
    @Transactional(readOnly = true)
    public List<Item> getTopClickedItems(int limit) {
        Pageable pageable = PageRequest.of(0, limit); // 0번 페이지에서 limit만큼 데이터를 가져옵니다.
        Page<Item> page = itemRepository.findTopByOrderByClickCountDesc(pageable); // Pageable 전달

        List<Item> items = page.getContent(); // 페이징 결과에서 실제 데이터를 추출합니다.

        // 대표 이미지 URL 설정 (없을 경우 기본 이미지 설정)
        for (Item item : items) {
            item.getRepresentativeImageUrl(); // 대표 이미지 URL 설정
        }

        // 디버깅 로그 추가
        System.out.println("Service에서 조회된 추천 상품 개수: " + items.size());
        for (Item item : items) {
            System.out.println("상품 ID: " + item.getId());
            System.out.println("상품명: " + item.getItemNm());
            System.out.println("클릭 수: " + item.getClickCount());
        }

        return items;
    }


    public boolean existsById(Long productId) {
        return itemRepository.existsById(productId);
    }

}
