/* 로그인·회원가입·아이디/비밀번호찾기·온보딩 클라이언트 유효성 검사 (데모)
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

    /* 비밀번호 규칙 한 곳 — 제출 검사와 실시간 검사가 같이 쓴다 */
    function pwOk(v) { return v.length >= 8 && /[a-zA-Z]/.test(v) && /\d/.test(v); }

    function checkPassword(input) {
        if (!input.value) { setError(input, '비밀번호를 입력해 주세요'); return false; }
        if (!pwOk(input.value)) {
            setError(input, '비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다'); return false;
        }
        clearError(input); return true;
    }

    function checkMatch(pw, pwc) {
        if (!pwc.value || pwc.value !== pw.value) {
            setError(pwc, '비밀번호가 일치하지 않습니다'); return false;
        }
        clearError(pwc); return true;
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
        /* 실제 인증은 백엔드 몫 — 데모에서는 입력 검사만 하고 최초 1회 흐름(PIN 설정)으로 넘긴다 */
        var ok = checkRequired($('loginId'), '아이디를 입력해 주세요');
        ok = checkRequired($('password'), '비밀번호를 입력해 주세요') && ok;
        if (ok) location.href = '/onboarding/pin';
    };

    window.kdSubmitSignup = function () {
        clearAllErrors();
        var ok = checkRequired($('userName'), '이름을 입력해 주세요');
        ok = checkRequired($('loginId'), '아이디를 입력해 주세요') && ok;
        ok = checkPassword($('password')) && ok;
        ok = checkMatch($('password'), $('passwordCheck')) && ok;
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
        var ok = checkPassword($('newPassword'));
        ok = checkMatch($('newPassword'), $('newPasswordCheck')) && ok;
        if (ok) location.href = '/find-pw/done';
    };

    /* ---------- 그림 드래그 차단 (전 화면 공통) ----------
       kkeudeok.css 의 -webkit-user-drag 를 무시하는 브라우저까지 덮는다.
       화면마다 draggable="false" 를 다는 대신 여기 한 곳에서 막으므로
       앞으로 추가되는 이미지에도 자동으로 적용된다 */
    document.addEventListener('dragstart', function (e) {
        if (e.target && e.target.tagName === 'IMG') e.preventDefault();
    });

    /* ---------- 온보딩 0: 보호자 PIN ---------- */
    var PIN_RE = /^\d{4}$/;

    /* 입력한 자릿수만큼 동그라미를 켠다. 글꼴이 그리는 '•' 는 세로 위치를 CSS 로
       못 맞춰서 투명 처리하고 점을 직접 그린다(onboarding.css .onb-pin-dots) */
    function renderPinDots(input) {
        var box = input.parentNode.querySelector('.onb-pin-dots');
        if (!box) return;
        var n = input.value.length;
        [].forEach.call(box.children, function (dot, i) {
            dot.classList.toggle('is-on', i < n);
        });
    }

    document.addEventListener('input', function (e) {
        if (e.target.classList && e.target.classList.contains('onb-pin')) renderPinDots(e.target);
    });
    document.querySelectorAll('.onb-pin').forEach(renderPinDots);

    function checkPin(input) {
        if (!input.value) { setError(input, 'PIN 을 입력해 주세요'); return false; }
        if (!PIN_RE.test(input.value)) { setError(input, '숫자 4자리로 입력해 주세요'); return false; }
        clearError(input); return true;
    }

    window.kdSubmitOnbPin = function () {
        clearAllErrors();
        var ok = checkPin($('pin'));
        var pinCheck = $('pinCheck');
        if (!pinCheck.value || pinCheck.value !== $('pin').value) {
            setError(pinCheck, 'PIN 이 일치하지 않습니다'); ok = false;
        } else {
            clearError(pinCheck);
        }
        if (ok) location.href = '/onboarding/start';
    };

    /* ---------- 온보딩 1: 아이 정보 ---------- */
    /* 드롭다운이 여러 개인 블록은 오류 문구 자리가 한 줄뿐이라, 비어 있는 첫 칸에만 표시한다 */
    function checkGroup(inputs, msg) {
        var empty = inputs.filter(function (el) { return !el.value; });
        if (empty.length) { setError(empty[0], msg); return false; }
        inputs.forEach(function (el) { clearError(el); });
        return true;
    }

    /* 온보딩 입력값 임시 보관 — 화면 이동이 POST 없이 location.href 라 값이 화면을 떠나면 사라진다.
       백엔드(세션·DB)가 붙기 전까지 완료 화면에 보여줄 값만 탭 단위로 들고 있는다 */
    var ONB_KEY = 'kdOnb';

    function readOnb() {
        try { return JSON.parse(sessionStorage.getItem(ONB_KEY)) || {}; } catch (e) { return {}; }
    }

    function saveOnb(obj) {
        var cur = readOnb();
        Object.keys(obj).forEach(function (k) { cur[k] = obj[k]; });
        /* 저장이 막힌 환경(시크릿 모드 등)에서는 완료 화면이 예시 값으로 뜨면 된다 */
        try { sessionStorage.setItem(ONB_KEY, JSON.stringify(cur)); } catch (e) { }
    }

    window.kdSubmitOnbProfile = function () {
        clearAllErrors();
        var ok = checkRequired($('childName'), '아이 이름을 입력해 주세요');
        ok = checkGroup([$('birthYear'), $('birthMonth'), $('birthDay')], '생년월일을 모두 선택해 주세요') && ok;
        ok = checkGroup([$('disabilityType'), $('disabilityLevel')], '장애 유형과 정도를 선택해 주세요') && ok;
        if (!ok) return;
        saveOnb({
            name: $('childName').value.trim(),
            birthY: +$('birthYear').value,
            birthM: +$('birthMonth').value,
            birthD: +$('birthDay').value
        });
        location.href = '/onboarding/character';
    };

    /* 캐릭터 선택 후 다음 화면 = 발달 체크리스트 */
    var ONB_AFTER_CHARACTER = '/onboarding/checklist';

    /* ---------- 온보딩 2: 캐릭터 선택 ---------- */
    window.kdSubmitOnbCharacter = function () {
        clearAllErrors();
        var picked = document.querySelector('input[name="character"]:checked');
        if (!picked) {
            setError($('charHaru'), '함께할 친구를 골라 주세요');
            return;
        }
        /* 애칭은 선택 항목(Figma 에 필수 표시 없음) */
        saveOnb({
            charKey: picked.value,
            charName: picked.dataset.nickname,
            nickname: $('characterName') ? $('characterName').value.trim() : ''
        });
        location.href = ONB_AFTER_CHARACTER;
    };

    /* ---------- 온보딩 3: 발달 체크리스트 (2장으로 나뉜다) ----------
       화면에 실제로 있는 문항만 대상으로 삼으므로 1장·2장 모두 이 코드 하나로 돈다 */
    function checkNames() {
        var seen = [];
        document.querySelectorAll('.onb-q input[type="radio"]').forEach(function (el) {
            if (seen.indexOf(el.name) === -1) seen.push(el.name);
        });
        return seen;
    }

    function checkAnswered(names) {
        return names.filter(function (n) {
            return document.querySelector('input[name="' + n + '"]:checked');
        }).length;
    }

    function renderCheckCount() {
        var el = $('checkCount');
        if (!el) return;
        var names = checkNames();
        el.textContent = checkAnswered(names) + '/' + names.length + ' 완료';
    }

    function markMissing(row) {
        if (row.querySelector('.miss')) return;
        row.classList.add('is-missing');
        var p = document.createElement('span');
        p.className = 'miss';
        p.textContent = '답변을 선택해 주세요';
        row.appendChild(p);
    }

    function clearMissing(row) {
        row.classList.remove('is-missing');
        var p = row.querySelector('.miss');
        if (p) p.remove();
    }

    /* 안 고른 문항을 전부 표시하고 첫 번째로 데려간다 */
    function checkSubmit(nextUrl) {
        var names = checkNames();
        var missing = names.filter(function (n) {
            return !document.querySelector('input[name="' + n + '"]:checked');
        });
        if (!missing.length) { location.href = nextUrl; return; }
        missing.forEach(function (n) {
            markMissing(document.querySelector('input[name="' + n + '"]').closest('.onb-q'));
        });
        if ($('checkCount')) $('checkCount').classList.add('is-missing');
        document.querySelector('input[name="' + missing[0] + '"]').closest('.onb-q')
            .scrollIntoView({block: 'center'});
    }

    window.kdSubmitOnbChecklist = function () { checkSubmit('/onboarding/checklist-2'); };
    window.kdSubmitOnbChecklist2 = function () { checkSubmit('/onboarding/face-guide'); };

    document.addEventListener('change', function (e) {
        if (!e.target.closest || !e.target.closest('.onb-q')) return;
        clearMissing(e.target.closest('.onb-q'));
        renderCheckCount();
        if (!document.querySelector('.onb-q.is-missing') && $('checkCount')) {
            $('checkCount').classList.remove('is-missing');
        }
    });

    renderCheckCount();

    /* ---------- 온보딩 4: 카메라 권한 ----------
       표정 등록은 카메라가 있어야 성립하는 단계라, 거부하면 다음으로 보내지 않는다.
       여기서는 권한만 확인하고 스트림은 곧바로 끊는다(실제 촬영은 아직 미구현) */
    var camFailed = false;

    function showCamDenied(title, desc) {
        var notes = $('camNotes'), denied = $('camDenied');
        if (!denied) return;
        camFailed = true;
        if (notes) notes.hidden = true;
        denied.hidden = false;
        if (title) $('camDeniedTitle').textContent = title;
        if (desc) $('camDeniedDesc').textContent = desc;
        var btn = document.querySelector('.onb-actions .kd-btn-primary');
        if (btn) { btn.disabled = false; btn.textContent = '다시 시도'; }
    }

    window.kdOnbAskCamera = function () {
        var btn = document.querySelector('.onb-actions .kd-btn-primary');
        /* 한 번 실패한 뒤 [다시 시도]를 또 누르면 그냥 보낸다 — 카메라 없는 PC에서
           화면 흐름을 확인할 수 없어서다. 실제 촬영이 붙으면 이 줄을 지울 것 */
        if (camFailed) { location.href = '/onboarding/face-capture'; return; }
        var md = navigator.mediaDevices;
        if (!md || !md.getUserMedia) {
            showCamDenied('카메라를 쓸 수 없어요',
                '이 브라우저에서는 카메라를 열 수 없어요. 크롬이나 엣지 최신 버전에서 다시 열어 주세요.');
            return;
        }
        if (btn) { btn.disabled = true; btn.textContent = '카메라 확인 중…'; }
        md.getUserMedia({ video: true }).then(function (stream) {
            /* 권한만 확인하면 되므로 바로 끊는다 — 안 끄면 카메라 표시등이 계속 켜져 있다 */
            stream.getTracks().forEach(function (t) { t.stop(); });
            location.href = '/onboarding/face-capture';
        }).catch(function (err) {
            if (err && err.name === 'NotFoundError') {
                showCamDenied('카메라를 찾을 수 없어요',
                    '이 기기에 연결된 카메라가 없어요. 카메라를 연결한 뒤 다시 시도해 주세요.');
            } else {
                showCamDenied();
            }
        });
    };

    /* ---------- 온보딩 5: 표정 등록 ----------
       기쁨부터 순서대로 한 칸씩 넘어가고 진행률이 차오른다.
       실제 촬영·판정은 기기/백엔드 몫이라 여기서는 흐름만 굴린다 */
    var faceIdx = 0;

    function faceRows() { return document.querySelectorAll('.onb-emotion'); }

    function renderFaces() {
        var rows = faceRows();
        if (!rows.length) return;
        rows.forEach(function (row, i) {
            var state = i < faceIdx ? 'is-done' : (i === faceIdx ? 'is-current' : 'is-wait');
            row.className = 'onb-emotion ' + state;
            row.querySelector('.st').textContent =
                i < faceIdx ? '완료' : (i === faceIdx ? '현재' : '대기');
        });
        document.querySelector('.onb-cam .fill').style.width = (faceIdx / rows.length * 100) + '%';
        document.querySelector('.onb-cam .cnt').textContent = faceIdx + ' / ' + rows.length + ' 완료';
        /* 마지막 표정 차례에는 다음 단계로 넘어간다는 걸 버튼에 알린다 */
        var next = document.querySelector('.onb-actions .kd-btn-primary');
        if (next) next.textContent = faceIdx === rows.length - 1 ? '등록 마치기' : '다음 표정';
        /* 첫 표정에서는 되돌릴 게 없으니 앞 화면으로 나가는 버튼이 된다 (비활성으로 두지 않는다) */
        var prev = document.querySelector('.onb-actions .kd-btn-outline');
        if (prev) {
            prev.textContent = faceIdx === 0 ? '뒤로 가기' : '다시 찍기';
            prev.classList.remove('is-disabled');
        }
    }

    window.kdOnbFaceNext = function () {
        faceIdx += 1;
        if (faceIdx >= faceRows().length) { location.href = '/onboarding/roadmap'; return; }
        renderFaces();
    };

    window.kdOnbFaceRetry = function () {
        if (faceIdx === 0) { location.href = '/onboarding/face-guide'; return; }
        faceIdx -= 1;
        renderFaces();
    };

    renderFaces();

    /* 카드를 고르면 오류를 지우고, 애칭칸 안내 문구를 고른 친구 이름으로 바꾼다
       (라디오는 kd-input 이 아니라 실시간 검사 대상이 아니다) */
    document.addEventListener('change', function (e) {
        if (e.target.name !== 'character') return;
        clearError($('charHaru'));
        var name = $('characterName');
        if (name) name.placeholder = e.target.dataset.nickname;
    });

    /* ---------- 온보딩 완료: 앞 단계 입력값 채우기 ----------
       JSP 에 적힌 값은 Figma 예시다. 보관된 값이 있을 때만 덮어쓴다
       — 주소로 바로 들어와도 화면이 비지 않는다 */
    function ageOf(y, m, d) {
        var t = new Date(), age = t.getFullYear() - y;
        /* 만 나이 — 올해 생일이 아직 안 지났으면 한 살 뺀다 */
        if (t.getMonth() + 1 < m || (t.getMonth() + 1 === m && t.getDate() < d)) age -= 1;
        return age;
    }

    function renderOnbDone() {
        if (!$('doneName')) return;
        var v = readOnb();
        if (v.name) {
            $('doneName').textContent =
                v.name + (v.birthY ? ' (' + ageOf(v.birthY, v.birthM, v.birthD) + '세)' : '');
            $('doneSub').textContent = v.name + '에게 맞는 학습을 준비했어요';
        }
        if (v.charName) {
            /* 애칭을 적었으면 같이 보여 준다 — 어느 친구인지도 남게 괄호로 */
            $('doneFriend').textContent = v.nickname ? v.charName + ' (' + v.nickname + ')' : v.charName;
            $('doneChar').src = '/img/char-' + v.charKey + '.png?v=2';
        }
    }

    renderOnbDone();

    /* ---------- 실시간 오류 해제 — 오류가 떠 있는 필드만 입력 시 재검증 ---------- */
    function notBlank(el) { return !!el.value.trim(); }
    function hasValue(el) { return !!el.value; }
    function pwLive(el) { return pwOk(el.value); }

    var LIVE_CHECKS = {
        userName: notBlank,
        loginId: notBlank,
        email: function (el) { return EMAIL_RE.test(el.value.trim()); },
        password: pwLive,
        newPassword: pwLive,
        passwordCheck: function (el) { return !!el.value && el.value === $('password').value; },
        newPasswordCheck: function (el) { return !!el.value && el.value === $('newPassword').value; },
        authCode: function (el) { return el.value.trim() === DEMO_CODE; },
        pin: function (el) { return PIN_RE.test(el.value); },
        pinCheck: function (el) { return !!el.value && el.value === $('pin').value; },
        childName: notBlank,
        birthYear: hasValue,
        birthMonth: hasValue,
        birthDay: hasValue,
        disabilityType: hasValue,
        disabilityLevel: hasValue
    };

    /* 비밀번호를 고치면 확인 필드의 일치 여부도 달라진다 */
    var PAIRS = { password: 'passwordCheck', newPassword: 'newPasswordCheck', pin: 'pinCheck' };

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
