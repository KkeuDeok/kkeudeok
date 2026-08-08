<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "보호자 PIN 재설정"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- PIN 재설정 — Figma 에 없는 화면이라 기존 자산만 조합했다.
     레이아웃은 보호자 확인 게이트(.mp-gate), 입력은 온보딩 PIN 설정(.onb-pin + .onb-pin-dots)
     그대로다. 실제 본인 확인은 백엔드 몫이라 데모에서는 형식 검사만 한다. --%>
<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>보호자 PIN을 다시 만들어요</h1>
    <p class="sub">가입할 때 쓴 이메일로 본인 확인을 한 뒤<br>새 PIN 4자리를 설정합니다.</p>

    <form onsubmit="return kdPinResetSubmit(event)">
        <div class="kd-field">
            <label class="kd-label" for="resetEmail">이메일 <span class="req">*</span></label>
            <input class="kd-input" type="email" id="resetEmail" value="jiu@example.com" readonly>
            <p class="kd-hint">가입할 때 등록한 이메일이에요</p>
        </div>

        <div class="kd-field">
            <label class="kd-label" for="newPin">새 PIN 4자리 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="newPin" name="newPin"
                       inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <p class="kd-hint">숫자 4자리를 입력해 주세요</p>
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
    function kdPinResetSubmit(e) {
        e.preventDefault();
        var a = document.getElementById('newPin');
        var b = document.getElementById('newPinCheck');
        var hint = document.getElementById('resetHint');
        var error = document.getElementById('resetError');

        function fail(msg) {
            hint.hidden = true;
            error.hidden = false;
            error.textContent = msg;
            b.classList.add('kd-input--error');
        }

        if (!/^\d{4}$/.test(a.value)) { fail('새 PIN 을 숫자 4자리로 입력해 주세요'); return false; }
        if (a.value !== b.value) { fail('PIN 이 일치하지 않습니다'); return false; }

        /* 데모 — 백엔드가 붙으면 POST 로 바꾼다 */
        location.href = '/mypage';
        return false;
    }
</script>

<%@ include file="../common/app-bottom.jsp" %>
