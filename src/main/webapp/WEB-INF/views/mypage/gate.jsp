<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- Figma 24:15067. 원본은 기본 상태와 오류 상태를 세로로 쌓아 놓은 시안이라
     입력칸이 두 개로 보이지만, 실제 화면은 하나다. --%>
<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>보호자 확인이 필요해요</h1>
    <p class="sub">이 메뉴에는 아이의 감정 기록이 담겨 있어요.<br>보호자만 들어올 수 있습니다.</p>

    <form onsubmit="return kdGateSubmit(event)">
        <%-- PIN 입력은 온보딩 PIN 설정(/onboarding/pin)과 같은 자산을 쓴다.
             점은 글꼴이 아니라 .onb-pin-dots 가 그리고, auth-validate.js 가 입력 수만큼 켠다. --%>
        <div class="kd-field">
            <label class="kd-label" for="gatePin">보호자 PIN 4자리 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="gatePin" name="pin"
                       inputmode="numeric" maxlength="4" autocomplete="off" data-nocopy>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <p class="kd-hint" id="gateHint">가입할 때 설정한 4자리 숫자예요</p>
            <p class="kd-error" id="gateError" hidden></p>
        </div>
        <div class="row">
            <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
            <button type="submit" class="kd-btn kd-btn-primary">확인</button>
        </div>
        <a class="forgot" href="/mypage/pin-reset">PIN을 잊었어요</a>
    </form>
</div>

<script>
    /* 데모용 — 정답 PIN 을 화면에 박아 두었다. 백엔드가 붙으면 POST 검증으로 교체할 것. */
    var kdGateTries = 0;
    function kdGateSubmit(e) {
        e.preventDefault();
        var input = document.getElementById('gatePin');
        var hint = document.getElementById('gateHint');
        var error = document.getElementById('gateError');
        if (input.value === '1234') {
            location.href = '/mypage/account';
            return false;
        }
        kdGateTries = Math.min(kdGateTries + 1, 5);
        hint.hidden = true;
        error.hidden = false;
        error.textContent = 'PIN이 일치하지 않아요 (' + kdGateTries + '/5회)';
        input.classList.add('kd-input--error');
        input.select();
        return false;
    }
</script>

<%@ include file="../common/app-bottom.jsp" %>
