<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "보호자 PIN 재설정"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>새 PIN을 정해 주세요</h1>
    <p class="sub">본인 확인이 끝났어요.<br>앞으로 쓸 PIN 4자리를 입력해 주세요.</p>

    <form onsubmit="kdSubmitNewPin(); return false;">
        <div class="kd-field">
            <label class="kd-label" for="newPin">새 PIN 4자리 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="newPin" name="newPin"
                       inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <p class="kd-hint" id="newPinHint">숫자 4자리를 입력해 주세요</p>
        </div>

        <div class="kd-field">
            <label class="kd-label" for="newPinCheck">새 PIN 확인 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="newPinCheck" name="newPinCheck"
                       inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <p class="kd-hint" id="resetHint">한 번 더 똑같이 입력해 주세요</p>
        </div>

        <div class="row">
            <a class="kd-btn kd-btn-outline" href="/mypage">취소</a>
            <button type="submit" class="kd-btn kd-btn-primary">변경</button>
        </div>
    </form>
</div>


<%@ include file="../common/app-bottom.jsp" %>
