package kopo.kkeudeok.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

// 비밀번호 해시(임의의 글자+숫자로 된 암호화 시키는) 도구
public class EncryptUtil {

    /** 문자열 String을 SHA-256 해시(소문자 16진수 64글자)로 수정
     * 객체 없이 쓸라고 Static 을 사용한다캄
     */
    public static String encHashSHA256(String str) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // 자바 내장된 해시 계산기를 꺼낸다 "SHA-256"은 해시 방식의 이름

        md.update(str.getBytes(StandardCharsets.UTF_8));
        /** 계산기에 값을 넣기 getBytes(UFT-8)은 글자를 숫자로 바꾸는 거
         * 해시는 글자가 아니라 숫자를 다루기 때문에 숫자로 먼저 수정한다캄
         */

        return HexFormat.of().formatHex(md.digest());
        /**
         * md.digest() : 계산 실행 결과를 숫자 덩어리소 받기
         * formatHex() : 그 숫자를 사람이 읽을 수 있는 16진수 글자로 수정
         */
    }
}
