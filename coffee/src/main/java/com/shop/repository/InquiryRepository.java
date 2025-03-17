package com.shop.repository;

import com.shop.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByOrderByIdDesc();

    Page<Inquiry> findAllByOrderByIdDesc(Pageable pageable);

    Page<Inquiry> findByTitleContainingOrderByIdDesc(String title, Pageable pageable);

    Page<Inquiry> findByContentContainingOrderByIdDesc(String content, Pageable pageable);

    Page<Inquiry> findByWriterContainingOrderByIdDesc(String writer, Pageable pageable);

    Page<Inquiry> findByItem_ItemNmContainingOrderByIdDesc(String itemNm, Pageable pageable);

    @Query("select i from Inquiry i join fetch i.item where i.id = :id")
    Optional<Inquiry> findByIdWithItem(@Param("id") Long id);
}
