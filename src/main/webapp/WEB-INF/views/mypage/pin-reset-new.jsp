<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "보호자 PIN 재설정"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- PIN 재설정 2단계 — 새 PIN 입력. 1단계(/mypage/pin-reset)에서 본인 확인을 마쳐야 열린다.
     입력은 온보딩 PIN 설정(.onb-pin + .onb-pin-dots) 자산 그대로다. --%>
<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>새 PIN을 정해 주세요</h1>
    <p class="sub">본인 확인이 끝났어요.<br>앞으로 쓸 PIN 4자리를 입력해 주세요.</p>

    <form onsubmit="return kdPinResetSubmit(event)">
        <div class="kd-field">
            <label class="kd-label" for="newPin">새 PIN 4자리 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="newPin" name="newPin"
                       inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <p class="kd-hint" id="newPinHint">숫자 4자리를 입력해 주세요</p>
            <%-- 자릿수 오류가 붙을 자리. 없으면 '새 PIN 확인' 칸으로 밀려나 엉뚱한 곳이 빨개진다 --%>
            <p class="kd-error" id="newPinError" hidden></p>
        </div>

        <div class="kd-field">
            <label class="kd-label" for="newPinCheck">새 PIN 확인 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="newPinCheck" name="newPinCheck"
                       inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <p class="kd-hint" id="resetHint">한 번 더 똑같이 입력해 주세요</p>
            <p class="kd-error" id="resetError" hidden></p>
        </div>

        <div class="row">
            <a class="kd-btn kd-btn-outline" href="/mypage">취소</a>
            <button type="submit" class="kd-btn kd-btn-primary">변경</button>
        </div>
    </form>
</div>

<script>
    function $k(id) { return document.getElementById(id); }

    /* 본인 확인을 건너뛰고 주소만 쳐서 들어온 경우 — 1단계로 돌려보낸다.
       ⚠ 브라우저 안에서만 막는 데모다. 백엔드가 붙으면 서버가 토큰을 검사해야 한다. */
    if (!sessionStorage.getItem('kdPinResetOk')) location.replace('/mypage/pin-reset');

    function kdPinResetSubmit(e) {
        e.preventDefault();
        var a = $k('newPin'), b = $k('newPinCheck');

        /* 오류는 그 오류가 난 칸에 붙인다.
           ⚠ 전에는 무슨 오류든 '새 PIN 확인' 칸(resetError)에 몰아 넣어서
             엉뚱하게 마지막 칸이 빨개졌다(2026-08-10 지적). */
        function fail(input, errEl, hintEl, msg) {
            hintEl.hidden = true;
            errEl.hidden = false;
            errEl.textContent = msg;
            input.classList.add('kd-input--error');
            input.focus();
        }

        /* 누른 순간 앞선 오류 표시는 전부 지운다 — 안 지우면 고친 칸이 계속 빨갛다 */
        ['newPinError', 'resetError'].forEach(function (id) { $k(id).hidden = true; });
        ['resetHint', 'newPinHint'].forEach(function (id) { $k(id).hidden = false; });
        [a, b].forEach(function (el) { el.classList.remove('kd-input--error'); });

        /* 자릿수는 '새 PIN' 칸의 문제다 — 확인 칸에 붙이면 엉뚱한 곳이 빨개진다 */
        if (!/^\d{4}$/.test(a.value)) {
            fail(a, $k('newPinError'), $k('newPinHint'), '새 PIN 을 숫자 4자리로 입력해 주세요');
            return false;
        }
        if (a.value !== b.value) {
            fail(b, $k('resetError'), $k('resetHint'), 'PIN 이 일치하지 않습니다');
            return false;
        }

        /* 데모 — 백엔드가 붙으면 POST 로 바꾼다.
           바꾼 PIN 을 저장하지 않으면 게이트가 계속 옛 값만 받아 "바꿨는데 안 들어가진다"가 된다.
           재설정에 성공했으니 5회 실패 잠금과 시도 횟수도 같이 푼다. */
        sessionStorage.setItem('kdPin', a.value);
        sessionStorage.removeItem('kdGateTries');
        sessionStorage.removeItem('kdGateLock');
        sessionStorage.removeItem('kdPinResetOk');
        location.href = '/mypage';
        return false;
    }
</script>

<%@ include file="../common/app-bottom.jsp" %>
