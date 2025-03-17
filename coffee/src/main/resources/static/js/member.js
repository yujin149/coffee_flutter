function sendEmail(email, csrfHeader, csrfToken) {
    if (!email) {
        alert("이메일을 입력해주세요.");
        return;
    }
    console.log("이메일요청하러 member.js 들어옴"+email)
    console.log("member.js header: "+csrfHeader)
    console.log("member.js token :"+csrfToken)



    $.ajax({
        url: '/members/sendEmail',
        type: 'POST',
        data: {
            email: email
        },
        beforeSend: function(xhr) {
            console.log("AJAX 요청: CSRF Header 및 Token 설정");
            xhr.setRequestHeader(csrfHeader, csrfToken);

        },
        success: function() {
            alert("인증 메일이 전송되었습니다.");
        },
        error: function() {
            console.log("에러발생 member.js ")
            alert("메일 전송 중 오류가 발생했습니다.");
        }
    });
}

function checkEmailCode(email, emailCode, csrfHeader, csrfToken) {
    if (!emailCode) {
        alert("인증 코드를 입력해주세요.");
        return;
    }

    $.ajax({
        url: '/members/checkCode',
        type: 'POST',
        data: {
            email: email,
            emailCode: emailCode
        },
        beforeSend: function(xhr) {
            xhr.setRequestHeader(csrfHeader, csrfToken);
        },
        success: function(response) {
            if (response === 'success') {
                alert("인증되었습니다.");
            } else {
                alert("인증 코드가 올바르지 않습니다.");
            }
        },
        error: function() {
            alert("인증 코드 확인 중 오류가 발생했습니다.");
        }
    });
}

