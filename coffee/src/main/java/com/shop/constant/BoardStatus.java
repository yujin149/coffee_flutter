package com.shop.constant;

/**
 * 게시판의 종류를 정의하는 열거형(Enum) 클래스
 */
public enum BoardStatus {
    // 각 게시판 타입을 상수로 정의
    NOTICE("공지"),    // 공지사항 게시판
    GENERAL("일반");   // 일반 게시판

    // 각 게시판 타입에 대한 한글 설명을 저장하는 변수
    private final String description;

    // 생성자: 게시판 타입 생성 시 description을 설정
    BoardStatus(String description) {
        this.description = description;
    }

    // description 값을 가져오는 getter 메소드
    public String getDescription() {
        return description;
    }

    //아래처럼 사용할 수 있다.
    // enum 상수 NOTICE는 내부적으로 description이 "공지"로 설정되어 있음
    //BoardStatus type = BoardStatus.NOTICE;
    //System.out.println(type.getDescription()); // "공지" 출력

    // enum 상수 GENERAL은 내부적으로 description이 "일반"으로 설정되어 있음
    //type = BoardStatus.GENERAL;
    //System.out.println(type.getDescription()); // "일반" 출력


}