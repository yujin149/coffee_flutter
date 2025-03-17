// 초기화: 구매자 정보 로드
window.onload = function () {
const IMP = window.IMP;
    IMP.init("imp77418255"); // 가맹점 식별 코드
     loadBuyerInfo(); // 구매자 정보 로드
};

// 구매자 정보를 장바구니 DB에서 가져오는 함수
function loadBuyerInfo() {
    $.ajax({
        url: "/cart/buyer-info", // 구매자 정보를 가져오는 API
        type: "GET",
        success: function (data) {
            // 서버에서 가져온 구매자 정보를 입력 필드에 채우기
            $('#buyerName').val(data.name);
            $('#buyerEmail').val(data.email);
              $('#buyerTel').val(data.phone || ""); // 전화번호 설정, 없으면 빈 문자열
        },
        error: function (error) {
            console.error("구매자 정보를 불러오는 중 오류 발생:", error);
        }
    });
}

function handlePayment(pg, payMethod,selectedProducts,finalAmount) {
    if (!pg || !payMethod) {
        console.error("PG사 또는 결제 방식이 전달되지 않았습니다.");
        return;
    }



    // 선택된 상품이 없을 경우
    if (selectedProducts.length === 0) {
        alert("결제할 상품을 선택해주세요.");
        return;
    }
    // 재고확인
    $.ajax({
            url: "/cart/checkstock",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify( selectedProducts),
            success: function (response) {
                if (response.status === "OK") {
                    // 재고 확인 성공 시 결제 진행
                    console.log("체크 성공")
                    processPayment(pg, payMethod, finalAmount, selectedProducts);
                } else {
                    // 재고 부족 알림
                    alert("주문할 수 없는 상품이 있습니다: " + response.message);
                }
            },
            error: function () {
                alert("재고 확인 중 문제가 발생했습니다. 다시 시도해주세요.");
            }
        });




    function processPayment(pg, payMethod, finalAmount, selectedProducts) {

    console.log("선택된 상품들:", selectedProducts);
    console.log("총 결제 금액:", finalAmount);

    // 구매자 정보 가져오기
    const buyerName = $('#buyerName').val();
    const buyerEmail = $('#buyerEmail').val();
    const buyerTel = $('#buyerTel').val() || ""; // 값이 없으면 빈 문자열

    // 유효성 검사
    if (!buyerName || !buyerEmail) {
        alert("구매자 정보를 입력해주세요.");
        return;
    }

    // 아임포트 결제 요청
    const IMP = window.IMP; // 아임포트 초기화
    IMP.init("imp77418255"); // 가맹점 식별 코드

    IMP.request_pay({
        pg: pg,
        pay_method: payMethod,
        merchant_uid: "order_no_" + new Date().getTime(),
        name: "총 결제 금액: " + finalAmount + "원",
        amount: finalAmount,
        buyer_email: buyerEmail,
        buyer_name: buyerName,
        buyer_tel: buyerTel,
    }, function (rsp) {
        if (rsp.success) {
            alert("결제가 완료되었습니다.");
            console.log("결제 성공 데이터:", rsp);

            const token = $("meta[name='_csrf']").attr("content");
            const header = $("meta[name='_csrf_header']").attr("content");


            console.log("전송 데이터:", JSON.stringify({
                imp_uid: rsp.imp_uid,
                merchant_uid: rsp.merchant_uid,
                paidAmount: rsp.paid_amount,
                apply_num: rsp.apply_num,
                usedMembership: parseInt($("#useMembership").val(), 10),
                selectedProducts: selectedProducts,
            }));


            // 서버로 결제 정보 및 선택된 상품 정보 전송
            $.ajax({
                url: "/cart/orders",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({
                    imp_uid: rsp.imp_uid,
                    merchant_uid: rsp.merchant_uid,
                    paidAmount: rsp.paid_amount,
                    apply_num: rsp.apply_num,
                    usedMembership: parseInt($("#useMembership").val(), 10),
                    selectedProducts: selectedProducts,
                }),
                success: function (response) {
                    console.log("카트 결제 성공")
                    const memberShip = response.memberShip;
                    alert(`결제가 성공적으로 처리되었습니다.\n적립금: ${memberShip}원이 적립되었습니다.`);
                    location.href = "/orders"; // 결제 완료 후 이동
                },
                error: function (error) {
                    alert("결제 정보 저장 중 문제가 발생했습니다.");
                    console.error(error);
                }
            });
        } else {
            alert("결제가 실패했습니다: " + rsp.error_msg);
            console.error("결제 실패 데이터:", rsp);
        }
    });
}
}
// 상품 상세페이지
function handleDetailPayment(pg,payMethod,singleItemId,singleItemCount,finalAmount) {


    console.log("상세페이지 결제 되는지 확인");

    const itemId = singleItemId;
    const count = singleItemCount;



    console.log("itemId:", itemId,  "count:", count , "finalAmount:" , finalAmount);



    // 유효성 검사
    if (!itemId  || !count || finalAmount <= 0) {
        console.log("유효성 검사 에러?")
        alert("결제 정보가 올바르지 않습니다.");
        return;
    }
    console.log("유효성 검사 통과 했는지 확인");
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    const product = {
            itemId: parseInt(itemId, 10), // 수정: cartItemId → itemId
            count: parseInt(count, 10)
        };
    $.ajax({
                url: "/order/checkstock",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(product),
                beforeSend: function (xhr) {
                        xhr.setRequestHeader(header, token); // CSRF 토큰 추가
                },
                success: function (response) {
                    if (response.status === "OK") {
                        // 재고 확인 성공 시 결제 진행
                        processDetailPayment(pg, payMethod, itemId, count , finalAmount);
                    } else {
                        // 재고 부족 알림
                        alert("주문할 수 없는 상품이 있습니다: " + response.message);
                    }
                },
                error: function () {
                    alert("재고 확인 중 문제가 발생했습니다. 다시 시도해주세요.");
                }
            });




    function processDetailPayment(pg, payMethod, itemId, count , finalAmount) {

    // 구매자 정보 가져오기
    const buyerName = $('#buyerName').val();
    const buyerEmail = $('#buyerEmail').val();
    const buyerTel = $('#buyerTel').val() || "";

    console.log("구매자 정보 가져오고 확인");

    if (!buyerName || !buyerEmail) {
        alert("구매자 정보를 입력해주세요.");
        return;
    }

    // 아임포트 결제 요청
    const IMP = window.IMP; // 아임포트 초기화
    IMP.init("imp77418255"); // 가맹점 식별 코드

    IMP.request_pay({
        pg: pg,
        pay_method: payMethod,
        merchant_uid: "order_no_" + new Date().getTime(),
        name: "총 결제 금액: " + finalAmount + "원",
        amount: finalAmount,
        buyer_email: buyerEmail,
        buyer_name: buyerName,
        buyer_tel: buyerTel,
    }, function (rsp) {
        if (rsp.success) {
            alert("결제가 완료되었습니다.");

            console.log("ajax 들어가기전에 확인");
            console.log(rsp.success);

             // CSRF 토큰 가져오기
            const token = $("meta[name='_csrf']").attr("content");
            const header = $("meta[name='_csrf_header']").attr("content");
            $.ajax({
                url: "/order",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({
                    impUid: rsp.imp_uid,
                    merchantUid: rsp.merchant_uid,
                    paidAmount: rsp.paid_amount,
                    usedMembership: parseInt($("#useMembership").val(), 10),
                    itemId: itemId,
                    count: count,
                }),
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(header, token);
                },
                success: function (response) {
                    console.log("서버 응답 데이터:", response);
                    const membership = response.membership;
                    alert(`결제가 성공적으로 처리되었습니다.\n적립금: ${membership}원이 적립되었습니다.`);
                    location.href = "/mypage?section=orders"; // 마이페이지의 구매내역 섹션으로 이동
                },
                error: function (error) {
                    alert("결제 정보 저장 중 문제가 발생했습니다.");
                    console.error(error);
                }
            });
        } else {
            alert("결제가 실패했습니다: " + rsp.error_msg);
        }
    });
}
}



//통합결제
function processPayment(pg, payMethod) {

    const buyerName = $("#buyerName").val();
    const buyerEmail = $("#buyerEmail").val();
    const buyerTel = $("#buyerTel").val();


    if (!buyerName || !buyerEmail) {
        alert("구매자 정보를 입력해주세요.");
        return;
    }

    const cartItems = [];
    const singleItemId = $("#itemId").val();
    const singleItemCount = $("#singleItemCount").text(); // 단일 상품 수량
    //const finalAmount = parseInt($("#finalAmount").text(), 10); // 최종 결제 금액
    const finalAmount = parseInt($("#finalAmount").text().replace(/,/g, ''), 10); // 콤마 제거 후 파싱

    console.log("1차 디버깅")
    console.log("singleItemId:",singleItemId);
    console.log("singleItemCount:" , singleItemCount);

    if (singleItemId && singleItemCount && finalAmount > 0) {
        // **단일 상품 결제 처리**
        console.log("단일 상품 결제 처리 시작");
        handleDetailPayment(pg,payMethod,singleItemId,singleItemCount,finalAmount);

    }
    const selectedProducts = [];
    let totalAmount = 0;

        $("div[data-cart-item-id]").each(function () {
            const cartItemId = $(this).data("cart-item-id");
            const count = $(this).data("count");
            const price = $(this).data("price");

            console.log("2차 디버깅")
            console.log("cartItemId: ", cartItemId);
            console.log("count: ", count);
            console.log("price: ", price);

            if (cartItemId && count && price) {
                selectedProducts.push({
                    cartItemId: parseInt(cartItemId, 10),
                    count: parseInt(count, 10),
                    price: parseInt(price, 10),
                });

                totalAmount += count * price;
                console.log("totalAmount: ", totalAmount);
            }
        });

    console.log("장바구니 상품 수:", selectedProducts.length);
    console.log("장바구니 원래 총 금액:", totalAmount);
    console.log("장바구니 적립금 차감 총 금액:", finalAmount);

    // 장바구니 결제 처리
    if (selectedProducts.length > 0) {
           console.log("장바구니 결제 조건 충족");
           handlePayment(pg, payMethod, selectedProducts, finalAmount);
           return;
    }

    // 결제 조건에 맞지 않는 경우

    console.error("결제 조건이 충족되지 않음: 단일 상품과 장바구니 모두 조건 불충족");

//     else if (cartItems.length > 0) {
//        // **장바구니 결제 처리**
//        const selectedProducts = [];
//        let totalAmount = 0;
//
//        $("div[data-cart-item-id]").each(function () {
//                const cartItemId = $(this).data("cart-item-id");
//                const count = $(this).data("count");
//                const price = $(this).data("price");
//
//                if (cartItemId && count && price) {
//                    selectedProducts.push({
//                        cartItemId: parseInt(cartItemId, 10),
//                        count: parseInt(count, 10),
//                        price: parseInt(price, 10),
//                    });
//
//                    totalAmount += count * price;
//                }
//        });
//        handlePayment(pg, payMethod,selectedProducts,finalAmount);
//
//
//    } else {
//        alert("결제할 상품 정보를 확인해주세요.");
//    }
}