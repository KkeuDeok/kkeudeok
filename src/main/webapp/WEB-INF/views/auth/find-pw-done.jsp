<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>비밀번호 변경 완료 | 끄덕</title>
    <%@ include file="../common/head.jsp" %>
</head>
<body class="auth-body">
<div class="app-frame">
    <%@ include file="../common/auth-left.jsp" %>
    <main class="auth-panel">
        <div class="auth-form-col">
            <div class="auth-brand auth-brand--solo">
                <span class="auth-avatar"><img src="/img/char-bear.png" alt=""></span>
                <span class="auth-logo">끄덕</span>
            </div>
            <div class="done-icon">
                <img src="/img/icon-check.svg" alt="">
            </div>
            <h1 class="done-title">비밀번호가 변경되었어요</h1>
            <p class="done-sub">새 비밀번호로 로그인해 주세요</p>

            <%-- 버튼 높이는 비찾 첫 화면 확인 버튼(645)과 동일 레일 — 팀장 확정 --%>
            <a href="/login" class="kd-btn kd-btn-primary auth-cta" style="margin-top: 124px;">로그인 하러가기</a>
        </div>
        <div class="auth-footer">개인정보 처리방침 · 이용약관 · 문의하기</div>
    </main>
</div>
</body>
</html>
