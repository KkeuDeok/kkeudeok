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
        /* 실제 인증은 백엔드 몫 — 데모에서는 입력 검사만 하고 최초 1회 흐름(PIN 설정)으로 넘긴다 */
        var ok = checkRequired($('loginId'), '아이디를 입력해 주세요');
        ok = checkRequired($('password'), '비밀번호를 입력해 주세요') && ok;
        if (ok) location.href = '/onboarding/pin';
    };

    window.kdSubmitSignup = function () {
        clearAllErrors();
        var ok = checkUserName($('userName'));
        ok = checkLoginId($('loginId')) && ok;
        ok = checkPassword($('password')) && ok;
        ok = checkMatch($('password'), $('passwordCheck')) && ok;
        var emailOk = checkEmail($('email'));
        ok = checkCode(emailOk) && emailOk && ok;
        if (!ok) return;
        /* 가입 폼에 적은 보호자 이름·이메일을 마이페이지에서 그대로 보여 준다.
           예전에는 버려져서 아이 이름과 같은 예시값('김지우')이 남아 두 탭이 같은 사람처럼 보였다 */
        saveOnb({ guardianName: $('userName').value.trim(), email: $('email').value.trim() });
        location.href = '/signup/done';
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
        var g = document.querySelector('input[name="gender"]:checked');
        saveOnb({
            name: $('childName').value.trim(),
            birthY: +$('birthYear').value,
            birthM: +$('birthMonth').value,
            birthD: +$('birthDay').value,
            /* 마이페이지 아동 프로필에서 그대로 다시 보여 줘야 해서 같이 담는다 */
            gender: g ? g.value : '',
            disType: $('disabilityType').value,
            disLevel: $('disabilityLevel').value
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
            /* ⚠ 여기가 charHaru 였다 — 하루는 예전에 뺀 캐릭터라 그 id 가 없어서
               $() 가 null 을 주고 setError 안에서 터졌다. 아무것도 안 고르고 [다음]을
               누르면 오류 문구가 안 뜨고 조용히 죽던 원인. */
            setError($('charTori'), '함께할 친구를 골라 주세요');
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
        clearError($('charTori'));
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

    /* 부제는 아이를 부르는 말투로 — '박성현에게' 보다 '성현이에게'.
       성을 떼고, 받침이 있으면 '이' 를 붙인다(성현이 / 지우).
       표의 '아이 이름' 칸은 기록이라 적어 준 이름 그대로 둔다 */
    var SURNAME2 = ['남궁', '황보', '제갈', '사공', '선우', '서문', '독고', '동방'];

    function givenName(full) {
        var n = (full || '').trim();
        /* 두 글자 이하는 이미 이름만 적은 것으로 본다 (입력칸 안내도 '아이가 부르는 이름') */
        if (n.length < 3) return n;
        return SURNAME2.indexOf(n.slice(0, 2)) !== -1 ? n.slice(2) : n.slice(1);
    }

    function callName(full) {
        var n = givenName(full);
        /* 한글 음절은 (코드 - 가) % 28 이 0 이 아니면 받침이 있다. 한글이 아니면 안 붙인다 */
        var i = n.charCodeAt(n.length - 1) - 0xAC00;
        return n + (i >= 0 && i < 11172 && i % 28 !== 0 ? '이' : '');
    }

    /* 캐릭터 키 + 포즈 -> 이미지 경로. 6명 × 12포즈 = 72장이 전부 이 한 규칙을 따른다
       (neutral surprise angry sad happy sleepy hurt wave celebrate sorry comfort proud).
       파일명이 새로 생긴 것들이라 ?v 는 붙이지 않는다 — 이름이 바뀐 것 자체가 캐시 무효화다. */
    function charImg(key, pose) {
        return '/img/char-' + (key || 'tori') + '-' + (pose || 'neutral') + '.png';
    }

    /* 아이가 고른 캐릭터를 화면에 반영한다.
       - 그림: data-kd-char="포즈" 가 붙은 <img> 의 src 를 갈아끼운다
       - 이름: data-kd="charName" 안의 글자만 바꾼다

       ⚠ 본문 전체 문자열 치환은 절대 금지 — '스토리' 안에 '토리'가 들어 있어서
         'AI 스토리 학습' 이 'AI 스라라 학습' 이 된다(학습 홈·리포트에 5군데).
       ⚠ 6명 이름이 전부 받침이 없어 조사(가/는/를/야)는 그대로 맞는다.
         받침 있는 이름이 생기면 callName() 의 판정 규칙을 가져다 쓸 것.
       ⚠ 마이페이지·온보딩의 '고르는 갤러리'에는 data-kd-char 를 붙이면 안 된다 —
         6명을 다 보여주는 자리라 전부 한 캐릭터로 바뀌어 버린다.
       renderAppOnb() 안에 두지 않은 이유: 그쪽은 아이 이름이 없으면 조기 반환한다.
       캐릭터 반영이 이름 입력 여부에 묶이면 안 된다. */
    /* 지금 자산이 있는 6명. 여기 없는 키(예전에 뺀 '하루'·'보리')가 세션에 남아 있으면
       char-haru-comfort.png 같은 없는 파일을 가리켜 화면의 그림이 전부 깨진다 — 실제로 겪었다.
       모르는 키면 그림도 이름도 건드리지 않고 JSP 기본값(토리)을 그대로 둔다. */
    var CHAR_KEYS = ['tori', 'koko', 'lala', 'bada', 'bomi', 'rubi'];

    function renderChar() {
        var v = readOnb();
        if (v.charKey && CHAR_KEYS.indexOf(v.charKey) === -1) return;
        if (v.charKey) {
            document.querySelectorAll('[data-kd-char]').forEach(function (el) {
                var pose = el.getAttribute('data-kd-char');
                el.onerror = function () { this.onerror = null; this.src = charImg('tori', pose); };
                el.src = charImg(v.charKey, pose);
            });
        }
        if (v.charName) {
            document.querySelectorAll('[data-kd="charName"]').forEach(function (el) {
                el.textContent = v.charName;
            });
        }
    }

    renderChar();
    /* 교체가 끝났으니 그림을 드러낸다 — 그 전에는 CSS 가 감춰 둔다(kd-char-ready 규칙).
       renderChar 안에서 조기 return 하는 경우에도 여기까지는 오므로 기본 그림이 보인다. */
    document.documentElement.classList.add('kd-char-ready');

    /* 마이페이지 캐릭터 관리 [저장] — 이게 없으면 골라도 저장이 안 돼
       스토리 화면에 반영되지 않는다(토스트만 뜨고 있었다). */
    window.kdSaveChar = function () {
        var picked = document.querySelector('.mp-chars input[name="character"]:checked');
        if (!picked) return;
        var card = picked.closest('label') || picked.parentElement;
        var nm = card ? card.querySelector('.nm') : null;
        saveOnb({
            charKey: picked.value,
            charName: nm ? nm.textContent.trim() : picked.value,
            nickname: $('charNick') ? $('charNick').value.trim() : ''
        });
        if (window.kdSaved) window.kdSaved('캐릭터를 저장했어요');
    };

    function renderOnbDone() {
        if (!$('doneName')) return;
        var v = readOnb();
        if (v.name) {
            $('doneName').textContent =
                v.name + (v.birthY ? ' (' + ageOf(v.birthY, v.birthM, v.birthD) + '세)' : '');
            $('doneSub').textContent = callName(v.name) + '에게 맞는 학습을 준비했어요';
        }
        if (v.charName) {
            /* 애칭을 적었으면 같이 보여 준다 — 어느 친구인지도 남게 괄호로 */
            $('doneFriend').textContent = v.nickname ? v.charName + ' (' + v.nickname + ')' : v.charName;
            $('doneChar').src = charImg(v.charKey);
        }
    }

    renderOnbDone();

    /* 온보딩에서 받은 아이 정보를 보호자 앱 전 화면에 반영한다.
       마크업에는 data-kd 훅만 두고 값은 여기서 채운다 — JSP 를 EL 로 바꾸는 건 백엔드가 붙은 뒤 일.
       kdOnb 가 비어 있으면(직접 URL 진입·시크릿 모드) 아무것도 건드리지 않아 예시값이 그대로 남는다. */
    function renderAppOnb() {
        var v = readOnb();

        /* 보호자 값은 아이 이름과 무관하게 채운다 — 가입만 하고 온보딩을 안 거친 경우도 있다 */
        if (v.guardianName) {
            document.querySelectorAll('[data-kd="guardianName"]').forEach(function (el) { el.value = v.guardianName; });
        }
        if (v.email) {
            var em = document.getElementById('mpEmail');
            if (em) em.value = v.email;
        }

        if (!v.name) return;

        var age = v.birthY ? ageOf(v.birthY, v.birthM, v.birthD) : null;

        document.querySelectorAll('[data-kd="childName"]').forEach(function (el) {
            el.textContent = v.name;
        });
        document.querySelectorAll('[data-kd="childNameAge"]').forEach(function (el) {
            el.textContent = v.name + (age === null ? '' : ' · ' + age + '세');
        });
        /* 소유격·호칭 자리는 부르는 말투로 — '박민준의 성장 리포트' 가 아니라 '민준이의 성장 리포트'.
           본명 그대로 둬야 하는 곳(사이드바·아동 프로필 이름)은 childName / childNameAge 를 쓴다 */
        document.querySelectorAll('[data-kd="childCall"]').forEach(function (el) {
            el.textContent = callName(v.name);
        });
        document.querySelectorAll('[data-kd="childAge"]').forEach(function (el) {
            if (age !== null) el.textContent = age;
        });

        /* 마이페이지 아동 프로필 폼 — 이 화면에서만. 온보딩 입력 화면과 id 가 같아서 가드가 필요하다.
           select 값은 onb-select.js 가 드롭다운을 만들기 전에 넣어야 버튼 글씨까지 따라온다
           (head.jsp 로드 순서가 auth-validate → onb-select 라 여기서 넣으면 맞다) */
        if (document.querySelector('.mp-profile')) {
            if ($('childName')) $('childName').value = v.name;
            if (v.birthY && $('birthYear')) {
                $('birthYear').value = v.birthY;
                $('birthMonth').value = v.birthM;
                $('birthDay').value = v.birthD;
            }
            if (v.gender) {
                var r = document.querySelector('input[name="gender"][value="' + v.gender + '"]');
                if (r) r.checked = true;
            }
            if (v.disType && $('disabilityType')) $('disabilityType').value = v.disType;
            if (v.disLevel && $('disabilityLevel')) $('disabilityLevel').value = v.disLevel;
        }

        /* 마이페이지 캐릭터 관리 — 온보딩에서 고른 친구를 선택 상태로.
           미리보기 갱신은 화면 자체 change 핸들러에 맡긴다(그 인라인 스크립트가 먼저 실행된다) */
        if (v.charKey && document.querySelector('.mp-chars')) {
            var pick = document.querySelector('.mp-chars input[value="' + v.charKey + '"]');
            if (pick) {
                pick.checked = true;
                pick.dispatchEvent(new Event('change'));
                if (v.nickname && $('charNick')) $('charNick').value = v.nickname;
            }
        }
    }

    renderAppOnb();
    /* 치환이 끝났으니 이름을 드러낸다 — 그 전에는 CSS 가 감춰 둔다(위 kd-named 규칙).
       kdOnb 가 비어 조기 return 한 경우에도 여기까지는 오므로 예시값이 그대로 보인다. */
    document.documentElement.classList.add('kd-named');

    /* 리포트 5탭 부제 — 관찰 데이터가 0인데 '관찰 8주차'가 남아 있으면 안 된다.
       탭마다 같은 문장이라 JSP 5개를 고치는 대신 여기서 한 번에 바꾼다. */
    if (document.documentElement.dataset.kdStage !== '2') {
        var rptSub = document.querySelector('.rpt-head p');
        if (rptSub) rptSub.textContent = '관찰 시작 전 · 첫 주 학습을 마치면 리포트가 만들어져요';
    }

    /* ---------- 실시간 오류 해제 — 오류가 떠 있는 필드만 입력 시 재검증 ---------- */
    function notBlank(el) { return !!el.value.trim(); }
    function hasValue(el) { return !!el.value; }
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
        authCode: function (el) { return !codeExpired && el.value.trim() === DEMO_CODE; },
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
