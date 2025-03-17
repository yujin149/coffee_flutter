package com.shop.constant;

public enum ExceptionCode {
    PAY_CANCEL("결제가 취소되었습니다."),
    PAY_FAILED("결제가 실패했습니다.");

    private final String message;

    ExceptionCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
