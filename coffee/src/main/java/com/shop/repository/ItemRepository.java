package com.shop.repository;

import com.shop.constant.ItemMenu;
import com.shop.constant.ItemSellStatus;
import com.shop.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface
ItemRepository extends JpaRepository<Item,Long>, QuerydslPredicateExecutor<Item>,ItemRepositoryCustom {

    boolean existsByItemNmAndImage(String itemNm, String image);

    // select * from item where itemNm = ?(String itemNm)
    List<Item> findByItemNm(String itemNm);

    List<Item> findByItemNmOrItemDetail(String itemNm,String itemDetail);

    List<Item> findByPriceLessThan(Integer price);

    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);

    // select * from Item where itemDetail like %itemDetail% order by price desc;
    @Query("select i from Item i where i.itemDetail like %:itemDetail% order by i.price desc")
    List<Item> findByItemDetail(@Param("itemDetail")String itemDetail);

    @Query(value = "select * from item i where i.item_Detail like %:itemDetail% order by i.price desc",nativeQuery = true)
    List<Item> findByItemDetailNative(@Param("itemDetail")String itemDetail);


    // 문의하기 - 판매중인 상품 조회
    List<Item> findByItemSellStatus(ItemSellStatus itemSellStatus);

    @Query("SELECT i FROM Item i JOIN FETCH i.images WHERE i.clickCount > 0 ORDER BY i.clickCount DESC")
    Page<Item> findTopByOrderByClickCountDesc(Pageable pageable);

    List<Item> findByIsCrawled(boolean isCrawled);


    Page<Item> findByItemMenu(ItemMenu itemMenu, Pageable pageable);


    // 문의하기 검색 기능
    Page<Item> findByItemNmContaining(String keyword, Pageable pageable);

    Page<Item> findByItemSellStatus(ItemSellStatus itemSellStatus, Pageable pageable);

    Page<Item> findByItemNmContainingAndItemSellStatus(String itemNm, ItemSellStatus itemSellStatus, Pageable pageable);
}
