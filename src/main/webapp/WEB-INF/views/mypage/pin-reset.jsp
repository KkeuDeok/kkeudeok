<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "보호자 PIN 재설정"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- PIN 재설정 1단계 — 본인 확인. 새 PIN 입력은 /mypage/pin-reset/new 로 넘어간다
     (비밀번호 찾기의 find-pw-email → find-pw-new 와 같은 구조).

     레이아웃은 보호자 확인 게이트(.mp-gate), 인증번호 줄은 아이디·비밀번호 찾기
     (.kd-input-row + .kd-btn-pill + .kd-timer) 그대로다.

     ⚠ 인증 단계가 없으면 게이트가 무의미해진다 — PIN 을 몰라도 [PIN을 잊었어요] 로 들어와
       아무 값이나 새로 정하면 그만이라, 5회 잠금이 통째로 우회된다(2026-08-09 추가한 이유).
     실제 본인 확인(메일 발송·대조)은 백엔드 몫이고, 데모는 인증번호 040505 만 통과시킨다. --%>
<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>보호자 PIN을 다시 만들어요</h1>
    <p class="sub">가입할 때 쓴 이메일로 본인 확인을 해 주세요.<br>확인이 끝나면 새 PIN을 설정합니다.</p>

    <%-- 인증번호 6자리가 맞으면 곧바로 2단계로 넘어간다 — [다음] 버튼은 없다 --%>
    <form onsubmit="return false">
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

    /* 뒤로 돌아왔을 때 이전 통과 기록이 남아 있으면 안 된다 — 다시 인증해야 한다 */
    sessionStorage.removeItem('kdPinResetOk');

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

    /* 6자리를 다 넣으면 바로 대조 — 맞으면 그대로 2단계로 넘어간다 */
    document.getElementById('resetCode').addEventListener('input', function () {
        if (this.value.length < 6 || kdCodeExpired || kdCodeVerified) return;
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

        /* 데모 — 백엔드가 붙으면 서버가 준 1회용 토큰으로 바꾼다.
           이 값이 없으면 2단계 페이지는 주소를 직접 쳐도 열리지 않는다. */
        sessionStorage.setItem('kdPinResetOk', '1');
        /* 통과 문구를 한 박자 보여 주고 넘긴다 — 즉시 이동하면 확인됐는지 모른 채 화면이 바뀐다 */
        setTimeout(function () { location.href = '/mypage/pin-reset/new'; }, 600);
    });
</script>

<%@ include file="../common/app-bottom.jsp" %>
