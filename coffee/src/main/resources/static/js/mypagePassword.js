function loadPasswordForm(url){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    if(!token || !header) {
        console.error("csrf 토큰 1");
        console.log("csrf 토큰 2");
        return;
    }
    console.log("1차 확인");
    $.ajax({
        url : url,
        type : "GET",
        beforeSend: function(xhr){
            xhr.setRequestHeader(header, token);
        },
        success : function(data) {

            if (typeof data === 'string') {
                alert(data);
                return;
            }

            console.log('응답:', data);
            let html = `
                <h3 class="contTitle">비밀번호 변경</h3>
                <form id="passwordForm" action="/mypage/password" method="post">

                <div class="form-group">
                    <label for="userid">ID</label>
                    <input type="text" name="userid" value="${data.userid}" class="form-control" readonly>
                </div>
                <div class="form-group">
                    <label for="email">이메일 주소</label>
                    <div class="list">
                        <input type="email" id="email" name="email" value="${data.email}" class="form-control" placeholder="이메일을 입력해주세요.">
                        <button type="button" id="sendEmailButton">이메일 인증 보내기</button>
                    </div>
                    <p class="fieldError" id="emailError"></p>
                </div>
                <div class="form-group">
                    <label for="emailCk">인증 코드</label>
                    <div class="list">
                        <input type="text" id="emailCk" name="emailCk" value="" class="form-control" placeholder="이메일을 입력해주세요.">
                        <button type="button" id="checkEmailCodeButton">인증 확인</button>
                    </div>
                    <p class="fieldError" id="emailCkError"></p>
                </div>
                <div class="form-group">
                    <label for="password">비밀번호</label>
                    <input type="password"  name="password" value="" class="form-control" placeholder="이메일을 입력해주세요.">
                    <p class="fieldError" id="passwordError"></p>
                </div>
                <div class="form-group">
                     <label for="passwordCk">비밀번호 확인</label>
                     <input type="password" name="passwordCk" value="" class="form-control" placeholder="이메일을 입력해주세요.">
                     <p class="fieldError" id="passwordCkError"></p>
                </div>
                <div class="btnWrap">
                    <button type="submit"  id="passwordBtn" class="saveBtn">비밀번호 변경</button>
                </form>
            `;
            $('#right-content').html('');
            $('#right-content').html(html);
            var csrfHeader = $("meta[name='_csrf_header']").attr("content");
            var csrfToken = $("meta[name='_csrf']").attr("content");

            $('#sendEmailButton').on('click',function(){
                var email = $('#email').val();
                sendEmail(email, csrfHeader , csrfToken);
            });

            $('#checkEmailCodeButton').on('click', function() {
                var emailCode = $('#emailCk').val();
                var email = $('#email').val();
                checkEmailCode(email, emailCode, csrfHeader,csrfToken);
            });


            $('#passwordBtn').on('click',function(){
                event.preventDefault();
                submitPassword();
            });

            function submitPassword(){
                var token = $("meta[name='_csrf']").attr("content");
                var header = $("meta[name='_csrf_header']").attr("content");
                var formData = $("#passwordForm").serialize();

                $.ajax({
                    url : "/mypage/password",
                    type : "POST",
                    data : formData,
                    beforeSend : function(xhr){
                        xhr.setRequestHeader(header,token);
                    },
                    success : function(response){
                        console.log("비밀번호 수정 성공:",response);
                        alert(response);
                        location.reload();
                    },
                    error : function(xhr){
                        try{
                            var errorMessage = JSON.parse(xhr.responseText);
                            alert(errorResponse.error);
                        } catch (e){
                            console.error("에러 상태 코드:", xhr.status);
                            console.error("에러 메시지:", xhr.responseText);
                            alert(xhr.responseText);
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
    })
}