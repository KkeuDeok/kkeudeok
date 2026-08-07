<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 회원가입 공통 헤더 (브랜드 + 3단 진행바)
     사용법: include 전에 스크립틀릿으로 int signupStep = 1|2|3; 선언 --%>
<%@ include file="auth-brand.jsp" %>
<div class="progress-track">
    <span class="bar<%= signupStep >= 1 ? " active" : "" %>"></span>
    <span class="bar<%= signupStep >= 2 ? " active" : "" %>"></span>
    <span class="bar<%= signupStep >= 3 ? " active" : "" %>"></span>
</div>
