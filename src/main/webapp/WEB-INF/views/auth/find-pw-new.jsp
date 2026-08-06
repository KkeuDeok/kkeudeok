<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>비밀번호 재설정 | 끄덕</title>
    <%@ include file="../common/head.jsp" %>
</head>
<body class="auth-body">
<div class="app-frame">
    <%@ include file="../common/auth-left.jsp" %>
    <main class="auth-panel">
        <div class="auth-form-col">
            <div class="auth-brand">
                <span class="auth-avatar"><img src="/img/char-bear.png?v=3" alt=""></span>
                <span class="auth-logo">끄덕</span>
            </div>

            <div class="progress-track progress-track--two">
                <span class="bar active"></span>
                <span class="bar active"></span>
            </div>

            <h1 class="auth-title">비밀번호 재설정</h1>

            <form method="post" action="/find-pw/new">
                <div class="kd-field">
                    <label class="kd-label" for="newPassword">새 비밀번호</label>
                    <input class="kd-input" type="password" id="newPassword" name="newPassword"
                           placeholder="새 비밀번호를 입력하세요" autocomplete="new-password">
                </div>
                <div class="kd-field">
                    <label class="kd-label" for="newPasswordCheck">새 비밀번호 확인</label>
                    <input class="kd-input" type="password" id="newPasswordCheck" name="newPasswordCheck"
                           placeholder="새 비밀번호를 다시 입력하세요" autocomplete="new-password">
                </div>

                <%-- 입력에 따라 auth-validate.js가 실시간으로 ok 표시 --%>
                <div class="pw-rules">
                    <div class="pw-rule" id="ruleLen"><span class="dot"></span>8자 이상</div>
                    <div class="pw-rule" id="ruleAlpha"><span class="dot"></span>영문 포함</div>
                    <div class="pw-rule" id="ruleNum"><span class="dot"></span>숫자 포함</div>
                </div>

                <%-- 데모 검증 — 백엔드 연동 시 type="submit"·비활성(muted) 로직 복원 --%>
                <button type="button" class="kd-btn kd-btn-primary auth-cta"
                        onclick="kdSubmitFindPwNew()">비밀번호 변경</button>
            </form>
        </div>
        <div class="auth-footer">개인정보 처리방침 · 이용약관 · 문의하기</div>
    </main>
</div>
</body>
</html>
