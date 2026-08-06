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
                <span class="auth-avatar"><img src="/img/char-bear.png?v=2" alt=""></span>
                <span class="auth-logo">끄덕</span>
            </div>

            <h1 class="auth-title auth-title--sub">아이디 찾기</h1>
            <p class="auth-subtitle">가입할 때 등록한 정보를 입력해 주세요</p>

            <form method="post" action="/find-id/verify">
                <div class="kd-field">
                    <label class="kd-label" for="userName">이름</label>
                    <input class="kd-input" type="text" id="userName" name="userName"
                           placeholder="이름을 입력하세요">
                </div>
                <div class="kd-field">
                    <label class="kd-label" for="email">이메일</label>
                    <div class="kd-input-row">
                        <div class="kd-input-wrap">
                            <input class="kd-input" type="email" id="email" name="email"
                                   placeholder="이메일을 입력하세요">
                        </div>
                        <button type="button" class="kd-btn kd-btn-pill is-disabled">인증번호 받기</button>
                    </div>
                </div>
                <div class="kd-field">
                    <label class="kd-label" for="authCode">인증번호</label>
                    <div class="kd-input-row">
                        <div class="kd-input-wrap">
                            <input class="kd-input" type="text" id="authCode" name="authCode"
                                   placeholder="인증번호 6자리를 입력하세요" maxlength="6"
                                   style="opacity: 0.5;" disabled>
                            <span class="kd-timer">02:47</span>
                        </div>
                        <button type="button" class="kd-btn kd-btn-pill">재전송</button>
                    </div>
                </div>

                <%-- 데모 내비게이션(재발송 → 인증 화면) — 백엔드 연동 시 type="submit"으로 복원 --%>
                <button type="button" class="kd-btn kd-btn-primary auth-cta"
                        onclick="location.href='/find-id/verify'">확인</button>
            </form>
        </div>
        <div class="auth-footer">개인정보 처리방침 · 이용약관 · 문의하기</div>
    </main>
</div>
</body>
</html>
