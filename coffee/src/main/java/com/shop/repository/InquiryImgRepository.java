package com.shop.repository;


import com.shop.entity.InquiryImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryImgRepository extends JpaRepository<InquiryImg, Long> {
    List<InquiryImg> findByInquiryIdOrderByIdAsc(Long inquiryId);
}
