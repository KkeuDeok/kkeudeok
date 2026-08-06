<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>아이디 찾기 | 끄덕</title>
    <%@ include file="../common/head.jsp" %>
</head>
<body class="auth-body">
<div class="app-frame">
    <%@ include file="../common/auth-left.jsp" %>
    <main class="auth-panel">
        <div class="auth-form-col">
            <div class="auth-brand auth-brand--solo">
                <span class="auth-avatar"><img src="/img/char-bear.png?v=3" alt=""></span>
                <span class="auth-logo">끄덕</span>
            </div>
            <div class="done-icon">
                <img src="/img/icon-check.svg" alt="">
            </div>
            <h1 class="done-title" style="margin-bottom: 0;">아이디를 찾았어요</h1>

            <div class="done-id-card">pa****</div>

            <a href="/find-pw" class="kd-btn kd-btn-outline auth-cta" style="margin-top: 24px;">비밀번호 찾기</a>
            <a href="/login" class="kd-btn kd-btn-primary auth-cta" style="margin-top: 13px;">로그인하기</a>
        </div>
        <div class="auth-footer">개인정보 처리방침 · 이용약관 · 문의하기</div>
    </main>
</div>
</body>
</html>
