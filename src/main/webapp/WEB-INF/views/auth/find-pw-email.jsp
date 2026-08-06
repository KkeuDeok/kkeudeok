<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>비밀번호 찾기 | 끄덕</title>
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

            <div class="progress-track progress-track--two">
                <span class="bar active"></span>
                <span class="bar"></span>
            </div>

            <h1 class="auth-title auth-title--sub">비밀번호 찾기</h1>
            <p class="auth-subtitle">가입한 이메일을 입력해 주세요</p>

            <form method="post" action="/find-pw">
                <div class="kd-field">
                    <label class="kd-label" for="email">이메일</label>
                    <div class="kd-input-row">
                        <div class="kd-input-wrap">
                            <input class="kd-input" type="email" id="email" name="email"
                                   placeholder="이메일을 입력하세요">
                        </div>
                        <button type="button" class="kd-btn kd-btn-pill">인증메일 받기</button>
                    </div>
                </div>
                <div class="kd-field">
                    <label class="kd-label" for="authCode">인증코드</label>
                    <div class="kd-input-row">
                        <div class="kd-input-wrap">
                            <input class="kd-input" type="text" id="authCode" name="authCode"
                                   placeholder="인증번호를 입력하세요">
                            <span class="kd-timer">02:47</span>
                        </div>
                        <button type="button" class="kd-btn kd-btn-pill" style="color: var(--kd-primary-strong);">재전송</button>
                    </div>
                </div>

                <%-- 데모 내비게이션 — 백엔드 연동 시 type="submit"으로 복원 --%>
                <button type="button" class="kd-btn kd-btn-primary auth-cta"
                        onclick="location.href='/find-pw/new'">확인</button>
            </form>
        </div>
        <div class="auth-footer">개인정보 처리방침 · 이용약관 · 문의하기</div>
    </main>
</div>
</body>
</html>
