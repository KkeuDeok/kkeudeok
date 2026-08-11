<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "비밀번호 재설정"; %>
<%@ include file="../common/auth-top.jsp" %>

<%@ include file="../common/auth-brand.jsp" %>

<h1 class="auth-title">비밀번호 재설정</h1>

<form method="post" action="/find-pw/new">
    <%-- data-nocopy: 복사·잘라내기·붙여넣기 차단 (auth-validate.js) --%>
    <div class="kd-field">
        <label class="kd-label" for="newPassword">새 비밀번호</label>
        <input class="kd-input" type="password" id="newPassword" name="newPassword"
               placeholder="새 비밀번호를 입력하세요" autocomplete="new-password" data-nocopy>
    </div>
    <div class="kd-field">
        <label class="kd-label" for="newPasswordCheck">새 비밀번호 확인</label>
        <input class="kd-input" type="password" id="newPasswordCheck" name="newPasswordCheck"
               placeholder="새 비밀번호를 다시 입력하세요" autocomplete="new-password" data-nocopy>
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

<%@ include file="../common/auth-bottom.jsp" %>
