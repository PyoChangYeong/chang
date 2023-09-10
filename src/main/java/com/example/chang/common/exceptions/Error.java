package com.example.chang.common.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Error {

    // 400 BAD_REQUEST 잘못된 요청
    NOT_FOUND_ID("400", "아이디 오류입니다."),
    PASSWORD_WRONG("400","잘못된 패스워드 입니다"),
    NOT_FOUND_POST("400", "게시물을 찾을 수 없습니다."),
    NOT_FOUND_MEMBER("400", "로그인이 필요합니다."),
    NOT_FOUND_SEARCH("400", "검색 결과가 없습니다."),
    NO_AUTHORITY("400", "권한이 없습니다."),
    WRONG_TOKEN("400", "토큰 오류"),
    NOT_VALIDURL("400","요효하지 않는 URL 입니다."),
    WRONG_BUSINESS("400", "사업자 번호를 입력해주세요"),
    NOT_VALIDCONTENT("400","유효하지 않는 내용입니다."),
    WRONG_PASSWORD_CHECK( "400", "비밀번호 형식이 아닙니다"),
    VALIDATE_BUSINESS("400","사업자 번호가 일치하지 않습니다."),
    VALIDATE_EMAIL_ERROR( "400", "이메일 형식이 아닙니다."),
    VALIDATE_NICKNAME_ERROR( "400", "닉네임은 알파벳 대, 소문자, 숫자로 구성된 2-12자리여야 한다."),
    NOT_MY_CONTENT("400", "작성자만 수정/삭제가 가능합니다"),
    NOT_EXIST_COMMENT("400","댓글이 존재하지 않습니다"),
    NOT_EXIST_USER("400","사용자가 없습니다"),
    BAD_REQUEST("400", ""),
    MAX_INPUT_IMAGE("400","최대 저장가능한 이미지 개수를 초과 했습니다."),
    CANNOT_CREATE_REVIEW_WITHIN_ONE_WEEK("400","게시글 작성할 수 있는 시간이 아닙니다."),
    // S3
    FAIL_S3_SAVE("400", "S3파일 저장 중 예외 발생"),
    FAIL_S3_DELETE("400","S3파일 삭제 중 예외 발생"),
    DELETE_S3_FILE("400","s3에 저장되었던 파일 삭제"),
    FAIL_S3_UPLOAD("400","S3에 이미지 업로드중 예외발생" ),
    WRONG_IMAGE_FORMAT("400", "잘못된 포멧입니다."),
    WRONG_INPUT_CONTEN("400", "파일이 존재하지 않습니다."),

    //SSE
    NOT_EXIST_NOTIFICATION("404","존재하지 않는 알림입니다."),

    //CHAT
   CHATROOM_NOT_FOUND("404", "채팅방이 존재하지 않습니다."),

    // 409 CONFLICT 중복된 리소스
    DUPLICATED_EMAIL("409", "이미 존재하는 이메일입니다."),
    DUPLICATED_NICKNAME("409", "이미 존재하는 닉네임입니다."),
    DUPLICATED_BUSINESS("409","중복된 사업자 입니다."),
    DUPLICATED_CHATROOM("409","이미 존제하는 채팅방 입니다.");

    private final String status;
    private final String message;


}