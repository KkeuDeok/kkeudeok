<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "로그인"; %>
<%@ include file="../common/auth-top.jsp" %>

<%@ include file="../common/auth-brand.jsp" %>
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

    <%-- 데모 검증 — 백엔드 연동 시 type="submit"으로 복원 --%>
    <button type="button" class="kd-btn kd-btn-primary auth-cta"
            onclick="kdSubmitLogin()">로그인</button>
</form>

<div class="auth-links">
    <a href="/find-id">아이디 찾기</a><span>·</span><a href="/find-pw">비밀번호 찾기</a><span>·</span><a href="/signup/terms">회원가입</a>
</div>

<%@ include file="../common/auth-bottom.jsp" %>
