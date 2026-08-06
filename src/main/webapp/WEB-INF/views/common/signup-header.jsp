<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 회원가입 공통 헤더 (브랜드 + 3단 진행바 + 단계 라벨)
     사용법: include 전에 스크립틀릿으로 int signupStep = 1|2|3; 선언 --%>
<div class="auth-brand">
    <span class="auth-avatar"><img src="/img/char-neutral.png" alt=""></span>
    <span class="auth-logo-text">끄덕</span>
</div>
<div class="progress-track">
    <span class="bar<%= signupStep >= 1 ? " active" : "" %>"></span>
    <span class="bar<%= signupStep >= 2 ? " active" : "" %>"></span>
    <span class="bar<%= signupStep >= 3 ? " active" : "" %>"></span>
</div>
<div class="progress-labels">
    <span class="<%= signupStep >= 1 ? "active" : "" %>">약관동의</span>
    <span class="<%= signupStep >= 2 ? "active" : "" %>">정보입력</span>
    <span class="<%= signupStep >= 3 ? "active" : "" %>">완료</span>
</div>
