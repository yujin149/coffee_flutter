package com.shop.repository;

import com.shop.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByItemId(Long itemId, Pageable pageable); // 특정 상품의 리뷰 가져오기
}
