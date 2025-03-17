package com.shop.repository;

import com.shop.constant.OrderStatus;
import com.shop.constant.ItemMenu;
import com.shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("select o from Order o where o.member.userid = :userid order by o.orderDate desc")
    List<Order> findOrders(@Param("userid") String userid, Pageable pageable);

    @Query("select count(o) from Order o where o.member.userid = :userid")
    Long countOrder(@Param("userid") String userid);


    Page<Order> findAllByOrderByIdDesc(Pageable pageable);

    Page<Order> findByIdOrderByIdDesc(Long orderId, Pageable pageable);

    Page<Order> findByMember_UseridContainingOrderByIdDesc(String userid, Pageable pageable);

    Page<Order> findByMember_NameContainingOrderByIdDesc(String name, Pageable pageable);

    Page<Order> findByOrderStatusOrderByIdDesc(OrderStatus orderStatus, Pageable pageable);

    Page<Order> findByOrderDateOrderByIdDesc(LocalDateTime orderDate , Pageable pageable);

    @Query("SELECT o FROM Order o WHERE FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d %H:%i') LIKE CONCAT('%', :query, '%') ORDER BY o.orderDate DESC")
    Page<Order> findByOrderDateContainingOrderByIdDesc(@Param("query") String query, Pageable pageable);

    //월별 주문 총액 조회
    @Query("SELECT COALESCE(SUM(o.paidAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Long findTotalAmountByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    //일별 카테고리별 매출 조회
    @Query("SELECT i.itemMenu, SUM(oi.orderPrice * oi.count) " +
           "FROM OrderItem oi " +
           "JOIN oi.item i " +
           "JOIN oi.order o " +
           "WHERE FUNCTION('DATE', o.orderDate) = FUNCTION('DATE', :orderDate) " +
           "AND o.orderStatus <> com.shop.constant.OrderStatus.CANCEL " +
           "GROUP BY i.itemMenu")
    List<Object[]> findDailySalesByCategory(@Param("orderDate") LocalDateTime orderDate);

    // 특정 날짜의 모든 주문 아이템 조회 (디버깅용)
    @Query("SELECT i.itemMenu, i.itemNm, oi.orderPrice, oi.count, o.orderDate " +
           "FROM OrderItem oi " +
           "JOIN oi.item i " +
           "JOIN oi.order o " +
           "WHERE FUNCTION('DATE', o.orderDate) = FUNCTION('DATE', :orderDate) " +
           "AND o.orderStatus <> com.shop.constant.OrderStatus.CANCEL")
    List<Object[]> findDailyOrderDetails(@Param("orderDate") LocalDateTime orderDate);

    // 상품 판매 순위 조회
    //주문 수에 따라 정렬, 같은 주문 수일 경우 상품명 순으로 정렬
    @Query("SELECT i.id, i.itemNm, COUNT(oi.item) as orderCount " +
           "FROM OrderItem oi " +
           "JOIN oi.item i " +
           "JOIN oi.order o " +
           "WHERE o.orderStatus <> com.shop.constant.OrderStatus.CANCEL " +
           "GROUP BY i.id, i.itemNm " +
           "ORDER BY orderCount DESC, i.itemNm ASC")
    List<Object[]> findTopSellingItems(Pageable pageable);

    // 일별 카테고리별 주문 내역 조회
    @Query("SELECT o.id, i.id, i.itemNm, i.price, oi.count, (i.price * oi.count), " +
           "o.member.name, o.member.tel, o.member.address, o.orderDate, img.imgUrl " +
           "FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.item i " +
           "LEFT JOIN ItemImg img ON img.item = i AND img.repImgYn = 'Y' " +
           "WHERE FUNCTION('DATE_FORMAT', o.orderDate, '%Y-%m-%d') = FUNCTION('DATE_FORMAT', :date, '%Y-%m-%d') " +
           "AND (:category IS NULL OR i.itemMenu = :category) " +
           "AND o.orderStatus <> 'CANCEL' " +
           "ORDER BY o.orderDate DESC")
    Page<Object[]> findDailyOrderDetails(@Param("date") LocalDateTime date, 
                                       @Param("category") ItemMenu category,
                                       Pageable pageable);

}
