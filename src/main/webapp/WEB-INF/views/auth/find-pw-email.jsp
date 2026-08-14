<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% String pageTitle = "비밀번호 찾기"; %>
<%@ include file="../common/auth-top.jsp" %>

<%@ include file="../common/auth-brand.jsp" %>

<%-- 로그인한 사람이 마이페이지에서 왔으면 UserController 가 ${myEmail} 을 담아 준다.
     그때는 제목·안내도 '변경' 말투로 바꾼다 — 비밀번호를 잊은 게 아니라 바꾸러 온 것이다. --%>
<c:choose>
    <c:when test="${not empty myEmail}">
        <h1 class="auth-title auth-title--sub">비밀번호 변경</h1>
        <p class="auth-subtitle">가입한 이메일로 본인 확인을 해 주세요</p>
    </c:when>
    <c:otherwise>
        <h1 class="auth-title auth-title--sub">비밀번호 찾기</h1>
        <p class="auth-subtitle">가입한 이메일을 입력해 주세요</p>
    </c:otherwise>
</c:choose>

<form method="post" action="/find-pw">
    <div class="kd-field">
        <label class="kd-label" for="email">이메일</label>
        <div class="kd-input-row">
            <div class="kd-input-wrap">
                <%-- 로그인 상태면 계정 이메일을 채우고 잠근다(오타·다른 주소 입력 방지).
                     mp-input-ro 는 마이페이지 이메일 칸이 쓰는 '수정 불가' 회색 표시다. --%>
                <input class="kd-input ${not empty myEmail ? 'mp-input-ro' : ''}"
                       type="email" id="email" name="email"
                       value="<c:out value='${myEmail}'/>"
                       <c:if test="${not empty myEmail}">readonly</c:if>
                       placeholder="이메일을 입력하세요">
            </div>
            <button type="button" class="kd-btn kd-btn-pill"
                    onclick="kdSendCode('findPw')">인증번호 전송</button>
        </div>
    </div>
    <%-- 인증번호 칸은 [인증번호 전송]을 눌러야 나타난다 --%>
    <div class="kd-field is-hidden" id="authCodeField">
        <label class="kd-label" for="authCode">인증번호</label>
        <div class="kd-input-row">
            <div class="kd-input-wrap">
                <input class="kd-input" type="text" id="authCode" name="authCode"
                       placeholder="인증번호 6자리를 입력하세요" maxlength="6">
                <span class="kd-timer">03:00</span>
            </div>
            <button type="button" class="kd-btn kd-btn-pill kd-btn-resend"
                    onclick="kdSendCode('findPw')">재전송</button>
        </div>
    </div>

    <%-- 데모 검증 — 백엔드 연동 시 type="submit"으로 복원 --%>
    <button type="button" class="kd-btn kd-btn-primary auth-cta"
            onclick="kdSubmitFindPwEmail()">확인</button>
</form>

<%@ include file="../common/auth-bottom.jsp" %>
