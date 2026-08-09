<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "보호자 PIN 재설정"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- PIN 재설정 — Figma 에 없는 화면이라 기존 자산만 조합했다.
     레이아웃은 보호자 확인 게이트(.mp-gate), 입력은 온보딩 PIN 설정(.onb-pin + .onb-pin-dots),
     인증번호 줄은 아이디·비밀번호 찾기(.kd-input-row + .kd-btn-pill + .kd-timer) 그대로다.

     ⚠ 인증 단계가 없으면 게이트가 무의미해진다 — PIN 을 몰라도 [PIN을 잊었어요] 로 들어와
       아무 값이나 새로 정하면 그만이라, 5회 잠금이 통째로 우회된다(2026-08-09 추가한 이유).
     실제 본인 확인(메일 발송·대조)은 백엔드 몫이고, 데모는 인증번호 040505 만 통과시킨다. --%>
<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>보호자 PIN을 다시 만들어요</h1>
    <p class="sub">가입할 때 쓴 이메일로 본인 확인을 한 뒤<br>새 PIN 4자리를 설정합니다.</p>

    <form onsubmit="return kdPinResetSubmit(event)">
        <div class="kd-field">
            <label class="kd-label" for="resetEmail">이메일 <span class="req">*</span></label>
            <div class="kd-input-row">
                <div class="kd-input-wrap">
                    <input class="kd-input" type="email" id="resetEmail" value="jiu@example.com" readonly>
                </div>
                <button type="button" class="kd-btn kd-btn-pill" onclick="kdPinSendCode()">인증번호 전송</button>
            </div>
            <p class="kd-hint" id="resetMailHint">가입할 때 등록한 이메일이에요</p>
        </div>

        <%-- 인증번호 칸은 [인증번호 전송]을 눌러야 나타난다 (아이디·비밀번호 찾기와 같은 규칙) --%>
        <div class="kd-field is-hidden" id="resetCodeField">
            <label class="kd-label" for="resetCode">인증번호</label>
            <div class="kd-input-row">
                <div class="kd-input-wrap">
                    <input class="kd-input" type="text" id="resetCode" inputmode="numeric" maxlength="6"
                           autocomplete="off" placeholder="인증번호 6자리를 입력하세요">
                    <span class="kd-timer" id="resetTimer">03:00</span>
                </div>
                <button type="button" class="kd-btn kd-btn-pill kd-btn-resend" onclick="kdPinSendCode()">재전송</button>
            </div>
            <p class="kd-hint" id="resetCodeHint" hidden></p>
            <p class="kd-error" id="resetCodeError" hidden></p>
        </div>

        <div class="kd-field">
            <label class="kd-label" for="newPin">새 PIN 4자리 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="newPin" name="newPin"
                       inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy disabled>
                <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
            </div>
            <p class="kd-hint" id="newPinHint">본인 확인을 마치면 입력할 수 있어요</p>
        </div>

        <div class="kd-field">
            <label class="kd-label" for="newPinCheck">새 PIN 확인 <span class="req">*</span></label>
            <div class="onb-pin-box">
                <input class="kd-input onb-pin" type="password" id="newPinCheck" name="newPinCheck"
                       inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy disabled>
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
    /* 데모 규칙 — 인증번호 040505 만 통과. auth-validate.js 의 DEMO_CODE 와 같은 값이다.
       백엔드가 붙으면 전송·대조를 전부 서버로 옮긴다. */
    var KD_DEMO_CODE = '040505';
    var kdCodeVerified = false;
    var kdCodeExpired = false;
    var kdCodeTimer = null;

    function $k(id) { return document.getElementById(id); }

    window.kdPinSendCode = function () {
        kdCodeVerified = false;
        kdCodeExpired = false;
        $k('resetCodeField').classList.remove('is-hidden');
        $k('resetCodeError').hidden = true;
        $k('resetCodeHint').hidden = false;
        $k('resetCodeHint').textContent = '인증번호를 보냈어요. 메일함을 확인해 주세요';
        $k('resetCode').disabled = false;
        $k('resetCode').value = '';
        $k('resetCode').focus();

        if (kdCodeTimer) clearInterval(kdCodeTimer);
        var left = 180;
        function render() {
            $k('resetTimer').textContent =
                ('0' + Math.floor(left / 60)).slice(-2) + ':' + ('0' + (left % 60)).slice(-2);
        }
        render();
        kdCodeTimer = setInterval(function () {
            left -= 1;
            if (left < 0) {
                clearInterval(kdCodeTimer); kdCodeTimer = null; kdCodeExpired = true;
                $k('resetCodeHint').hidden = true;
                $k('resetCodeError').hidden = false;
                $k('resetCodeError').textContent = '인증번호가 만료됐어요. 다시 받아 주세요';
                return;
            }
            render();
        }, 1000);
    };

    /* 6자리를 다 넣으면 바로 대조 — 맞으면 새 PIN 칸이 열린다 */
    document.getElementById('resetCode').addEventListener('input', function () {
        if (this.value.length < 6 || kdCodeExpired) return;
        if (this.value.trim() !== KD_DEMO_CODE) {
            $k('resetCodeHint').hidden = true;
            $k('resetCodeError').hidden = false;
            $k('resetCodeError').textContent = '인증번호가 일치하지 않아요';
            this.classList.add('kd-input--error');
            return;
        }
        kdCodeVerified = true;
        if (kdCodeTimer) { clearInterval(kdCodeTimer); kdCodeTimer = null; }
        this.classList.remove('kd-input--error');
        this.disabled = true;
        $k('resetTimer').hidden = true;
        $k('resetCodeError').hidden = true;
        $k('resetCodeHint').hidden = false;
        $k('resetCodeHint').textContent = '본인 확인이 끝났어요';
        $k('newPin').disabled = $k('newPinCheck').disabled = false;
        $k('newPinHint').textContent = '숫자 4자리를 입력해 주세요';
        $k('newPin').focus();
    });

    /* 잠긴 칸은 클릭 이벤트가 안 올라온다 → 감싸는 .onb-pin-box 에서 받는다.
       눌러도 아무 반응이 없으면 고장으로 오해한다("클릭이 안 돼요" 제보). */
    document.querySelectorAll('.mp-gate .onb-pin-box').forEach(function (box) {
        box.addEventListener('click', function () {
            if (kdCodeVerified) return;
            var sent = !$k('resetCodeField').classList.contains('is-hidden');
            $k('resetHint').hidden = true;
            $k('resetError').hidden = false;
            $k('resetError').textContent = sent
                ? '메일로 받은 인증번호 6자리를 먼저 입력해 주세요'
                : '[인증번호 전송]을 눌러 본인 확인을 먼저 해 주세요';
            var target = sent ? $k('resetCode') : document.querySelector('.mp-gate .kd-btn-pill');
            target.classList.add('kd-input--error');
            setTimeout(function () { target.classList.remove('kd-input--error'); }, 1200);
            if (sent) target.focus();
        });
    });

    function kdPinResetSubmit(e) {
        e.preventDefault();
        var a = $k('newPin'), b = $k('newPinCheck');
        var hint = $k('resetHint'), error = $k('resetError');

        function fail(msg) {
            hint.hidden = true;
            error.hidden = false;
            error.textContent = msg;
            b.classList.add('kd-input--error');
        }

        if (!kdCodeVerified) { fail('이메일 본인 확인을 먼저 해 주세요'); return false; }
        if (!/^\d{4}$/.test(a.value)) { fail('새 PIN 을 숫자 4자리로 입력해 주세요'); return false; }
        if (a.value !== b.value) { fail('PIN 이 일치하지 않습니다'); return false; }

        /* 데모 — 백엔드가 붙으면 POST 로 바꾼다.
           바꾼 PIN 을 저장하지 않으면 게이트가 계속 옛 값만 받아 "바꿨는데 안 들어가진다"가 된다.
           재설정에 성공했으니 5회 실패 잠금과 시도 횟수도 같이 푼다. */
        sessionStorage.setItem('kdPin', a.value);
        sessionStorage.removeItem('kdGateTries');
        sessionStorage.removeItem('kdGateLock');
        location.href = '/mypage';
        return false;
    }
</script>

<%@ include file="../common/app-bottom.jsp" %>
