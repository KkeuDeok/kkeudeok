<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "보호자 PIN 설정 (곰 버전 시안)"; %>
<%@ include file="../common/auth-top.jsp" %>
<%@ include file="../common/auth-brand.jsp" %>
<h1 class="auth-title">보호자 PIN을<br>만들어주세요</h1>
<p class="auth-subtitle">아이 화면에서 보호자 화면으로 돌아올 때 사용해요</p>

<form method="post" action="/onboarding/pin">
    <div class="kd-field">
        <label class="kd-label" for="pin">보호자 PIN 4자리 <span class="req">*</span></label>
        <input class="kd-input onb-pin" type="password" id="pin" name="pin"
               inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
        <p class="kd-hint">숫자 4자리를 입력해 주세요</p>
    </div>
    <div class="kd-field">
        <label class="kd-label" for="pinCheck">PIN 확인 <span class="req">*</span></label>
        <input class="kd-input onb-pin" type="password" id="pinCheck" name="pinCheck"
               inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
        <p class="kd-hint">한 번 더 똑같이 입력해 주세요</p>
    </div>

    <button type="button" class="kd-btn kd-btn-primary auth-cta"
            onclick="kdSubmitOnbPin()">다음</button>
</form>

<%@ include file="../common/auth-bottom.jsp" %>
