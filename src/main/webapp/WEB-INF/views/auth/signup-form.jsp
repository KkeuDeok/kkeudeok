<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "회원가입 - 정보입력"; %>
<%@ include file="../common/auth-top.jsp" %>

<%@ include file="../common/signup-header.jsp" %>

<h1 class="auth-title">회원가입</h1>

<form method="post" action="/signup/form" class="auth-legacy">
    <div class="kd-field">
        <label class="kd-label" for="userName">이름 <span class="req">*</span></label>
        <input class="kd-input" type="text" id="userName" name="userName"
               placeholder="이름을 입력하세요">
    </div>
    <div class="kd-field kd-field--tight">
        <label class="kd-label" for="loginId">아이디 <span class="req">*</span></label>
        <input class="kd-input" type="text" id="loginId" name="loginId"
               placeholder="아이디를 입력하세요">
    </div>
    <div class="kd-field kd-field--tight">
        <label class="kd-label" for="password">비밀번호 <span class="req">*</span></label>
        <input class="kd-input" type="password" id="password" name="password"
               placeholder="비밀번호를 입력하세요" autocomplete="new-password">
        <p class="kd-hint">8자 이상, 영문과 숫자를 포함해 주세요</p>
    </div>
    <div class="kd-field kd-field--tight">
        <label class="kd-label" for="passwordCheck">비밀번호 확인 <span class="req">*</span></label>
        <input class="kd-input" type="password" id="passwordCheck" name="passwordCheck"
               placeholder="비밀번호를 다시 입력하세요" autocomplete="new-password">
    </div>
    <div class="kd-field kd-field--tight">
        <label class="kd-label" for="email">이메일 <span class="req">*</span></label>
        <div class="kd-input-row">
            <div class="kd-input-wrap">
                <input class="kd-input" type="email" id="email" name="email"
                       placeholder="이메일을 입력하세요">
            </div>
            <button type="button" class="kd-btn kd-btn-pill"
                    onclick="kdSendCode('signup')">인증번호 전송</button>
        </div>
    </div>
    <div class="kd-field kd-field--tight is-hidden" id="authCodeField">
        <label class="kd-label" for="authCode">인증번호 <span class="req">*</span></label>
        <div class="kd-input-row">
            <div class="kd-input-wrap">
                <input class="kd-input" type="text" id="authCode" name="authCode"
                       placeholder="인증번호 6자리를 입력하세요" maxlength="6">
                <span class="kd-timer">03:00</span>
            </div>
            <button type="button" class="kd-btn kd-btn-pill kd-btn-resend"
                    onclick="kdSendCode('signup')">재전송</button>
        </div>
    </div>

    <button type="button" class="kd-btn kd-btn-primary auth-cta"
            onclick="kdSubmitSignup()">확인</button>
</form>

<%@ include file="../common/auth-bottom.jsp" %>
