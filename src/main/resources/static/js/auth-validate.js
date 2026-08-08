/* 로그인·회원가입·아이디/비밀번호찾기 클라이언트 유효성 검사 (데모)
   - 백엔드 연동 시 서버 검증으로 대체하고, 인증번호는 실제 발송·대조로 교체할 것
   - 데모 규칙: 임시 인증번호 040505만 통과 */
(function () {
    'use strict';

    /* ---------- 공통 헬퍼 ---------- */
    function $(id) { return document.getElementById(id); }

    function fieldOf(input) { return input.closest('.kd-field'); }

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
        field.querySelectorAll('.kd-error, .kd-info').forEach(function (e) { e.remove(); });
        var hint = field.querySelector('.kd-hint');
        if (hint) hint.style.visibility = '';
    }

    function clearAllErrors() {
        document.querySelectorAll('.kd-error, .kd-info').forEach(function (e) { e.remove(); });
        document.querySelectorAll('.kd-input--error').forEach(function (i) { i.classList.remove('kd-input--error'); });
        document.querySelectorAll('.kd-hint').forEach(function (h) { h.style.visibility = ''; });
    }

    /* 안내(초록) — 오류와 같은 예약석을 쓰는 긍정 문구. 인증번호 전송 안내 등 */
    function setInfo(input, msg) {
        clearError(input);
        var field = fieldOf(input);
        if (!field) return;
        var hint = field.querySelector('.kd-hint');
        if (hint) hint.style.visibility = 'hidden';
        var p = document.createElement('p');
        p.className = 'kd-info';
        p.textContent = msg;
        field.appendChild(p);
    }

    var EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    var NAME_RE = /^[가-힣a-zA-Z]{2,20}$/;   /* 이름: 한글·영문 2~20자 */
    var ID_RE = /^[a-z0-9]{4,12}$/;          /* 아이디: 영문 소문자·숫자 4~12자 */
    var CODE_RE = /^\d{6}$/;
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

    /* 이름·아이디는 형식까지 본다 — "멘트 유형이 적다"는 팀장 피드백(2026-08-08) 반영 */
    function checkUserName(input) {
        var v = input.value.trim();
        if (!v) { setError(input, '이름을 입력해 주세요'); return false; }
        if (!NAME_RE.test(v)) { setError(input, '이름은 한글 또는 영문 2~20자로 입력해 주세요'); return false; }
        clearError(input); return true;
    }

    function checkLoginId(input) {
        var v = input.value.trim();
        if (!v) { setError(input, '아이디를 입력해 주세요'); return false; }
        if (!ID_RE.test(v)) { setError(input, '아이디는 영문 소문자와 숫자 4~12자로 입력해 주세요'); return false; }
        clearError(input); return true;
    }

    /* 비밀번호 규칙 한 곳 — 제출 검사와 실시간 검사가 같이 쓴다.
       어긴 이유를 돌려줘야 원인별 멘트가 된다 (빈 문자열 = 통과) */
    function pwWhy(v) {
        if (v.length < 8) return '비밀번호는 8자 이상이어야 합니다';
        if (!/[a-zA-Z]/.test(v)) return '비밀번호에 영문을 포함해 주세요';
        if (!/\d/.test(v)) return '비밀번호에 숫자를 포함해 주세요';
        return '';
    }

    function pwOk(v) { return !pwWhy(v); }

    function checkPassword(input) {
        if (!input.value) { setError(input, '비밀번호를 입력해 주세요'); return false; }
        var why = pwWhy(input.value);
        if (why) { setError(input, why); return false; }
        clearError(input); return true;
    }

    function checkMatch(pw, pwc) {
        if (!pwc.value) { setError(pwc, '비밀번호를 한 번 더 입력해 주세요'); return false; }
        if (pwc.value !== pw.value) { setError(pwc, '비밀번호가 일치하지 않습니다'); return false; }
        clearError(pwc); return true;
    }

    /* emailOk: 이메일 검사 결과 — 이메일 자체가 틀렸으면 그 오류를 덮지 않는다
       (빈 이메일에 "인증번호를 먼저 전송해 주세요"가 뜨면 원인을 잘못 가리킨다) */
    function checkCode(emailOk) {
        var field = $('authCodeField');
        var code = $('authCode');
        var email = $('email');
        if (field && field.classList.contains('is-hidden')) {
            if (emailOk) setError(email, '인증번호를 먼저 전송해 주세요');
            return false;
        }
        if (!CODE_RE.test(code.value.trim())) {
            setError(code, '인증번호 6자리를 입력해 주세요'); return false;
        }
        if (codeExpired) {
            setError(code, '인증 시간이 지났습니다. 인증번호를 다시 전송해 주세요'); return false;
        }
        if (code.value.trim() !== DEMO_CODE) {
            setError(code, '인증번호가 일치하지 않습니다'); return false;
        }
        clearError(code); return true;
    }

    /* ---------- 인증번호 전송(칸 표시 + 타이머) ---------- */
    var timerHandle = null;
    var codeExpired = false;    /* 타이머가 0이 되면 참 — 재전송해야 풀린다 */

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
            if (left < 0) { clearInterval(timerHandle); codeExpired = true; return; }
            render();
        }, 1000);
    }

    /* kind: 'signup' | 'findId' | 'findPw' — 전송 전 이메일(아찾은 이름도) 먼저 검사 */
    window.kdSendCode = function (kind) {
        clearAllErrors();
        var ok = true;
        if (kind === 'findId') ok = checkUserName($('userName')) && ok;
        ok = checkEmail($('email')) && ok;
        if (!ok) return;
        codeExpired = false;
        var field = $('authCodeField');
        field.classList.remove('is-hidden');
        startTimer(field);
        setInfo($('email'), '인증번호를 보냈습니다. 메일함을 확인해 주세요');
        $('authCode').focus();
    };

    /* ---------- 폼별 제출 검증 ---------- */
    window.kdSubmitLogin = function () {
        clearAllErrors();
        /* 통과 시 이동할 다음 화면은 백엔드(로그인 처리) 영역 — 데모에서는 검증까지만 */
        checkRequired($('loginId'), '아이디를 입력해 주세요');
        checkRequired($('password'), '비밀번호를 입력해 주세요');
    };

    window.kdSubmitSignup = function () {
        clearAllErrors();
        var ok = checkUserName($('userName'));
        ok = checkLoginId($('loginId')) && ok;
        ok = checkPassword($('password')) && ok;
        ok = checkMatch($('password'), $('passwordCheck')) && ok;
        var emailOk = checkEmail($('email'));
        ok = checkCode(emailOk) && emailOk && ok;
        if (ok) location.href = '/signup/done';
    };

    window.kdSubmitFindId = function () {
        clearAllErrors();
        var ok = checkUserName($('userName'));
        var emailOk = checkEmail($('email'));
        ok = checkCode(emailOk) && emailOk && ok;
        if (ok) location.href = '/find-id/result';
    };

    window.kdSubmitFindPwEmail = function () {
        clearAllErrors();
        var emailOk = checkEmail($('email'));
        var ok = checkCode(emailOk) && emailOk;
        if (ok) location.href = '/find-pw/new';
    };

    window.kdSubmitFindPwNew = function () {
        clearAllErrors();
        var ok = checkPassword($('newPassword'));
        ok = checkMatch($('newPassword'), $('newPasswordCheck')) && ok;
        if (ok) location.href = '/find-pw/done';
    };

    /* ---------- 실시간 오류 해제 — 오류가 떠 있는 필드만 입력 시 재검증 ---------- */
    function notBlank(el) { return !!el.value.trim(); }
    function pwLive(el) { return pwOk(el.value); }

    var LIVE_CHECKS = {
        userName: function (el) { return NAME_RE.test(el.value.trim()); },
        /* 아이디 형식은 회원가입에서만 — 로그인 화면(비밀번호 확인 칸 없음)은 입력 여부만 본다 */
        loginId: function (el) { return $('passwordCheck') ? ID_RE.test(el.value.trim()) : notBlank(el); },
        email: function (el) { return EMAIL_RE.test(el.value.trim()); },
        password: pwLive,
        newPassword: pwLive,
        passwordCheck: function (el) { return !!el.value && el.value === $('password').value; },
        newPasswordCheck: function (el) { return !!el.value && el.value === $('newPassword').value; },
        authCode: function (el) { return !codeExpired && el.value.trim() === DEMO_CODE; }
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

    /* ---------- data-nocopy 필드: 복사·잘라내기·붙여넣기 차단 ----------
       비밀번호를 눈으로 다시 치게 해 오타를 잡는 목적. 로그인 화면은 대상이 아니다
       (비밀번호 관리자 자동입력을 막으면 안 되므로). */
    ['copy', 'cut', 'paste'].forEach(function (evt) {
        document.addEventListener(evt, function (e) {
            if (e.target.hasAttribute && e.target.hasAttribute('data-nocopy')) e.preventDefault();
        });
    });

    /* ---------- 약관 동의 ---------- */
    var REQUIRED_TERMS = ['agreeTerms', 'agreePrivacy', 'agreeSensitive'];

    function termsBoxes() {
        return Array.prototype.slice.call(document.querySelectorAll('.terms-item .kd-check'));
    }

    /* 하위 항목이 전부 켜지면 [전체 동의]도 켜고, 하나라도 꺼지면 끈다 */
    function syncAgreeAll() {
        var all = $('agreeAll');
        var boxes = termsBoxes();
        all.checked = boxes.length > 0 && boxes.every(function (c) { return c.checked; });
    }

    function clearTermsError() {
        var msg = $('termsError');
        if (msg) msg.classList.remove('is-shown');
        REQUIRED_TERMS.forEach(function (id) {
            if ($(id)) $(id).classList.remove('kd-check--error');
        });
    }

    document.addEventListener('change', function (e) {
        var el = e.target;
        if (el.id === 'agreeAll') {
            /* 전체 동의 → 하위 일괄 토글 (해제 포함) */
            termsBoxes().forEach(function (c) { c.checked = el.checked; });
        } else if (el.classList && el.classList.contains('kd-check') && el.closest('.terms-item')) {
            /* 하위 항목 → 전체 동의 역방향 동기화 */
            syncAgreeAll();
        } else {
            return;
        }
        clearTermsError();
    });

    /* 필수 3개를 모두 동의해야 다음 단계로 — 선택(마케팅)은 검사하지 않는다 */
    window.kdSubmitTerms = function () {
        clearTermsError();
        var missing = REQUIRED_TERMS.filter(function (id) { return !$(id).checked; });
        if (missing.length) {
            missing.forEach(function (id) { $(id).classList.add('kd-check--error'); });
            var msg = $('termsError');
            msg.textContent = '필수 약관에 모두 동의해 주세요';
            msg.classList.add('is-shown');
            $(missing[0]).focus();
            return;
        }
        location.href = '/signup/form';
    };

    /* ---------- 새 비밀번호 규칙 체크리스트 실시간 반영 ----------
       head.jsp 가 이 스크립트를 defer 로 싣기 때문에 여기선 DOM이 이미 준비돼 있다 */
    var newPw = $('newPassword');
    if (newPw && $('ruleLen')) {
        newPw.addEventListener('input', function () {
            var v = newPw.value;
            $('ruleLen').classList.toggle('ok', v.length >= 8);
            $('ruleAlpha').classList.toggle('ok', /[a-zA-Z]/.test(v));
            $('ruleNum').classList.toggle('ok', /\d/.test(v));
        });
    }
})();
