//itemForm-
$(document).ready(function(){

    calculateTotalPrice();

    $("#count").change(function(){
        calculateTotalPrice();
    });
});
function calculateTotalPrice(){
    var count = $("#count").val();
    var price = $("#price").val();
    var totalPrice = price*count;
    console.log(totalPrice);
    $("#totalPrice").html(totalPrice + '원');
}

function orders(selectedProducts) {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    const url = "/cart/orders";

    // 선택된 상품 ID 배열을 서버에 맞게 변환
    const paramData = {
        cartOrderDtoList: selectedProducts.map(productId => ({ cartItemId: productId }))
    };

    const param = JSON.stringify(paramData);

    console.log("전송 데이터:", param); // 디버깅용 로그

    $.ajax({
        url: url,
        type: "POST",
        contentType: "application/json",
        data: param,
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        dataType: "json",
        cache: false,
        success: function (result, status) {
            alert("주문이 완료되었습니다.");
            location.href = "/orders"; // 구매 이력 페이지로 이동
        },
        error: function (jqXHR, status, error) {
            if (jqXHR.status === 401) {
                alert('로그인 후 이용해 주세요');
                location.href = '/members/login';
            } else {
                alert("오류가 발생했습니다: " + jqXHR.responseText);
                console.error("오류 디버깅:", jqXHR.responseText);
            }
        }
    });
}

function addCart(){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/cart";

    var paramData = {
        itemId : $("#itemId").val(),
        count : $("#count").val()
    };


    var param = JSON.stringify(paramData);

    $.ajax({
        url : url,
        type : "POST",
        contentType : "application/json",
        data : param,
        beforeSend : function(xhr){
            /* 데이터 전송하기 전에 헤더이 csrf 값을 설정 */
            xhr.setRequestHeader(header,token);
        },
        dataType : "json",
        cache : false,
        success : function(result,status){
            alert("상품을 장바구니에 담았습니다.");
            location.href='/';
        },
        error : function(jqXHR,status,error){
            if(jqXHR.status == '401'){
                alert('로그인 후 이용해주세요');
                location.href='/members/login';
            }else{
                alert(jqXHR.responseText);
            }
        }
    });
}
// itemMng
$(document).ready(function(){
    $("#searchBtn").on("click",function(e){
        e.preventDefault(); // 검색버튼 클릭시 form 태그 전송을 막습니다.
        page(0);
    });
});
function page(page){
    var searchDateType = $("#searchDateType").val();
    var searchSellStatus = $("#searchSellStatus").val();
    var searchBy = $("#searchBy").val();
    var searchQuery = $("#searchQuery").val();

    location.href="/admin/items/" + page + "?searchDateType=" + searchDateType + "&searchSellStatus=" + searchSellStatus + "&searchBy=" + searchBy + "&searchQuery=" + searchQuery;
}

// 상품관리 (삭제)
function deleteItem(itemId) {
    console.log("deleteItem 호출됨, itemId:", itemId); // 디버깅 메시지 추가

    if (!itemId) {
        alert("유효하지 않은 상품 ID입니다.");
        return;
    }

    if (confirm("정말로 이 상품을 삭제하시겠습니까?")) {
        const token = document.querySelector('input[name="_csrf"]').value; // CSRF 토큰 가져오기
        console.log("CSRF 토큰:", token);

        fetch(`/admin/item/${itemId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': token // CSRF 토큰 추가
            }
        })
            .then(response => {
                console.log("서버 응답 상태 코드:", response.status);
                if (response.ok) {
                    alert("상품이 성공적으로 삭제되었습니다.");
                    window.location.href = "/admin/items"; // 상품 관리 페이지로 이동
                } else {
                    return response.text().then(text => {
                        console.error("삭제 실패 이유:", text);
                        alert("상품 삭제에 실패했습니다. 다시 시도해주세요.");
                    });
                }
            })
            .catch(error => {
                console.error("삭제 요청 중 오류 발생:", error);
                alert("오류가 발생했습니다. 관리자에게 문의하세요.");
            });
    }
}



function orders(selectedProduct) {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    const url = "/cart/orders";

    const paramData = {
        cartOrderDtoList: [
            { cartItemId: selectedProduct }
        ]
    };

    const param = JSON.stringify(paramData);

    $.ajax({
        url: url,
        type: "POST",
        contentType: "application/json",
        data: param,
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        <!--        dataType: "json",-->
        <!--        cache: false,-->
        success: function (result, status) {
            alert("주문이 완료되었습니다.");
            location.href = "/orders"; // 구매 이력 페이지로 이동
        },
        error: function (jqXHR, status, error) {
            if (jqXHR.status == '401') {
                alert('로그인 후 이용해 주세요');
                location.href = '/members/login';
            } else {
                alert(jqXHR.responseText);
            }
        }
    });
}

function updateMainImage(selectedImage) {
    const mainImage = document.getElementById('mainImage');
    if (mainImage && selectedImage.src) {
        mainImage.src = selectedImage.src;
    }
}