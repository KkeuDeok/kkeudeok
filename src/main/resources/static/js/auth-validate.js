/* 로그인·회원가입·아이디/비밀번호찾기 클라이언트 유효성 검사 (데모)
   - 백엔드 연동 시 서버 검증으로 대체하고, 인증번호는 실제 발송·대조로 교체할 것
   - 데모 규칙: 임시 인증번호 040505만 통과 */
(function () {
    'use strict';

    /* ---------- 공통 헬퍼 ---------- */
    function $(id) { return document.getElementById(id); }

    function fieldOf(input) {
        var el = input;
        while (el && !el.classList.contains('kd-field')) el = el.parentElement;
        return el;
    }

    function setError(input, msg) {
        var field = fieldOf(input);
        if (!field) return;
        clearError(input);
        input.classList.add('kd-input--error');
        /* 힌트와 오류는 같은 자리를 쓰므로 오류가 뜨는 동안은 힌트를 감춘다 */
        var hint = field.querySelector('.kd-hint');
        if (hint) hint.style.visibility = 'hidden';
        var p = document.createElement('p');
        p.className = 'kd-error';
        p.textContent = msg;
        field.appendChild(p);
    }

    function clearError(input) {
        var field = fieldOf(input);
        if (!field) return;
        input.classList.remove('kd-input--error');
        var old = field.querySelector('.kd-error');
        if (old) old.remove();
        var hint = field.querySelector('.kd-hint');
        if (hint) hint.style.visibility = '';
    }

    function clearAllErrors() {
        document.querySelectorAll('.kd-error').forEach(function (e) { e.remove(); });
        document.querySelectorAll('.kd-input--error').forEach(function (i) { i.classList.remove('kd-input--error'); });
        document.querySelectorAll('.kd-hint').forEach(function (h) { h.style.visibility = ''; });
    }

    var EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    var DEMO_CODE = '040505';   /* 임시 인증번호 — 백엔드 연동 시 제거 */

    function checkRequired(input, msg) {
        if (!input.value.trim()) { setError(input, msg); return false; }
        clearError(input); return true;
    }

    function checkEmail(input) {
        if (!input.value.trim()) { setError(input, '이메일을 입력해 주세요'); return false; }
        if (!EMAIL_RE.test(input.value.trim())) { setError(input, '이메일 형식이 올바르지 않습니다'); return false; }
        clearError(input); return true;
    }

    function checkPassword(input) {
        var v = input.value;
        if (!v) { setError(input, '비밀번호를 입력해 주세요'); return false; }
        if (v.length < 8 || !/[a-zA-Z]/.test(v) || !/\d/.test(v)) {
            setError(input, '비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다'); return false;
        }
        clearError(input); return true;
    }

    function checkCode() {
        var field = $('authCodeField');
        var code = $('authCode');
        var email = $('email');
        if (field && field.classList.contains('is-hidden')) {
            setError(email, '인증번호를 먼저 전송해 주세요'); return false;
        }
        if (code.value.trim() !== DEMO_CODE) {
            setError(code, '인증번호가 일치하지 않습니다'); return false;
        }
        clearError(code); return true;
    }

    /* ---------- 인증번호 전송(칸 표시 + 타이머) ---------- */
    var timerHandle = null;

    function startTimer(field) {
        var span = field.querySelector('.kd-timer');
        if (!span) return;
        if (timerHandle) clearInterval(timerHandle);
        var left = 180;
        function render() {
            var m = String(Math.floor(left / 60)).padStart(2, '0');
            var s = String(left % 60).padStart(2, '0');
            span.textContent = m + ':' + s;
        }
        render();
        timerHandle = setInterval(function () {
            left -= 1;
            if (left < 0) { clearInterval(timerHandle); return; }
            render();
        }, 1000);
    }

    /* kind: 'signup' | 'findId' | 'findPw' — 전송 전 이메일(아찾은 이름도) 먼저 검사 */
    window.kdSendCode = function (kind) {
        clearAllErrors();
        var ok = true;
        if (kind === 'findId') ok = checkRequired($('userName'), '이름을 입력해 주세요') && ok;
        ok = checkEmail($('email')) && ok;
        if (!ok) return;
        var field = $('authCodeField');
        field.classList.remove('is-hidden');
        startTimer(field);
        $('authCode').focus();
    };

    /* ---------- 폼별 제출 검증 ---------- */
    window.kdSubmitLogin = function () {
        clearAllErrors();
        var ok = checkRequired($('loginId'), '아이디를 입력해 주세요');
        ok = checkRequired($('password'), '비밀번호를 입력해 주세요') && ok;
        /* 통과 시 이동할 다음 화면은 백엔드(로그인 처리) 영역 — 데모에서는 검증까지만 */
    };

    window.kdSubmitSignup = function () {
        clearAllErrors();
        var ok = checkRequired($('userName'), '이름을 입력해 주세요');
        ok = checkRequired($('loginId'), '아이디를 입력해 주세요') && ok;
        ok = checkPassword($('password')) && ok;
        var pw = $('password'), pwc = $('passwordCheck');
        if (pwc.value !== pw.value || !pwc.value) {
            setError(pwc, '비밀번호가 일치하지 않습니다'); ok = false;
        } else clearError(pwc);
        ok = checkEmail($('email')) && ok;
        ok = checkCode() && ok;
        if (ok) location.href = '/signup/done';
    };

    window.kdSubmitFindId = function () {
        clearAllErrors();
        var ok = checkRequired($('userName'), '이름을 입력해 주세요');
        ok = checkEmail($('email')) && ok;
        ok = checkCode() && ok;
        if (ok) location.href = '/find-id/result';
    };

    window.kdSubmitFindPwEmail = function () {
        clearAllErrors();
        var ok = checkEmail($('email'));
        ok = checkCode() && ok;
        if (ok) location.href = '/find-pw/new';
    };

    window.kdSubmitFindPwNew = function () {
        clearAllErrors();
        var pw = $('newPassword'), pwc = $('newPasswordCheck');
        var ok = checkPassword(pw);
        if (pwc.value !== pw.value || !pwc.value) {
            setError(pwc, '비밀번호가 일치하지 않습니다'); ok = false;
        } else clearError(pwc);
        if (ok) location.href = '/find-pw/done';
    };

    /* ---------- 실시간 오류 해제 — 오류가 떠 있는 필드만 입력 시 재검증 ---------- */
    var LIVE_CHECKS = {
        userName: function (el) { return !!el.value.trim(); },
        loginId: function (el) { return !!el.value.trim(); },
        email: function (el) { return EMAIL_RE.test(el.value.trim()); },
        password: function (el) { var v = el.value; return v.length >= 8 && /[a-zA-Z]/.test(v) && /\d/.test(v); },
        newPassword: function (el) { var v = el.value; return v.length >= 8 && /[a-zA-Z]/.test(v) && /\d/.test(v); },
        passwordCheck: function (el) { return !!el.value && el.value === $('password').value; },
        newPasswordCheck: function (el) { return !!el.value && el.value === $('newPassword').value; },
        authCode: function (el) { return el.value.trim() === DEMO_CODE; }
    };

    /* 비밀번호를 고치면 확인 필드의 일치 여부도 달라진다 */
    var PAIRS = { password: 'passwordCheck', newPassword: 'newPasswordCheck' };

    function hasError(input) {
        var field = fieldOf(input);
        return field && !!field.querySelector('.kd-error');
    }

    function liveRevalidate(input) {
        var check = LIVE_CHECKS[input.id];
        if (!check || !hasError(input)) return;
        if (check(input)) clearError(input);
    }

    document.addEventListener('input', function (e) {
        var el = e.target;
        if (!el.classList || !el.classList.contains('kd-input')) return;
        liveRevalidate(el);
        var pairId = PAIRS[el.id];
        if (pairId && $(pairId)) liveRevalidate($(pairId));
    });

    /* ---------- 새 비밀번호 규칙 체크리스트 실시간 반영 ---------- */
    document.addEventListener('DOMContentLoaded', function () {
        var pw = $('newPassword');
        if (!pw || !$('ruleLen')) return;
        pw.addEventListener('input', function () {
            var v = pw.value;
            $('ruleLen').classList.toggle('ok', v.length >= 8);
            $('ruleAlpha').classList.toggle('ok', /[a-zA-Z]/.test(v));
            $('ruleNum').classList.toggle('ok', /\d/.test(v));
        });
    });
})();
