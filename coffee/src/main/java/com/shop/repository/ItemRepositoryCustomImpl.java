package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemMenu;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import com.shop.entity.QItemImg;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {
    private JPAQueryFactory queryFactory; // 동적쿼리 사용하기 위해 JPAQueryFactory 변수 선언

    public ItemRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em); //JPAQueryFactory 실질적인 객체가 만들어 집니다.
    }

    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus){
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
        //itemSellStatus null이면 null 리턴 null 아니면 SELL, SOLD 둘중 하나 리턴
    }


    private BooleanExpression regDtsAfter(String searchDateType){ // all, 1d, 1w, 1m, 6m
        LocalDateTime dateTime = LocalDateTime.now(); //현재 시간을 추출해서 변수에 대입

        if (StringUtils.equals("all", searchDateType) || searchDateType == null){
            return null;
        }else if(StringUtils.equals("1d", searchDateType)){
            dateTime = dateTime.minusDays(1);
        }else if(StringUtils.equals("1w", searchDateType)){
            dateTime = dateTime.minusWeeks(1);
        }else if(StringUtils.equals("1m", searchDateType)){
            dateTime = dateTime.minusMonths(1);
        }else if(StringUtils.equals("6m", searchDateType)){
            dateTime = dateTime.minusMonths(6);
        }
        return QItem.item.regTime.after(dateTime);
        //dateTime을 시간에 맞게 세팅 후 시간에 맞는 등록된 상품이 조회하도록 조건값 반환
    }

    //searchBy에 따라 검색어에 포함되어있는 상품명 또는 작성자 조회
    private BooleanExpression searchByLike(String searchBy, String searchQuery){
        if (StringUtils.equals("itemNm", searchBy)){ //상품명
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        }else if(StringUtils.equals("createdBy", searchBy)){ //작성자
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }
        return null;
    }

    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        /*
        select * from item
        where
        1. null 조건제외
        2. null 조건제외
        3. null 조건제외
        4. 정렬을 item id 기준 내림차순
        offset 0 Limit 5
        실행
         */
        QueryResults<Item> results = queryFactory.selectFrom(QItem.item) //selectFrom(QItem.item) : 상품 데이터 조회
                //where 조건절 : BooleanExpression 반환하는 조건문들을 넣어줌. ','단위로 넣어줄 경우 and 조건으로 인식.
                .where(regDtsAfter(itemSearchDto.getSearchDateType()), searchSellStatusEq(itemSearchDto.getSearchSellStatus()),searchByLike(itemSearchDto.getSearchBy(),itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                //offset : 데이터를 가지고 올 인덱스 지정
                .offset(pageable.getOffset())
                // limit : 한번에 가지고 올 최대 갯수 지정
                .limit(pageable.getPageSize())
                //fetchResults() : 조회한 리스트 및 전체 개수를 포함하는 QueryResults반환.
                // 상품데이터 리스트 조회 및 상품 데이터 전체 개수 조회하는 2번의 쿼리문이 실행
                .fetchResults();
        List<Item> content = results.getResults(); // 결과값 -> List로 받는다.
        long total = results.getTotal(); // 결과가 나온 길이 / 5이하만 나오기
        return new PageImpl<>(content, pageable, total); // List, 페이지 세팅, 길이 PageImpl 객체로 반환
    }

    public BooleanExpression itemNmLike(String searchQuery){
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
    }

    private BooleanExpression itemMenuEq(ItemMenu itemMenu) {
        return itemMenu == null ? null : QItem.item.itemMenu.eq(itemMenu);
    }

    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;
        /*
        select i.id,id,itemNm,i.itemDetail,im.itemImg,i.price from item i, itemImg im join i.id = im.itemId
        where im.repImgYn = "Y" and i.itemNm like %searchQuery% order by i.id desc
        QMainItemDto @QueryProjection을 활용하면 DTO로 바로 조회가능
         */
        QueryResults<MainItemDto> results = queryFactory
                .select(new QMainItemDto(item.id,item.itemNm,item.itemDetail,itemImg.imgUrl,item.price))
                .from(itemImg)
                .join(itemImg.item, item)
                .where(
                        itemImg.repImgYn.eq("Y"),
                        itemNmLike(itemSearchDto.getSearchQuery())
                )
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable,total);
    }

    public Page<MainItemDto> getCoffeeItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                .select(new QMainItemDto(item.id,item.itemNm,item.itemDetail,itemImg.imgUrl,item.price))
                .from(itemImg)
                .join(itemImg.item, item)
                .where(
                        itemImg.repImgYn.eq("Y"),
                        itemNmLike(itemSearchDto.getSearchQuery()),
                        item.itemMenu.eq(ItemMenu.COFFEE)
                )
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable,total);
    }

    public Page<MainItemDto> getBeanItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                .select(new QMainItemDto(item.id,item.itemNm,item.itemDetail,itemImg.imgUrl,item.price))
                .from(itemImg)
                .join(itemImg.item, item)
                .where(
                        itemImg.repImgYn.eq("Y"),
                        itemNmLike(itemSearchDto.getSearchQuery()),
                        item.itemMenu.eq(ItemMenu.BEAN)
                )
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable,total);
    }

    public Page<MainItemDto> getDesertItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                .select(new QMainItemDto(item.id,item.itemNm,item.itemDetail,itemImg.imgUrl,item.price))
                .from(itemImg)
                .join(itemImg.item, item)
                .where(
                        itemImg.repImgYn.eq("Y"),
                        itemNmLike(itemSearchDto.getSearchQuery()),
                        item.itemMenu.eq(ItemMenu.DESERT)
                )
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable,total);
    }
}
