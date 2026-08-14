<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% String pageTitle = "보호자 PIN 재설정"; String appNav = "mypage"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- PIN 재설정 1단계 — 본인 확인. 새 PIN 입력은 /mypage/pin-reset/new 로 넘어간다
     (비밀번호 찾기의 find-pw-email → find-pw-new 와 같은 구조).

     레이아웃은 보호자 확인 게이트(.mp-gate), 인증번호 줄은 아이디·비밀번호 찾기
     (.kd-input-row + .kd-btn-pill + .kd-timer) 그대로다.

     ⚠ 인증 단계가 없으면 게이트가 무의미해진다 — PIN 을 몰라도 [PIN을 잊었어요] 로 들어와
       아무 값이나 새로 정하면 그만이라, 5회 잠금이 통째로 우회된다.
     발송·대조는 전부 서버가 한다(/sendAuthCodeProc → /pinResetVerifyProc). 화면엔 정답이 없다. --%>
<div class="mp-gate">
    <span class="ic"><img src="/img/icon-hand.png" alt=""></span>
    <h1>보호자 PIN을 다시 만들어요</h1>
    <p class="sub">가입할 때 쓴 이메일로 본인 확인을 해 주세요.<br>확인이 끝나면 새 PIN을 설정합니다.</p>

    <%-- 인증번호 6자리가 맞으면 곧바로 2단계로 넘어간다 — [다음] 버튼은 없다 --%>
    <form onsubmit="return false">
        <div class="kd-field">
            <label class="kd-label" for="email">이메일 <span class="req">*</span></label>
            <div class="kd-input-row">
                <div class="kd-input-wrap">
                    <%-- 계정 이메일을 서버가 채워 준다(UserController.mypagePinReset).
                         ⚠ readonly 는 오타 방지용이다 — 서버는 화면이 보낸 주소를 아예 안 받고
                           세션의 계정 이메일로만 대조한다. --%>
                    <input class="kd-input mp-input-ro" type="email" id="email" name="email"
                           value="<c:out value='${myEmail}'/>" readonly>
                </div>
                <button type="button" class="kd-btn kd-btn-pill"
                        onclick="kdSendCode('pinReset')">인증번호 전송</button>
            </div>
            <p class="kd-hint">가입할 때 등록한 이메일이에요</p>
        </div>

        <%-- 인증번호 칸은 [인증번호 전송]을 눌러야 나타난다 (아이디·비밀번호 찾기와 같은 규칙) --%>
        <div class="kd-field is-hidden" id="authCodeField">
            <label class="kd-label" for="authCode">인증번호</label>
            <div class="kd-input-row">
                <div class="kd-input-wrap">
                    <input class="kd-input" type="text" id="authCode" inputmode="numeric" maxlength="6"
                           autocomplete="off" placeholder="인증번호 6자리를 입력하세요">
                    <span class="kd-timer">03:00</span>
                </div>
                <button type="button" class="kd-btn kd-btn-pill kd-btn-resend"
                        onclick="kdSendCode('pinReset')">재전송</button>
            </div>
        </div>
    </form>
</div>

<script>
    /* 6자리를 다 넣으면 바로 서버에 대조를 맡긴다 — 맞으면 서버가 2단계 주소를 알려 준다.
       ⚠ auth-validate.js 는 defer 라 이 인라인 스크립트보다 늦게 돈다. */
    document.addEventListener('DOMContentLoaded', function () {
        document.getElementById('authCode').addEventListener('input', function () {
            if (this.value.trim().length === 6) kdSubmitPinReset();
        });
    });
</script>

<%@ include file="../common/app-bottom.jsp" %>
