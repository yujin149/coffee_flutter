package com.shop.repository;


import com.shop.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {
    InquiryAnswer findByInquiryId(Long inquiryId);
}