<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- Figma 24:15067. 원본은 기본 상태와 오류 상태를 세로로 쌓아 놓은 시안이라
     입력칸이 두 개로 보이지만, 실제 화면은 하나다.

     ⚠ 맞는지는 서버(/verifyParentPinProc)가 판정한다. 예전에는 정답 PIN(1234)이 이 파일에
       박혀 있었고 실패 횟수·잠금도 sessionStorage 라, 값을 지우면 그냥 열렸다.
       통과하면 서버가 세션에 표를 끊고, 그 표가 있어야 /mypage/account 이하가 열린다. --%>
<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>보호자 확인이 필요해요</h1>
    <p class="sub">이 메뉴에는 아이의 감정 기록이 담겨 있어요.<br>보호자만 들어올 수 있습니다.</p>

    <form onsubmit="kdSubmitGate(); return false;">
        <%-- PIN 입력은 온보딩 PIN 설정(/onboarding/pin)과 같은 자산을 쓴다.
             점은 글꼴이 아니라 .onb-pin-dots 가 그리고, auth-validate.js 가 입력 수만큼 켠다. --%>
        <div class="kd-field">
            <label class="kd-label" for="gatePin">보호자 PIN 4자리 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="gatePin" name="pin"
                       inputmode="numeric" maxlength="4" autocomplete="off" data-nocopy>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <%-- 오류 문구는 auth-validate.js 가 이 자리에 만들어 붙인다(.kd-error) --%>
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
    /* 잠겨 있는 상태로 들어왔는지는 서버가 알려 준다(UserController.mypage).
       새로고침해도 잠금이 그대로 보여야 하는데, 남은 시간을 화면이 재면 시계를 돌려 풀 수 있다.

       ⚠ auth-validate.js 는 defer 라 이 인라인 스크립트보다 늦게 돈다 — 바로 부르면 없는 함수다. */
    document.addEventListener('DOMContentLoaded', function () {
        kdPinLock(Number('${pinLockLeft}') || 0);
    });
</script>

<%@ include file="../common/app-bottom.jsp" %>
