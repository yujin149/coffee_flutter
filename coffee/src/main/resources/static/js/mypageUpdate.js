    function loadUpdateForm(url) {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        if (!token || !header) {
            console.error("CSRF 토큰이 누락되었습니다.");
            console.log("csrt 토큰 2누락")
            return;
        }

        $.ajax({
            url: url,
            type: 'GET',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function(data) {

                if (typeof data === 'string') {
                    alert(data);
                    return;
                }

                console.log('응답 데이터:', data);
                let html = `
                    <h3 class="contTitle">회원정보 수정</h3>
                    <form id="updateForm" action="/mypage/update" method="post">
                    <input type="hidden" name="userid" value="${data.userid}" />

                    <div class="form-group">
                        <label for="name">이름</label>
                        <input type="text" name="name" value="${data.name}" class="form-control" placeholder="이름을 입력해주세요.">
                        <p class="fieldError" id="nameError"></p>
                    </div>
                    <div class="form-group">
                        <label for="membership">적립금</label>
                        <input type="text" name="membership" value="${data.membership}" class="form-control" readonly>
                    </div>
                    <div class="form-group">
                        <label for="email">이메일 주소</label>
                        <input type="email" name="email" value="${data.email}" class="form-control" placeholder="이메일을 입력해주세요.">
                        <p class="fieldError" id="emailError"></p>
                    </div>
                    <div class="form-group">
                        <label for="tel">전화번호</label>
                        <input type="tel" name="tel" value="${data.tel}" class="form-control" placeholder="전화번호를 입력해주세요.">
                        <p class="fieldError" id="telError"></p>
                    </div>
                    <div class="form-group">
                        <label for="address">주소</label>
                        <div class="addressList address01">
                            <input type="text" id="sample6_postcode" name="postcode" placeholder="우편번호" value="${data.postcode}" readonly>
                            <input type="button" onclick="sample6_execDaumPostcode()" value="주소검색">
                        </div>
                        <div class="addressList address02">
                            <input type="text" id="sample6_address" name="address" placeholder="주소" value="${data.address}" readonly>
                        </div>
                        <div class="addressList address03">
                            <input type="text" id="sample6_detailAddress" placeholder="상세주소">
                            <input type="text" id="sample6_extraAddress" placeholder="추가주소">
                        </div>
                        <p class="error" id="addressError"></p>
                    </div>
                    <div class="form-group">
                        <label for="birthdate">생년월일</label>
                        <input type="text" name="birthdate" value="${data.birthdate}" class="form-control" placeholder="생년월일을 입력해주세요.">
                        <p class="fieldError" id="birthdateError"></p>
                    </div>
                    <div class="btnWrap">
                        <button type="submit"  id="updatebtn" class="updateBtn">수정</button>
                    </div>

                    </form>

                `;
                $('#right-content').html('');
                $('#right-content').html(html);

                $('#updatebtn').on('click',function(){
                  event.preventDefault();
                  submitUpdate();
                });

                function submitUpdate(){
                  var token = $("meta[name='_csrf']").attr("content");
                  var header = $("meta[name='_csrf_header']").attr("content");
                  var formData = $("#updateForm").serialize();

                  $.ajax({
                      url : "/mypage/update",
                      type : "POST",
                      data : formData,
                      beforeSend : function(xhr) {
                          xhr.setRequestHeader(header, token);
                      },
                      success : function(response) {

                          console.log("수정성공 :" ,response);
                          alert(response);
                          location.reload();
                      },
                      error : function(xhr) {
                          try {
                             var errorResponse = JSON.parse(xhr.responseText); // JSON 파싱
                             alert(errorResponse.error);
                          } catch (e) {
                             alert("에러 발생:\n" + xhr.responseText); // 단순 문자열 처리
                          }
                      }
                  });
                }
            },
            error: function(xhr) {
                try {
                    var errorResponse = JSON.parse(xhr.responseText); // JSON 파싱
                    alert(errorResponse.error); // 서버에서 반환한 오류 메시지 표시
                } catch (e) {
                    console.error("JSON 파싱 오류:", e);
                    alert("에러 발생:\n" + xhr.responseText); // 단순 문자열 처리
                }
            }
        });
    }
