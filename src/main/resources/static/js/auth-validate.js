
(function () {
    'use strict';

    function $(id) { return document.getElementById(id); }

    function postForm(url, params) {
        var body = Object.keys(params).map(function (k) {
            return encodeURIComponent(k) + '=' + encodeURIComponent(params[k]);
        }).join('&');

        return fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: body
        }).then(function (res) { return res.json(); });
    }

    function showServerError(data, fallbackId) {
        var el = (data && data.field) ? $(data.field) : null;
        setError(el || $(fallbackId), data.msg);
    }

    function onFail(input) {
        return function (err) {
            console.error(err);
            setError(input, '통신에 실패했습니다. 잠시 후 다시 시도해 주세요');
        };
    }

    function fieldOf(target) {
        if (!target) return null;

        return target.closest('.kd-field');
    }

    function setError(input, msg) {
        var field = fieldOf(input);
        if (!field) return;
        clearError(input);
        input.classList.add('kd-input--error');

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
    var NAME_RE = /^[가-힣a-zA-Z]{2,20}$/;   
    var ID_RE = /^[a-z0-9]{4,12}$/;          
    var CODE_RE = /^\d{6}$/;

    function checkRequired(input, msg) {
        if (!input.value.trim()) { setError(input, msg); return false; }
        clearError(input); return true;
    }

    function checkEmail(input) {
        if (!input.value.trim()) { setError(input, '이메일을 입력해 주세요'); return false; }
        if (!EMAIL_RE.test(input.value.trim())) { setError(input, '이메일 형식이 올바르지 않습니다'); return false; }
        clearError(input); return true;
    }

    function checkUserName(input) {
        var v = input.value.trim();
        if (!v) { setError(input, '이름을 입력해 주세요'); return false; }
        if (!NAME_RE.test(v)) { setError(input, '이름은 한글 또는 영문 2~20자로 입력해 주세요'); return false; }
        clearError(input); return true;
    }

    function checkChildName(input) {
        var v = input.value.trim();
        if (!v) { setError(input, '아이 이름을 입력해 주세요'); return false; }
        if (!NAME_RE.test(v)) { setError(input, '이름은 한글 또는 영문 2~20자로 입력해 주세요'); return false; }
        clearError(input); return true;
    }

    function checkLoginId(input) {
        var v = input.value.trim();
        if (!v) { setError(input, '아이디를 입력해 주세요'); return false; }
        if (!ID_RE.test(v)) { setError(input, '아이디는 영문 소문자와 숫자 4~12자로 입력해 주세요'); return false; }
        clearError(input); return true;
    }

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

        clearError(code); return true;
    }

    var timerHandle = null;
    var codeExpired = false;    

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

    window.kdSendCode = function (kind) {
        clearAllErrors();
        var ok = true;
        if (kind === 'findId') ok = checkUserName($('userName')) && ok;
        ok = checkEmail($('email')) && ok;
        if (!ok) return;

        postForm('/sendAuthCodeProc', { email: $('email').value.trim(), kind: kind })
            .then(function (data) {
                if (data.result !== 1) { showServerError(data, 'email'); return; }

                codeExpired = false;
                var field = $('authCodeField');
                field.classList.remove('is-hidden');
                startTimer(field);
                setInfo($('email'), data.msg);
                $('authCode').focus();
            })
            .catch(onFail($('email')));
    };

    window.kdSubmitLogin = function () {
        clearAllErrors();
        var ok = checkRequired($('loginId'), '아이디를 입력해 주세요');
        ok = checkRequired($('password'), '비밀번호를 입력해 주세요') && ok;
        if (!ok) return;

        postForm('/loginProc', {
            loginId: $('loginId').value.trim(),
            password: $('password').value
        }).then(function (data) {

            if (data.result === 1) location.href = data.next || '/dashboard';
            else showServerError(data, 'password');
        }).catch(onFail($('password')));
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

        postForm('/signupProc', {
            userName: $('userName').value.trim(),
            loginId: $('loginId').value.trim(),
            password: $('password').value,
            email: $('email').value.trim(),
            authCode: $('authCode').value.trim()
        }).then(function (data) {
            if (data.result !== 1) {
                showServerError(data, 'authCode');
                return;
            }

            saveOnb({ guardianName: $('userName').value.trim(), email: $('email').value.trim() });
            location.href = '/signup/done';
        }).catch(onFail($('authCode')));
    };

    window.kdSubmitFindId = function () {
        clearAllErrors();
        var ok = checkUserName($('userName'));
        var emailOk = checkEmail($('email'));
        ok = checkCode(emailOk) && emailOk && ok;
        if (!ok) return;

        postForm('/findIdProc', {
            userName: $('userName').value.trim(),
            email: $('email').value.trim(),
            authCode: $('authCode').value.trim()
        }).then(function (data) {

            if (data.result === 1) location.href = '/find-id/result';
            else showServerError(data, 'authCode');
        }).catch(onFail($('authCode')));
    };

    window.kdSubmitFindPwEmail = function () {
        clearAllErrors();
        var emailOk = checkEmail($('email'));
        var ok = checkCode(emailOk) && emailOk;
        if (!ok) return;

        postForm('/findPwProc', {
            email: $('email').value.trim(),
            authCode: $('authCode').value.trim()
        }).then(function (data) {

            if (data.result === 1) location.href = '/find-pw/new';
            else showServerError(data, 'authCode');
        }).catch(onFail($('authCode')));
    };

    window.kdSubmitFindPwNew = function () {
        clearAllErrors();
        var ok = checkPassword($('newPassword'));
        ok = checkMatch($('newPassword'), $('newPasswordCheck')) && ok;
        if (!ok) return;

        postForm('/newPasswordProc', { newPassword: $('newPassword').value })
            .then(function (data) {
                if (data.result === 1) location.href = '/find-pw/done';
                else showServerError(data, 'newPassword');
            })
            .catch(onFail($('newPassword')));
    };

    document.addEventListener('dragstart', function (e) {
        if (e.target && e.target.tagName === 'IMG') e.preventDefault();
    });

    var PIN_RE = /^\d{4}$/;

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
        if (!ok) return;

        postForm('/parentPinProc', { pin: $('pin').value })
            .then(function (data) {
                if (data.result === 1) location.href = data.next || '/onboarding/start';
                else showServerError(data, 'pin');
            })
            .catch(onFail($('pin')));
    };

    var lockHandle = null;

    window.kdPinLock = function (left) {
        var pin = $('gatePin');
        if (!pin) return;
        var btn = document.querySelector('.mp-gate button[type=submit]');

        if (lockHandle) { clearTimeout(lockHandle); lockHandle = null; }

        if (left <= 0) {
            pin.disabled = false;
            if (btn) btn.disabled = false;
            clearError(pin);
            return;
        }

        pin.disabled = true;
        if (btn) btn.disabled = true;
        pin.value = '';
        renderPinDots(pin);

        (function tick() {
            if (left <= 0) { kdPinLock(0); return; }
            setError(pin, Math.floor(left / 60) + '분 ' + String(left % 60).padStart(2, '0') +
                '초 동안 잠겼어요. PIN을 잊었다면 아래에서 다시 설정하세요');
            left -= 1;
            lockHandle = setTimeout(tick, 1000);
        })();
    };

    window.kdSubmitGate = function () {
        clearAllErrors();
        if (!checkPin($('gatePin'))) return;

        postForm('/verifyParentPinProc', { pin: $('gatePin').value })
            .then(function (data) {
                if (data.result === 1) { location.href = data.next || '/mypage/account'; return; }
                if (data.lockLeft > 0) { kdPinLock(data.lockLeft); return; }

                if (data.next) { location.href = data.next; return; }
                showServerError(data, 'gatePin');
                $('gatePin').select();
            })
            .catch(onFail($('gatePin')));
    };

    var pinResetBusy = false;

    window.kdSubmitPinReset = function () {

        if (pinResetBusy) return;
        clearAllErrors();
        if (!checkCode(true)) return;

        pinResetBusy = true;
        postForm('/pinResetVerifyProc', { authCode: $('authCode').value.trim() })
            .then(function (data) {
                if (data.result !== 1) { pinResetBusy = false; showServerError(data, 'authCode'); return; }

                setInfo($('authCode'), data.msg);
                setTimeout(function () {
                    location.href = data.next || '/mypage/pin-reset/new';
                }, 600);
            })
            .catch(function (err) { pinResetBusy = false; onFail($('authCode'))(err); });
    };

    window.kdSubmitNewPin = function () {
        clearAllErrors();
        var ok = checkPin($('newPin'));
        var check = $('newPinCheck');
        if (!check.value || check.value !== $('newPin').value) {
            setError(check, 'PIN 이 일치하지 않습니다'); ok = false;
        } else {
            clearError(check);
        }
        if (!ok) return;

        postForm('/newParentPinProc', { newPin: $('newPin').value })
            .then(function (data) {
                if (data.result === 1) location.href = data.next || '/mypage/account';
                else showServerError(data, 'newPin');
            })
            .catch(onFail($('newPin')));
    };

    function checkGroup(inputs, msg) {
        var empty = inputs.filter(function (el) { return !el.value; });
        if (empty.length) { setError(empty[0], msg); return false; }
        inputs.forEach(function (el) { clearError(el); });
        return true;
    }

    var ONB_KEY = 'kdOnb';

    function readOnb() {
        try { return JSON.parse(sessionStorage.getItem(ONB_KEY)) || {}; } catch (e) { return {}; }
    }

    function saveOnb(obj) {
        var cur = readOnb();
        Object.keys(obj).forEach(function (k) { cur[k] = obj[k]; });

        try { sessionStorage.setItem(ONB_KEY, JSON.stringify(cur)); } catch (e) { }
    }

    var DIS_LEVELS = {
        '자폐 장애': ['중증'],
        '지적 장애': ['중증'],
        '발달 장애': ['경증', '중증']
    };

    var disPlaceholder = null;

    function syncDisLevels() {
        var type = $('disabilityType'), level = $('disabilityLevel');
        if (!type || !level) return;
        var levels = DIS_LEVELS[type.value] || [];
        var keep = level.value;

        var ph = disPlaceholder !== null && levels.length !== 1 ? disPlaceholder : null;
        level.innerHTML = '';
        if (ph !== null) level.add(new Option(ph, ''));
        levels.forEach(function (v) { level.add(new Option(v, v)); });

        if (levels.indexOf(keep) >= 0) level.value = keep;
        else if (ph === null) level.selectedIndex = 0;
        else level.value = '';

        if (level.__kdSync) level.__kdSync();
    }

    (function initDisLevels() {
        var type = $('disabilityType'), level = $('disabilityLevel');
        if (!type || !level) return;
        disPlaceholder = level.options.length && !level.options[0].value
            ? level.options[0].textContent : null;
        type.addEventListener('change', function () {
            syncDisLevels();
            if (level.value) clearError(level);
        });
        syncDisLevels();
    })();

    window.kdSubmitOnbProfile = function () {
        clearAllErrors();
        var ok = checkChildName($('childName'));
        ok = checkGroup([$('birthYear'), $('birthMonth'), $('birthDay')], '생년월일을 모두 선택해 주세요') && ok;

        if (!$('disabilityType').value) { setError($('disabilityType'), '장애 유형을 선택해 주세요'); ok = false; }
        else ok = checkGroup([$('disabilityLevel')], '장애 정도를 선택해 주세요') && ok;
        if (!ok) return;
        var g = document.querySelector('input[name="gender"]:checked');
        saveOnb({
            name: $('childName').value.trim(),
            birthY: +$('birthYear').value,
            birthM: +$('birthMonth').value,
            birthD: +$('birthDay').value,

            gender: g ? g.value : '',
            disType: $('disabilityType').value,
            disLevel: $('disabilityLevel').value
        });
        location.href = '/onboarding/character';
    };

    var ONB_AFTER_CHARACTER = '/onboarding/checklist';

    window.kdSubmitOnbCharacter = function () {
        clearAllErrors();
        var picked = document.querySelector('input[name="character"]:checked');
        if (!picked) {

            setError($('charTori'), '함께할 친구를 골라 주세요');
            return;
        }

        saveOnb({
            charKey: picked.value,
            charName: picked.dataset.nickname,
            nickname: $('characterName') ? $('characterName').value.trim() : ''
        });
        location.href = ONB_AFTER_CHARACTER;
    };

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

        if (camFailed) { location.href = '/onboarding/face-capture'; return; }
        var md = navigator.mediaDevices;
        if (!md || !md.getUserMedia) {
            showCamDenied('카메라를 쓸 수 없어요',
                '이 브라우저에서는 카메라를 열 수 없어요. 크롬이나 엣지 최신 버전에서 다시 열어 주세요.');
            return;
        }
        if (btn) { btn.disabled = true; btn.textContent = '카메라 확인 중…'; }
        md.getUserMedia({ video: true }).then(function (stream) {

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

        var next = document.querySelector('.onb-actions .kd-btn-primary');
        if (next) next.textContent = faceIdx === rows.length - 1 ? '등록 마치기' : '다음 표정';

        var prev = document.querySelector('.onb-actions .kd-btn-outline');
        if (prev) {
            prev.textContent = faceIdx === 0 ? '뒤로 가기' : '다시 찍기';
            prev.classList.remove('is-disabled');
        }
    }

    window.kdOnbFaceNext = function () {
        faceIdx += 1;

        if (faceIdx >= faceRows().length) { location.href = '/onboarding/done'; return; }
        renderFaces();
    };

    window.kdOnbFaceRetry = function () {
        if (faceIdx === 0) { location.href = '/onboarding/face-guide'; return; }
        faceIdx -= 1;
        renderFaces();
    };

    renderFaces();

    document.addEventListener('change', function (e) {
        if (e.target.name !== 'character') return;
        clearError($('charTori'));
        var name = $('characterName');
        if (name) name.placeholder = e.target.dataset.nickname;
    });

    var ageOf = window.kdName.age;
    var callName = window.kdName.call;

    function charImg(key, pose) {
        return '/img/char-' + (key || 'tori') + '-' + (pose || 'neutral') + '.png';
    }

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

    document.documentElement.classList.add('kd-char-ready');

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

            $('doneFriend').textContent = v.nickname ? v.charName + ' (' + v.nickname + ')' : v.charName;
            $('doneChar').src = charImg(v.charKey);
        }
    }

    renderOnbDone();

    function renderAppOnb() {
        var v = readOnb();

        if (v.guardianName) {
            document.querySelectorAll('[data-kd="guardianName"]').forEach(function (el) { el.value = v.guardianName; });
        }
        if (v.email) {
            var em = document.getElementById('mpEmail');
            if (em) em.value = v.email;
        }

        if (!v.name) return;

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

            if (v.disType && $('disabilityType')) {
                $('disabilityType').value = [].concat(v.disType)[0] || '';
                syncDisLevels();
            }
            if (v.disLevel && $('disabilityLevel')) {
                $('disabilityLevel').value = v.disLevel;

                if (!$('disabilityLevel').value) syncDisLevels();
            }
        }

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

    document.documentElement.classList.add('kd-named');

    function notBlank(el) { return !!el.value.trim(); }
    function hasValue(el) { return !!el.value; }
    function pwLive(el) { return pwOk(el.value); }

    var LIVE_CHECKS = {
        userName: function (el) { return NAME_RE.test(el.value.trim()); },

        loginId: function (el) { return $('passwordCheck') ? ID_RE.test(el.value.trim()) : notBlank(el); },
        email: function (el) { return EMAIL_RE.test(el.value.trim()); },
        password: pwLive,
        newPassword: pwLive,
        passwordCheck: function (el) { return !!el.value && el.value === $('password').value; },
        newPasswordCheck: function (el) { return !!el.value && el.value === $('newPassword').value; },

        authCode: function (el) { return !codeExpired && CODE_RE.test(el.value.trim()); },
        pin: function (el) { return PIN_RE.test(el.value); },
        pinCheck: function (el) { return !!el.value && el.value === $('pin').value; },
        childName: function (el) { return NAME_RE.test(el.value.trim()); },
        birthYear: hasValue,
        birthMonth: hasValue,
        birthDay: hasValue,
        disabilityType: hasValue,
        disabilityLevel: hasValue
    };

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

    (function initLoginIdDupCheck() {
        var id = $('loginId');
        if (!id || !$('passwordCheck')) return;

        var lastAsked = '';

        id.addEventListener('blur', function () {
            var v = id.value.trim();

            if (!v || !ID_RE.test(v) || v === lastAsked) return;
            lastAsked = v;

            postForm('/checkLoginIdProc', { loginId: v })
                .then(function (data) {

                    if (id.value.trim() !== v) return;
                    if (data.result === 1) setInfo(id, data.msg);
                    else showServerError(data, 'loginId');
                })
                .catch(function () {  });
        });

        id.addEventListener('input', function () {
            if (id.value.trim() !== lastAsked) {
                lastAsked = '';
                var f = fieldOf(id);
                if (f) f.querySelectorAll('.kd-info').forEach(function (e) { e.remove(); });
            }
        });
    })();

    ['copy', 'cut', 'paste'].forEach(function (evt) {
        document.addEventListener(evt, function (e) {
            if (e.target.hasAttribute && e.target.hasAttribute('data-nocopy')) e.preventDefault();
        });
    });

    var REQUIRED_TERMS = ['agreeTerms', 'agreePrivacy', 'agreeSensitive'];

    function termsBoxes() {
        return Array.prototype.slice.call(document.querySelectorAll('.terms-item .kd-check'));
    }

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

            termsBoxes().forEach(function (c) { c.checked = el.checked; });
        } else if (el.classList && el.classList.contains('kd-check') && el.closest('.terms-item')) {

            syncAgreeAll();
        } else {
            return;
        }
        clearTermsError();
    });

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
