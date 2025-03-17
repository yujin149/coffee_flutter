//  CartScript-->

$(document).ready(function(){
    $("input[name=cartChkBox]").change(function(){
        getOrderTotalPrice();
    });
});

function getOrderTotalPrice(){
    var orderTotalPrice = 0;

    $("input[name=cartChkBox]:checked").each(function () {
        var cartItemId = $(this).val();
        console.log("체크된 카트 아이템 ID: ", cartItemId); // 체크된 카트 아이템 로그

        var price = $("#price_" + cartItemId).attr("data-price");
        console.log("상품 가격: ", price); // 상품 가격 로그

        var count = $("#count_" + cartItemId).val();
        console.log("상품 수량: ", count); // 상품 수량 로그

        orderTotalPrice += price * count;
    });

    console.log("전체 주문 금액: ", orderTotalPrice); // 전체 주문 금액 로그
    $("#orderTotalPrice").html(formatPrice(orderTotalPrice) + '원');
}

function changeCount(obj){
    var count = obj.value;
    console.log("변경된 수량 : " ,count);

    var cartItemId = obj.id.split('_')[1];
    console.log("카트 아이템 ID: ", cartItemId); // 카트 아이템 ID 로그

    var price = $("#price_"+cartItemId).data("price");
    console.log("단가: ", price); // 상품 단가 로그

    var totalPrice = count * price;
    console.log("계산된 총 금액: ", totalPrice); // 계산된 총 금액 로그

    $("#totalPrice_" + cartItemId).html(totalPrice+"원");
    // 총 주문 금액 계산
    getOrderTotalPrice();
    // 서버로 업데이트 요청
    updateCartItemCount(cartItemId, count);
}
function checkAll(){
    if($("#checkall").prop("checked")){
        $("input[name=cartChkBox]").prop("checked",true);
    }
    else{
        $("input[name=cartChkBox]").prop("checked",false);
    }
    getOrderTotalPrice();
}

function updateCartItemCount(cartItemId, count){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/cartItem/" + cartItemId + "?count=" + count;

    console.log("서버로 보낼 URL: ", url);

    $.ajax({
        url : url,
        type : "PATCH",
        beforeSend : function(xhr){
            xhr.setRequestHeader(header,token);
        },
        dataType : "json",
        cache : false,
        success : function(result, status){
            console.log("서버 응답 성공: ", result);
        },
        error : function(jqXHR, status, error){
            console.error("서버 응답 실패: ", jqXHR.responseText);
            if(jqXHR.status == '401'){
                alert('로그인 후 이용해주세요');
                location.href='/members/login';
            }else{
                alert(jqXHR.responseText);
            }
        }
    });
}
function deleteCartItem(obj){
    var cartItemId = obj.dataset.id;
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/cartItem/" + cartItemId;

    /*$.ajax({
        url : url,
        type : "DELETE",
        beforeSend : function(xhr){
            xhr.setRequestHeader(header,token);
        },
        dataType : "json",
        cache : false,
        success : function(result,status){
            console.log("!!!!!!!!!!!!!!!!!!!!!!!!")

            location.href='/cart'; // 다시 본인을 부름 이유 삭제로 인한 화면 변경이 필요하기 때문
        },
        error : function(jqXHR,status,error){
            if(jqXHR.status == '401'){
                alert('로그인 후 이용해주세요');
                location.href='/members/login';
            }else{
                alert(jqXHR.responseText);
            }
        }
    });*/

    $.ajax({
        url: url, type: "DELETE", beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        }, dataType: "json", cache: false, success: function (result, status) {
            console.log("장바구니 항목 삭제 성공");
            // URL에서 현재 경로와 파라미터 확인
            var currentPath = window.location.pathname;
            var searchParams = new URLSearchParams(window.location.search);

            // 마이페이지의 장바구니 섹션인 경우
            if (currentPath === '/mypage' && searchParams.get('section') === 'cart') {
                loadCartPage(); // 장바구니 내용만 다시 로드
            } else {
                location.href = '/cart'; // 일반 장바구니 페이지인 경우 리다이렉트
            }
        }, error: function (jqXHR, status, error) {
            if (jqXHR.status == '401') {
                alert('로그인 후 이용해주세요');
                location.href = '/members/login';
            } else {
                alert(jqXHR.responseText);
            }
        }
    });
}

function submitOrder() {
    const selectedProducts = [];
    $("input[name='cartChkBox']:checked").each(function () {
        const cartItemId = $(this).val();
        const count = $(`#count_${cartItemId}`).val();

        // 유효성 검사
        if (!cartItemId || !count) {
            alert("상품 ID와 수량을 확인해주세요.");
            return;
        }

        selectedProducts.push({
            cartItemId: parseInt(cartItemId, 10),
            count: parseInt(count, 10)
        });
    });

    if (selectedProducts.length === 0) {
        alert("주문할 상품을 선택해주세요.");
        return;
    }

    // AJAX 요청
    const url = "/cart/orders";
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: "/cart/orders", // 주문 요청 URL
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            cartOrderDtoList: selectedProducts, // 선택된 상품 리스트
            impUid: rsp.imp_uid,                // 아임포트 결제 고유 ID
            merchantUid: rsp.merchant_uid,      // 주문 번호
            paidAmount: rsp.paid_amount,        // 결제된 금액
            buyerName: $('#buyerName').val(),   // 구매자 이름
            buyerEmail: $('#buyerEmail').val(), // 구매자 이메일
            buyerTel: $('#buyerTel').val(),     // 구매자 전화번호
        }),
        success: function (response) {
            alert("주문이 성공적으로 처리되었습니다.");
            location.href = "/orders"; // 주문 내역 페이지로 이동
        },
        error: function (jqXHR) {
            alert("주문 처리 중 오류가 발생했습니다.");
            console.error(jqXHR.responseText);
        }
    });
}

// 숫자 포맷팅 함수 추가
function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}