<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>보호자 확인이 필요해요</h1>
    <p class="sub">이 메뉴에는 아이의 감정 기록이 담겨 있어요.<br>보호자만 들어올 수 있습니다.</p>
    <form onsubmit="kdSubmitGate(); return false;">
        <div class="kd-field">
            <label class="kd-label" for="gatePin">보호자 PIN 4자리 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="gatePin" name="pin"
                       inputmode="numeric" maxlength="4" autocomplete="off" data-nocopy>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <p class="kd-hint">가입할 때 설정한 4자리 숫자예요</p>
        </div>
        <div class="row">
            <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
            <button type="submit" class="kd-btn kd-btn-primary">확인</button>
        </div>
        <a class="forgot" href="/mypage/pin-reset">PIN을 잊었어요</a>
    </form>
</div>

<script>

    document.addEventListener('DOMContentLoaded', function () {
        kdPinLock(Number('${pinLockLeft}') || 0);
    });
</script>

<%@ include file="../common/app-bottom.jsp" %>