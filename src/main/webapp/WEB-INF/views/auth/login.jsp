<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>로그인 | 끄덕</title>
    <%@ include file="../common/head.jsp" %>
</head>
<body class="auth-body">
<div class="app-frame">
    <%@ include file="../common/auth-left.jsp" %>
    <main class="auth-panel">
        <div class="auth-form-col">
            <div class="auth-brand">
                <span class="auth-avatar"><img src="/img/char-bear.png" alt=""></span>
                <span class="auth-logo-text">끄덕</span>
            </div>
            <h1 class="auth-title">로그인</h1>

            <form method="post" action="/login">
                <div class="kd-field">
                    <label class="kd-label" for="loginId">아이디</label>
                    <input class="kd-input" type="text" id="loginId" name="loginId"
                           placeholder="아이디를 입력하세요" autocomplete="username">
                </div>
                <div class="kd-field">
                    <label class="kd-label" for="password">비밀번호</label>
                    <input class="kd-input" type="password" id="password" name="password"
                           placeholder="비밀번호를 입력하세요" autocomplete="current-password">
                </div>

                <div class="auth-remember">
                    <input class="kd-check" type="checkbox" id="rememberMe" name="rememberMe">
                    <label for="rememberMe">로그인 상태 유지</label>
                </div>

                <button type="submit" class="kd-btn kd-btn-primary auth-cta">로그인</button>
            </form>

            <div class="auth-links">
                <%-- 아이디/비밀번호 찾기는 새 창(팝업)으로 진행 --%>
                <a href="/find-id" onclick="window.open(this.href, 'kkeudeokFind', 'width=1000,height=760'); return false">아이디 찾기</a><span>·</span><a href="/find-pw" onclick="window.open(this.href, 'kkeudeokFind', 'width=1000,height=760'); return false">비밀번호 찾기</a><span>·</span><a href="/signup/terms">회원가입</a>
            </div>
        </div>
    </main>
</div>
</body>
</html>
