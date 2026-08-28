<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% String pageTitle = "보호자 PIN 재설정"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>보호자 PIN을 다시 만들어요</h1>
    <p class="sub">가입할 때 쓴 이메일로 본인 확인을 해 주세요.<br>확인이 끝나면 새 PIN을 설정합니다.</p>

    <form onsubmit="return false">
        <div class="kd-field">
            <label class="kd-label" for="email">이메일 <span class="req">*</span></label>
            <div class="kd-input-row">
                <div class="kd-input-wrap">
                    <input class="kd-input mp-input-ro" type="email" id="email" name="email"
                           value="<c:out value='${myEmail}'/>" readonly>
                </div>
                <button type="button" class="kd-btn kd-btn-pill"
                        onclick="kdSendCode('pinReset')">인증번호 전송</button>
            </div>
            <p class="kd-hint">가입할 때 등록한 이메일이에요</p>
        </div>
        <div class="kd-field is-hidden" id="authCodeField">
            <label class="kd-label" for="authCode">인증번호</label>
            <div class="kd-input-row">
                <div class="kd-input-wrap">
                    <input class="kd-input" type="text" id="authCode" inputmode="numeric" maxlength="6"
                           autocomplete="off" placeholder="인증번호 6자리를 입력하세요">
                    <span class="kd-timer">03:00</span>
                </div>
                <button type="button" class="kd-btn kd-btn-pill kd-btn-resend"
                        onclick="kdSendCode('pinReset')">재전송</button>
            </div>
        </div>
    </form>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        document.getElementById('authCode').addEventListener('input', function () {
            if (this.value.trim().length === 6) kdSubmitPinReset();
        });
    });
</script>

<%@ include file="../common/app-bottom.jsp" %>
