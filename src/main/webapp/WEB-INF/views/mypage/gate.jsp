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
    /* 데모용 — 정답 PIN 을 화면에 박아 두었다. 백엔드가 붙으면 POST 검증으로 교체할 것.
       ⚠ 지금은 브라우저 안에서만 막는다. 개발자도구로 sessionStorage 를 지우면 풀린다 —
          실제 잠금은 서버가 계정 단위로 세야 한다(로그인 시도 제한과 같은 규칙). */
    var KD_MAX_TRY = 5;      // 5회 틀리면
    var KD_LOCK_MS = 5 * 60 * 1000;   // 5분 잠금
    /* PIN 재설정(/mypage/pin-reset)에서 바꾼 값을 받는다 — 안 읽으면 "바꿨는데 안 들어가진다" */
    var KD_PIN = sessionStorage.getItem('kdPin') || '1234';

    var kdTries = +(sessionStorage.getItem('kdGateTries') || 0);
    var kdLockUntil = +(sessionStorage.getItem('kdGateLock') || 0);
    var kdTimer = null;

    function kdGateEls() {
        return {
            input: document.getElementById('gatePin'),
            hint: document.getElementById('gateHint'),
            error: document.getElementById('gateError'),
            submit: document.querySelector('.mp-gate button[type=submit]')
        };
    }

    function kdGatePaint() {
        var e = kdGateEls();
        var left = Math.ceil((kdLockUntil - Date.now()) / 1000);

        if (left > 0) {
            e.input.disabled = e.submit.disabled = true;
            e.input.value = '';
            if (window.kdPinDots) kdPinDots(e.input);
            e.hint.hidden = true;
            e.error.hidden = false;
            e.error.textContent = '5회 틀려서 ' + Math.floor(left / 60) + '분 ' +
                ('0' + (left % 60)).slice(-2) + '초 동안 잠겼어요. PIN을 잊었다면 아래에서 다시 설정하세요';
            if (!kdTimer) kdTimer = setInterval(kdGatePaint, 1000);
            return;
        }

        if (kdTimer) { clearInterval(kdTimer); kdTimer = null; }
        if (kdLockUntil) {           // 잠금이 막 풀린 순간 — 횟수도 초기화
            kdLockUntil = 0; kdTries = 0;
            sessionStorage.removeItem('kdGateLock');
            sessionStorage.removeItem('kdGateTries');
            e.error.hidden = true;
            e.hint.hidden = false;
            e.input.classList.remove('kd-input--error');
        }
        e.input.disabled = e.submit.disabled = false;
    }

    function kdGateSubmit(ev) {
        ev.preventDefault();
        var e = kdGateEls();
        if (Date.now() < kdLockUntil) return false;

        if (e.input.value === KD_PIN) {
            sessionStorage.removeItem('kdGateTries');
            location.href = '/mypage/account';
            return false;
        }

        kdTries += 1;
        sessionStorage.setItem('kdGateTries', kdTries);
        e.input.classList.add('kd-input--error');
        e.hint.hidden = true;
        e.error.hidden = false;

        if (kdTries >= KD_MAX_TRY) {
            kdLockUntil = Date.now() + KD_LOCK_MS;
            sessionStorage.setItem('kdGateLock', kdLockUntil);
            kdGatePaint();
        } else {
            e.error.textContent = 'PIN이 일치하지 않아요 (' + kdTries + '/' + KD_MAX_TRY + '회)';
            e.input.select();
        }
        return false;
    }

    kdGatePaint();   /* 새로고침해도 잠금이 유지되도록 그릴 것 */
</script>

<%@ include file="../common/app-bottom.jsp" %>
