//package com.shop.service;
//
//import com.shop.dto.PayOrderDto;
//import com.shop.repository.PayOderRepository;
//import com.siot.IamportRestClient.IamportClient;
//import com.siot.IamportRestClient.request.CancelData;
//import com.siot.IamportRestClient.response.IamportResponse;
//import com.siot.IamportRestClient.response.Payment;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
///**
// * 전반적 단계
// * 1. (F) 유저가 예약 정보를 기록하고 (B)데이터베이스에 예약 Table 에 기록한다.
// * 2. (F) 결제 단계에서 예약 Table 의 ID 를 merchant_uid 로 사용하여 결제를 진행한다.
// * 	- merchant_uid 매번 고유해야한다.
// * 	- 결제 바로 전 단계까지만 진행한 후 Reservation Table ID 를 바탕으로 결제 건 진행 (이어서 작성하기 등 가능)
// * 	- 혹은 날짜와 시간을 이용해 고유의 주문번호를 생성하고 예약 테이블에 기록할 수도 있다.
// * 3. (B) 결제 완료 후 서버에서 결제 정보를 한 번 더 확인하여 검증
// * 4. (B) 모든 검증 완료 시 결제 정보를 데이터베이스에 저장
// * 	4-1 (B) 검증 실패 시 결제 취소
// */
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class PaymentService {
//    private final IamportClient iamportClient;
//    private final PayOderRepository payOderRepository;
//
//    /**
//     * 아임포트 서버로부터 결제 정보를 검증
//     //     * @param imp_uid
//     */
//    public IamportResponse<Payment> validateIamport(String imp_uid) {
//        try {
//            IamportResponse<Payment> payment = iamportClient.paymentByImpUid(imp_uid);
//            log.info("결제 요청 응답. 결제 내역 - 주문 번호: {}", payment.getResponse());
//            return payment;
//        } catch (Exception e) {
//            log.info(e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * 아임포트 서버로부터 결제 취소 요청
//     *
//     //     * @param imp_uid
//     //     * @return
//     */
//    public IamportResponse<Payment> cancelPayment(String imp_uid) {
//        try {
//            CancelData cancelData = new CancelData(imp_uid, true);
//            IamportResponse<Payment> payment = iamportClient.cancelPaymentByImpUid(cancelData);
//            return payment;
//        } catch (Exception e) {
//            log.info(e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * 주문 정보 저장
//     //     * @param
//     //     * @return
//     */
//    public String saveOrder(PayOrderDto payOrderDto){
//        try {
//            payOderRepository.save(payOrderDto.toEntity());
//            return "주문 정보가 성공적으로 저장되었습니다.";
//        } catch (Exception e) {
//            log.info(e.getMessage());
//            cancelPayment(payOrderDto.getImpUid());
//            return "주문 정보 저장에 실패했습니다.";
//        }
//    }
//
//}