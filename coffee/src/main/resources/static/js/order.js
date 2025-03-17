// orderHist

//function cancelOrder(orderId){
//    var token = $("meta[name='_csrf']").attr("content");
//    var header = $("meta[name='_csrf_header']").attr("content");
//
//    console.log("orderId : " , orderId)
//
//    var url = "/order/" + orderId +"/cancel";
//    var paramData = {
//      orderId : orderId
//    }
//    var param = JSON.stringify(paramData);
//
//    $.ajax({
//      url : url,
//      type : "POST",
//      contentType : "application/json",
//      data : param,
//      beforeSend : function(xhr){
//
//      xhr.setRequestHeader(header,token);
//    },
//    dataType : "json",
//    cache : false,
//    success : function(result,status){
//       console.log("주문 취소");
//      alert("주문이 취소 되었습니다.");
//      location.reload();
//    },
//    error : function(jqXHR, status, error){
//        if(jqXHR.status == '401'){
//            alert('로그인 후 이용해주세요');
//            location.href='/members/login';
//        }else{
//            alert(jqXHR.responseText);
//        }
//    }
//    });
//}


  function updateFinalAmount() {
        console.log("updateFinalAmount 함수 호출됨");
        const totalAmount = parseFloat(document.getElementById("totalAmount").innerText);
        const membership = parseFloat(document.getElementById("membership").innerText);
        const useMembershipInput = document.getElementById("useMembership");
        let useMembership = parseFloat(useMembershipInput.value);

    if (isNaN(useMembership) || useMembership < 0) {
        useMembership = 0;
        useMembershipInput.value = '';
    }

    const maxUsableMembership = Math.min(totalAmount * 0.1, membership);
    if (useMembership > maxUsableMembership) {
        useMembership = maxUsableMembership;
        useMembershipInput.value = maxUsableMembership.toFixed(0);
    }

    const finalAmount = totalAmount - useMembership;
    document.getElementById("finalAmount").innerText = finalAmount.toFixed(0);
    }

    function checkEnter(event) {
        if (event.key === "Enter") {
            updateFinalAmount();
        }
    }
function requestCancelOrder(orderId) {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: `/order/${orderId}/cancel-request`,
        type: "POST",
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {
            alert(response);
            location.reload();
        },
        error: function(jqXHR) {
            console.log(jqXHR.responseText)
            alert(jqXHR.responseText);
        }
    });
}
// 0115 환불 구현
function approveCancellation(orderId) {
    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: `/admin/order/${orderId}/approve-cancellation`,
        type: "POST",
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(response) {

            alert(response);
            location.reload();
        },
        error: function(jqXHR) {
        console.error("오류 발생:", jqXHR.responseText); // 서버 응답 메시지 출력
           alert(`환불 처리 중 오류 발생: ${jqXHR.responseText}`);
        }
    });
}


//$(document).ready(function () {
//    console.log("order.js is loaded (2)");
//
//    // 폼 제출 이벤트 처리
//    $('#updateForm').on('submit', function (event) {
//        event.preventDefault(); // 기본 동작 차단
//        console.log("Form submission intercepted");
//
//        const name = $('#name').val();
//        const tel = $('#tel').val();
//        const postcode = $('#sample6_postcode').val();
//        const address = $('#sample6_address').val();
//
//        console.log("정보:", { name, tel, postcode, address });
//
//        if (!name || !tel || !postcode || !address) {
//            alert("모든 필드를 입력해주세요.");
//            return;
//        }
//
//        const csrfToken = $("meta[name='_csrf']").attr("content");
//        const csrfHeader = $("meta[name='_csrf_header']").attr("content");
//
//        // AJAX 요청
//        $.ajax({
//            url: '/order/updateBuyerInfo',
//            type: 'POST',
//            data: {
//                name: name,
//                tel: tel,
//                postcode: postcode,
//                address: address
//            },
//            beforeSend: function (xhr) {
//                xhr.setRequestHeader(csrfHeader, csrfToken);
//            },
//            success: function (response) {
//                console.log("AJAX 성공:", response);
//                alert("회원 정보가 성공적으로 수정되었습니다.");
//            },
//            error: function (xhr, status, error) {
//                console.error('AJAX 요청 오류:', error);
//                alert("회원 정보 수정 중 오류가 발생했습니다.");
//            }
//        });
//    });
//});


//월별 주문 총액 조회
document.addEventListener('DOMContentLoaded', function() {
     console.log('Initializing all components...');
     
     // Chart.js 로드 확인
     if (typeof Chart === 'undefined') {
         console.error('Chart.js is not loaded!');
         return;
     }
     
     // 이전 차트 인스턴스 제거
     if (dailyPaymentChart) {
         dailyPaymentChart.destroy();
         dailyPaymentChart = null;
     }
     
     // 각 컴포넌트 초기화
     initializeMonthlyStats();
     initializeDailyVisits();
     initializeMonthlyVisits();
     initializeDailyPayment();
     initializeTopItems();
     
     console.log('All components initialized');
     
     // 엑셀 다운로드 버튼 이벤트
     const downloadBtn = document.querySelector('.downBtn');
     console.log('Download button found:', downloadBtn);
     
     if (downloadBtn) {
         downloadBtn.addEventListener('click', function(e) {
             console.log('Download button clicked');
             e.preventDefault();
             const category = getCurrentCategory();
             const date = getCurrentDate();
             const url = `/admin/orders/stats/daily-details/excel?year=${date.year}&month=${date.month}&day=${date.day}&category=${category}`;
             console.log('Downloading excel with URL:', url);
             
             fetch(url, {
                 headers: {
                     'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                 }
             })
             .then(response => {
                 console.log('Response status:', response.status);
                 return response.blob();
             })
             .then(blob => {
                 const url = window.URL.createObjectURL(blob);
                 const a = document.createElement('a');
                 a.href = url;
                 a.download = `주문내역_${date.year}${date.month}${date.day}.xlsx`;
                 document.body.appendChild(a);
                 a.click();
                 window.URL.revokeObjectURL(url);
             })
             .catch(error => {
                 console.error('Error:', error);
                 alert('엑셀 다운로드 중 오류가 발생했습니다.');
             });
         });
     }
});

function initializeMonthlyStats() {
    // 월별 주문 금액 초기화
    const monthBox = document.querySelector('.monthBox');
    if (!monthBox) {
        console.error('Monthly stats box not found');
        return;
    }
    const prevBtn = monthBox.querySelector('.prev.arrow');
    const nextBtn = monthBox.querySelector('.next.arrow');
    
    // 초기 데이터 로드
    loadMonthlyStats();
    
    // 이벤트 리스너 등록
    prevBtn.addEventListener('click', function(e) {
        e.preventDefault();
        updateMonth(-1);
    });
    
    nextBtn.addEventListener('click', function(e) {
        e.preventDefault();
        updateMonth(1);
    });
}

function updateMonth(change) {
    const yearSpan = document.querySelector('.monthBox .year');
    const monthSpan = document.querySelector('.monthBox .month');
    
    let year = parseInt(yearSpan.textContent);
    let month = parseInt(monthSpan.textContent);
    
    month += change;
    
    if (month > 12) {
        month = 1;
        year++;
    } else if (month < 1) {
        month = 12;
        year--;
    }
    
    loadMonthlyStats(year, month);
}

function loadMonthlyStats(year, month) {
    if (!year || !month) {
        const yearSpan = document.querySelector('.monthBox .year');
        const monthSpan = document.querySelector('.monthBox .month');
        year = parseInt(yearSpan.textContent);
        month = parseInt(monthSpan.textContent);
    }
    
    fetch(`/admin/orders/stats/monthly?year=${year}&month=${month}`)
        .then(response => response.json())
        .then(data => {
            updateMonthlyStatsUI(data);
        })
        .catch(error => console.error('Error:', error));
}

function updateMonthlyStatsUI(data) {
    const yearSpan = document.querySelector('.monthBox .year');
    const monthSpan = document.querySelector('.monthBox .month');
    const priceElement = document.querySelector('.monthBox .price');
    
    yearSpan.textContent = data.year;
    monthSpan.textContent = data.month;
    priceElement.innerHTML = formatPrice(data.total) + '<span class="won">원</span>';
}

function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

//일별 방문자 수 조회
function initializeDailyVisits() {
    // 일별 방문자 수 초기화
    const todayBox = document.querySelector('.todayBox');
    if (!todayBox) return;
    
    const prevBtn = todayBox.querySelector('.prev.arrow');
    const nextBtn = todayBox.querySelector('.next.arrow');
    
    // 초기 데이터 로드
    loadDailyVisits();
    
    // 이벤트 리스너 등록
    prevBtn.addEventListener('click', function(e) {
        e.preventDefault();
        updateDay(-1);
    });
    
    nextBtn.addEventListener('click', function(e) {
        e.preventDefault();
        updateDay(1);
    });
}

function updateDay(change) {
    const yearSpan = document.querySelector('.todayBox .year');
    const monthSpan = document.querySelector('.todayBox .month');
    const dateSpan = document.querySelector('.todayBox .date');
    
    let date = new Date(
        parseInt(yearSpan.textContent),
        parseInt(monthSpan.textContent) - 1,
        parseInt(dateSpan.textContent)
    );
    
    date.setDate(date.getDate() + change);
    
    loadDailyVisits(date.getFullYear(), date.getMonth() + 1, date.getDate());
}

function loadDailyVisits(year, month, day) {
    if (!year || !month || !day) {
        const yearSpan = document.querySelector('.todayBox .year');
        const monthSpan = document.querySelector('.todayBox .month');
        const dateSpan = document.querySelector('.todayBox .date');
        
        year = parseInt(yearSpan.textContent);
        month = parseInt(monthSpan.textContent);
        day = parseInt(dateSpan.textContent);
    }
    
    fetch(`/admin/visits/daily?year=${year}&month=${month}&day=${day}`)
        .then(response => response.json())
        .then(data => {
            updateDailyVisitsUI(data);
        })
        .catch(error => console.error('Error:', error));
}

function updateDailyVisitsUI(data) {
    const yearSpan = document.querySelector('.todayBox .year');
    const monthSpan = document.querySelector('.todayBox .month');
    const dateSpan = document.querySelector('.todayBox .date');
    const numElement = document.querySelector('.todayBox .num');
    
    yearSpan.textContent = data.year;
    monthSpan.textContent = data.month;
    dateSpan.textContent = data.date;
    numElement.innerHTML = formatNumber(data.count) + '<span class="won">명</span>';
}

function formatNumber(number) {
    return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

//월별 방문자 수 조회
function initializeMonthlyVisits() {
    // 월별 방문자 수 초기화
    const monthlyVisitBox = document.querySelector('.totalNum li:last-child');
    if (!monthlyVisitBox) return;
    
    const prevBtn = monthlyVisitBox.querySelector('.prev.arrow');
    const nextBtn = monthlyVisitBox.querySelector('.next.arrow');
    
    // 초기 데이터 로드
    loadMonthlyVisits();
    
    // 이벤트 리스너 등록
    prevBtn.addEventListener('click', function(e) {
        e.preventDefault();
        updateVisitMonth(-1);
    });
    
    nextBtn.addEventListener('click', function(e) {
        e.preventDefault();
        updateVisitMonth(1);
    });
}

function updateVisitMonth(change) {
    const monthlyVisitBox = document.querySelector('.totalNum li:last-child');
    const yearSpan = monthlyVisitBox.querySelector('.year');
    const monthSpan = monthlyVisitBox.querySelector('.month');
    
    let year = parseInt(yearSpan.textContent);
    let month = parseInt(monthSpan.textContent);
    
    month += change;
    
    if (month > 12) {
        month = 1;
        year++;
    } else if (month < 1) {
        month = 12;
        year--;
    }

    // 일별 방문자 수의 날짜와 비교
    const dailyBox = document.querySelector('.todayBox');
    const dailyYear = parseInt(dailyBox.querySelector('.year').textContent);
    const dailyMonth = parseInt(dailyBox.querySelector('.month').textContent);

    // 연도와 월이 일치할 때만 방문자 수 계산
    if (year === dailyYear && month === dailyMonth) {
        calculateMonthlyTotal(year, month);
    } else {
        const numElement = monthlyVisitBox.querySelector('.num');
        numElement.innerHTML = '0<span class="won">명</span>';
        yearSpan.textContent = year;
        monthSpan.textContent = month;
    }
}

function calculateMonthlyTotal(year, month) {
    const monthlyVisitBox = document.querySelector('.totalNum li:last-child');
    const yearSpan = monthlyVisitBox.querySelector('.year');
    const monthSpan = monthlyVisitBox.querySelector('.month');
    const numElement = monthlyVisitBox.querySelector('.num');
    
    // 해당 월의 마지막 날짜 구하기
    const lastDay = new Date(year, month, 0).getDate();
    let totalVisits = 0;
    let completedRequests = 0;
    
    // 각 일자별 방문자 수 합산
    for(let day = 1; day <= lastDay; day++) {
        fetch(`/admin/visits/daily?year=${year}&month=${month}&day=${day}`)
            .then(response => response.json())
            .then(data => {
                totalVisits += data.count;
                completedRequests++;
                
                // 모든 요청이 완료되면 UI 업데이트
                if(completedRequests === lastDay) {
                    yearSpan.textContent = year;
                    monthSpan.textContent = month;
                    numElement.innerHTML = formatNumber(totalVisits) + '<span class="won">명</span>';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                completedRequests++;
            });
    }
}

function loadMonthlyVisits() {
    const monthlyVisitBox = document.querySelector('.totalNum li:last-child');
    const yearSpan = monthlyVisitBox.querySelector('.year');
    const monthSpan = monthlyVisitBox.querySelector('.month');
    
    const year = parseInt(yearSpan.textContent);
    const month = parseInt(monthSpan.textContent);
    
    calculateMonthlyTotal(year, month);
}

// 일별 결제금액 차트 초기화
function initializeDailyPayment() {
    // 일별 결제금액 차트 초기화
    const todayPayWrap = document.querySelector('.todayPayWrap');
    if (!todayPayWrap) {
        console.error('Daily payment wrap not found');
        return;
    }
    initializeChart();
    const prevBtn = todayPayWrap.querySelector('.prev.arrow');
    const nextBtn = todayPayWrap.querySelector('.next.arrow');
    
    // 초기 데이터 로드
    loadDailyPaymentChart();
    
    // 이벤트 리스너 등록
    prevBtn.addEventListener('click', function(e) {
        e.preventDefault();
        updatePaymentDay(-1);
    });
    
    nextBtn.addEventListener('click', function(e) {
        e.preventDefault();
        updatePaymentDay(1);
    });
}

function updatePaymentDay(change) {
    const yearSpan = document.querySelector('.todayPayWrap .year');
    const monthSpan = document.querySelector('.todayPayWrap .month');
    const dateSpan = document.querySelector('.todayPayWrap .date');
    
    let date = new Date(
        parseInt(yearSpan.textContent),
        parseInt(monthSpan.textContent) - 1,
        parseInt(dateSpan.textContent)
    );
    
    date.setDate(date.getDate() + change);
    
    loadDailyPaymentChart(date.getFullYear(), date.getMonth() + 1, date.getDate());
}

let dailyPaymentChart = null;

function loadDailyPaymentChart(year, month, day) {
    console.log('Loading daily payment chart...', {year, month, day});
    if (!year || !month || !day) {
        const yearSpan = document.querySelector('.todayPayWrap .year');
        const monthSpan = document.querySelector('.todayPayWrap .month');
        const dateSpan = document.querySelector('.todayPayWrap .date');
        
        year = parseInt(yearSpan.textContent);
        month = parseInt(monthSpan.textContent);
        day = parseInt(dateSpan.textContent);
    }
    
    fetch(`/admin/orders/stats/daily?year=${year}&month=${month}&day=${day}`)
        .then(response => response.json())
        .then(data => {
            console.log('Daily payment data received:', data);
            updateDailyPaymentUI(data);
            updateDailyPaymentChart(data.sales);
        })
        .catch(error => console.error('Error loading daily payment:', error));
}

function updateDailyPaymentUI(data) {
    const yearSpan = document.querySelector('.todayPayWrap .year');
    const monthSpan = document.querySelector('.todayPayWrap .month');
    const dateSpan = document.querySelector('.todayPayWrap .date');
    
    yearSpan.textContent = data.year;
    monthSpan.textContent = data.month;
    dateSpan.textContent = data.date;
}

function updateDailyPaymentChart(sales) {
    const ctx = document.getElementById('dailyPaymentChart');
    if (!ctx) {
        console.error('Chart canvas not found');
        return;
    }
    
    // sales 데이터가 없을 경우 기본값 설정
    const chartData = {
        TOTAL: sales?.TOTAL || 0,
        COFFEE: sales?.COFFEE || 0,
        BEAN: sales?.BEAN || 0,
        DESERT: sales?.DESERT || 0
    };
    
    if (dailyPaymentChart) {
        dailyPaymentChart.destroy();
    }
    
    console.log('Updating chart with data:', chartData);
    
    dailyPaymentChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['전체', '커피', '원두', '디저트'],
            datasets: [{
                data: [
                    chartData.TOTAL,
                    chartData.COFFEE,
                    chartData.BEAN,
                    chartData.DESERT
                ],
                backgroundColor: [
                    '#ee3424',
                    '#ffe6e4',
                    '#ffe6e4',
                    '#ffe6e4'
                ],
                borderColor: [
                    '#ee3424',
                    '#ffe6e4',
                    '#ffe6e4',
                    '#ffe6e4'
                ],
                borderWidth: 1,
                borderRadius: 10,
                barThickness: 130
            }]
        },
        options: {
            maintainAspectRatio: false,
            responsive: true,
            height: 400,
            onHover: (event, elements) => {
                event.native.target.style.cursor = elements.length ? 'pointer' : 'default';
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        display: false
                    },
                    ticks: {
                        callback: function(value) {
                            return value.toLocaleString() + '원';
                        },
                        font: {
                            size: 14,
                            family: "'Pretendard', sans-serif",
                            color:"#888"
                        }
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            size: 14,
                            family: "'Pretendard', sans-serif",
                            weight: '500',
                            color:"#202020"
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 10,
                    titleFont: {
                        size: 14,
                        family: "'Pretendard', sans-serif"
                    },
                    bodyFont: {
                        size: 13,
                        family: "'Pretendard', sans-serif"
                    }
                }
            },
            onClick: function(event, elements) {
                if (elements.length > 0) {
                    const index = elements[0].index;
                    const category = ['TOTAL', 'COFFEE', 'BEAN', 'DESERT'][index];
                    const label = ['전체', '커피', '원두', '디저트'][index];
                    loadDailyOrderDetails(category, label, 1);
                }
            }
        }
    });
    console.log('Chart updated successfully');
}

function loadDailyOrderDetails(category, label, page) {
    const yearSpan = document.querySelector('.todayPayWrap .year');
    const monthSpan = document.querySelector('.todayPayWrap .month');
    const dateSpan = document.querySelector('.todayPayWrap .date');
    
    const year = parseInt(yearSpan.textContent);
    const month = parseInt(monthSpan.textContent);
    const day = parseInt(dateSpan.textContent);
    
    fetch(`/admin/orders/stats/daily-details?year=${year}&month=${month}&day=${day}&category=${category}&page=${page}`)
        .then(response => response.json())
        .then(data => {
            updateOrderDetailsUI(data, category, label);
        })
        .catch(error => console.error('Error:', error));
}

function updateOrderDetailsUI(data, category, label) {
    const titleSpan = document.querySelector('.statsDetail .tit span');
    titleSpan.textContent = label;
    
    const tbody = document.querySelector('.statsDetail table tbody');
    tbody.innerHTML = '';
    
    data.orders.forEach((order, index) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${(data.currentPage - 1) * 5 + index + 1}</td>
            <td>${order.itemId}</td>
            <td><div class="imgWrap"><img src="${order.imgUrl}" alt="${order.itemNm}"></div></td>
            <td>
                <div class="prodInfo">
                    <p class="title">${order.itemNm}</p>
                    <p class="price">${order.price.toLocaleString()}원</p>
                </div>
            </td>
            <td>${order.count}</td>
            <td>${order.totalPrice.toLocaleString()}원</td>
            <td>${order.orderName}</td>
            <td>${order.phone}</td>
            <td>${order.address}</td>
            <td>${formatDateTime(order.orderDate)}</td>
        `;
        tbody.appendChild(tr);
    });
    
    updatePagination(data.totalPages, data.currentPage, category, label);
}

function updatePagination(totalPages, currentPage, category, label) {
    const paginationElement = document.querySelector('.statsDetail .pagination');
    if (!paginationElement) return;
    
    let html = '';
    
    // 맨 처음 페이지
    if (currentPage > 1) {
        html += `
            <li class="page-item page-arrow page-prev01">
                <a class="page-link" href="#" data-page="1">맨 처음 페이지</a>
            </li>
            <li class="page-item page-arrow page-prev02">
                <a class="page-link" href="#" data-page="${currentPage - 1}">이전 페이지</a>
            </li>
        `;
    } else {
        html += `
            <li class="page-item page-arrow page-prev01 disabled">
                <a class="page-link" href="#">맨 처음 페이지</a>
            </li>
            <li class="page-item page-arrow page-prev02 disabled">
                <a class="page-link" href="#">이전 페이지</a>
            </li>
        `;
    }
    
    // 페이지 번호 (시작과 끝 계산)
    const start = Math.floor((currentPage - 1) / 10) * 10 + 1;
    const end = Math.min(start + 9, totalPages);
    
    // 페이지 번호 생성
    for (let i = start; i <= end; i++) {
        html += `<li class="page-item page-num ${i === currentPage ? 'active' : ''}">`;
        if (i === currentPage) {
            html += `<a class="page-link" href="#" data-page="${i}">${i}</a>`;
        } else {
            html += `<a class="page-link" href="#" data-page="${i}">${i}</a>`;
        }
        html += '</li>';
    }
    
    // 다음 페이지와 마지막 페이지
    if (currentPage < totalPages) {
        html += `
            <li class="page-item page-arrow page-next02">
                <a class="page-link" href="#" data-page="${currentPage + 1}">다음 페이지</a>
            </li>
            <li class="page-item page-arrow page-next01">
                <a class="page-link" href="#" data-page="${totalPages}">맨 뒤 페이지</a>
            </li>
        `;
    } else {
        html += `
            <li class="page-item page-arrow page-next02 disabled">
                <a class="page-link" href="#">다음 페이지</a>
            </li>
            <li class="page-item page-arrow page-next01 disabled">
                <a class="page-link" href="#">맨 뒤 페이지</a>
            </li>
        `;
    }
    
    paginationElement.innerHTML = html;
    
    // 페이지 클릭 이벤트 추가
    paginationElement.querySelectorAll('.page-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            if (this.parentElement.classList.contains('disabled')) return;
            const page = parseInt(this.dataset.page);
            loadDailyOrderDetails(category, label, page);
        });
    });
}

function formatDateTime(dateTime) {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    }).replace(/\./g, '-').replace(',', '');
}

function getCurrentCategory() {
    const titleSpan = document.querySelector('.statsDetail .tit span');
    const label = titleSpan.textContent;
    const categories = {
        '전체': 'TOTAL',
        '커피': 'COFFEE',
        '원두': 'BEAN',
        '디저트': 'DESERT'
    };
    return categories[label] || 'TOTAL';
}

function getCurrentDate() {
    const yearSpan = document.querySelector('.todayPayWrap .year');
    const monthSpan = document.querySelector('.todayPayWrap .month');
    const dateSpan = document.querySelector('.todayPayWrap .date');
    
    return {
        year: parseInt(yearSpan.textContent),
        month: parseInt(monthSpan.textContent),
        day: parseInt(dateSpan.textContent)
    };
}

// 상품 판매 순위 조회
function initializeTopItems() {
    // 상품 판매 순위 초기화
    console.log('Initializing top items...');
    loadTopItems();
    
    // 5초마다 데이터 갱신
    setInterval(loadTopItems, 5000);
}

function loadTopItems() {
    console.log('Loading top items...');
    fetch('/admin/orders/stats/top-items')
        .then(response => response.json())
        .then(data => {
            console.log('Top items data:', data);
            updateTopItemsUI(data);
        })
        .catch(error => {
            console.error('Error loading top items:', error);
        });
}

function updateTopItemsUI(items) {
    const listElement = document.querySelector('.rightBox .list');
    if (!listElement) {
        console.error('Top items list not found');
        return;
    }
    listElement.innerHTML = '';
    
    if (!items || items.length === 0) {
        listElement.innerHTML = '<li><p class="title">판매 데이터가 없습니다.</p></li>';
        return;
    }
    
    items.forEach((item, index) => {
        const li = document.createElement('li');
        li.innerHTML = `
            <a href="/item/${item.itemId}" target="_blank">
                <span class="num">${index + 1}.</span>
                <p class="title">${item.itemNm}</p>
            </a>
        `;
        listElement.appendChild(li);
    });
}

// 차트 초기화 확인
function initializeChart() {
    const ctx = document.getElementById('dailyPaymentChart');
    if (!ctx) {
        console.error('Chart canvas not found');
        return;
    }
    if (typeof Chart === 'undefined') {
        console.error('Chart.js not loaded');
        return;
    }
    console.log('Chart initialization successful');
}