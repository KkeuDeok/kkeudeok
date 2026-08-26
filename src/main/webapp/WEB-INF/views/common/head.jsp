<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<%-- 카메라 예열 — 반드시 아래 <link> 들보다 **위**에 있어야 한다.
     스크립트는 앞선 스타일시트를 다 받을 때까지 실행되지 않아서, 이 줄이 CDN 링크 뒤로
     내려가면 폰트·부트스트랩 왕복이 끝난 뒤에야 카메라를 요청하게 된다(그래서 늦게 켜졌다).
     스트림은 window.kdCam 에 담아 두고 kd-mediapipe.js 가 받아 쓴다. --%>
<script>
    (function () {
        var p = location.pathname.replace(/\/$/, '');
        if (p !== '/story/face' && p !== '/story/act') return;
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) return;

        window.kdCam = navigator.mediaDevices
            .getUserMedia({ video: { facingMode: 'user' }, audio: false })
            .catch(function (e) { window.kdCamFail = e; return null; });

        window.addEventListener('pagehide', function () {
            window.kdCam.then(function (s) {
                if (s) s.getTracks().forEach(function (t) { t.stop(); });
            });
        });
    })();
</script>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<%-- 300(Light)은 온보딩 생년월일의 년·월·일 글자에만 쓰인다 --%>
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
<%-- 부트스트랩 5.3.3 — CDN 이 아니라 우리 서버에서 준다.
     클래스는 한 곳도 안 쓰지만(2026-08-25 전수 확인), 이 파일의 Reboot 이
     box-sizing:border-box 와 body margin:0 을 깔아 준다. 자체 리셋이 없어서
     빼면 픽셀 실측 레이아웃이 통째로 어긋난다 — 지우지 말 것.
     CDN 을 떼는 이유: 스크립트는 앞선 스타일시트를 기다리므로 jsdelivr 왕복이
     카메라 시작(getUserMedia)까지 늦췄다. --%>
<link href="/css/bootstrap.min.css?v=299" rel="stylesheet">
<%-- ?v= 는 캐시 무효화용 — 정적 파일 수정 시 숫자를 올릴 것. ⚠ 절대 낮추지 말 것 --%>
<%-- 병합 결과는 양쪽이 합쳐진 제3의 파일이라 223 도 220 도 그 내용을 안 가리킨다.
     둘보다 큰 224 로 통일한다. ⚠ 절대 낮추지 말 것 --%>
<link href="/css/kkeudeok.css?v=294" rel="stylesheet">
<link href="/css/auth.css?v=294" rel="stylesheet">
<link href="/css/onboarding.css?v=294" rel="stylesheet">
<link href="/css/app.css?v=295" rel="stylesheet">
<link href="/css/report.css?v=294" rel="stylesheet">
<link href="/css/mypage.css?v=294" rel="stylesheet">
<link href="/css/child.css?v=294" rel="stylesheet">
<%-- 사용 단계 스위치 — 0 신규(아무것도 없음) · 1 일상 기록만 남김 · 2 이야기 1편 이상(수치 대시보드).
     ?stage=0|1|2 로 바꾸면 탭 단위로 유지돼 화면을 옮겨도 따라간다.
     ?empty=1 → 0 · ?empty=0 → 2 별칭은 예전 주소를 안 깨려고 남겨 둔다.
     블록이 깜빡이지 않게 defer 없이 <head> 에서 즉시 실행한다(fit-frame.js 와 같은 이유). --%>
<script>
    (function () {
        var q = new URLSearchParams(location.search), s = null;
        if (q.has('stage')) s = q.get('stage');
        else if (q.has('empty')) s = q.get('empty') === '0' ? '2' : '0';
        try {
            if (s !== null && '012'.indexOf(s) >= 0) sessionStorage.setItem('kdStage', s);
            s = sessionStorage.getItem('kdStage');
        } catch (e) { }
        /* ?stage= 로 직접 정한 게 없으면 **실제 학습 횟수**로 정한다(2026-08-10 요청:
           "학습을 한 번 하면 그 다음에는 수치가 나오는 대시보드로").
           **이야기를 한 편이라도 마치면 곧바로 2단계**(수치가 있는 대시보드) — 2026-08-10 요청.
           예전엔 3회였는데, 학습을 하고도 화면이 그대로라 "적용이 안 된다"고 느껴졌다.
           kdDaily(일상 기록만 남긴 상태) → 1단계 · 아무것도 없으면 0단계.
           ?stage= 를 쓰면 예전처럼 그 값이 이 탭에서 계속 이긴다(시연용).
           ⚠ kdStage 는 **시연 스위치 전용**이다. 다른 화면이 여기에 쓰면 학습 횟수가
             통째로 무시돼 "몇 번을 해도 대시보드가 그대로"가 된다(2026-08-10 지적, 실제 원인).
             승격이 필요하면 kdDaily 처럼 자기 키를 쓸 것. */
        if (!(s !== null && '012'.indexOf(s) >= 0)) {
            var done = 0, daily = 0;
            try {
                done = +(localStorage.getItem('kdDone') || 0) || 0;
                daily = sessionStorage.getItem('kdDaily') ? 1 : 0;
            } catch (e) { }
            s = done >= 1 ? '2' : (daily ? '1' : '0');
        }
        document.documentElement.dataset.kdStage = ('012'.indexOf(s) >= 0 && s) ? s : '2';
    })();
</script>
<%-- 아이 이름은 sessionStorage(kdOnb)에만 있어 JSP 는 예시값('지우')을 먼저 그린다.
     defer 스크립트가 뒤늦게 갈아끼우면 새로고침마다 이름이 한 번 깜빡였다(2026-08-10 지적).
     그래서 파서가 요소를 붙이는 즉시 여기서 채운다 — MutationObserver 콜백은 마이크로태스크라
     첫 페인트보다 먼저 돈다. defer 를 안 쓰는 이유는 fit-frame.js 와 같다.
     ⚠ data-kd="childName|childNameAge|childCall|childAge" 채우기는 이 블록이 유일한 주인이다.
       auth-validate.js 에 다시 넣으면 깜빡임이 그대로 돌아온다. --%>
<%-- 로그인한 보호자의 아이(DB) — ChildInfoAdvice 가 실어 준다. 없으면 이 줄들이 아예 안 나온다.
     meta 로 두는 이유: 아래 스크립트가 첫 페인트 전에 동기로 읽어야 하는데, 이름에 따옴표가
     섞여도 안전하고(HTML 이스케이프) fetch 처럼 늦게 도착하지 않는다. --%>
<%
    String kdcName = (String) request.getAttribute("kdChildName");
    if (kdcName != null && !kdcName.isBlank()) {
        String kdcCall = (String) request.getAttribute("kdChildCall");
        Object kdcAge = request.getAttribute("kdChildAge");
%>
<meta name="kd-child-name" content="<%= org.springframework.web.util.HtmlUtils.htmlEscape(kdcName) %>">
<meta name="kd-child-call" content="<%= org.springframework.web.util.HtmlUtils.htmlEscape(kdcCall == null ? kdcName : kdcCall) %>">
<meta name="kd-child-vocative" content="<%= org.springframework.web.util.HtmlUtils.htmlEscape(
        String.valueOf(request.getAttribute("kdChildVocative"))) %>">
<meta name="kd-child-age" content="<%= kdcAge == null ? "" : kdcAge %>">
<meta name="kd-char-key" content="<%= org.springframework.web.util.HtmlUtils.htmlEscape(
        String.valueOf(request.getAttribute("kdCharKey"))) %>">
<meta name="kd-char-name" content="<%= org.springframework.web.util.HtmlUtils.htmlEscape(
        String.valueOf(request.getAttribute("kdCharName"))) %>">
<%
    }
%>
<script>
    (function () {
        var v = {};
        try { v = JSON.parse(sessionStorage.getItem('kdOnb')) || {}; } catch (e) { }

        /* DB 에 아이가 있으면 그 값이 우선이다 — sessionStorage 는 온보딩을 막 마친
           그 탭에만 있어서, 다시 로그인하면 비어 예시값 '지우'가 그대로 남았다. */
        function kdMeta(n) {
            var m = document.querySelector('meta[name="' + n + '"]');
            return m ? m.content : '';
        }
        var srvName = kdMeta('kd-child-name');
        if (srvName) {
            v.name = srvName;
            v.callName = kdMeta('kd-child-call');
            v.vocative = kdMeta('kd-child-vocative');
            v.age = kdMeta('kd-child-age');
        }

        /* 함께하는 친구 — 이름과 그림. sessionStorage 보다 DB 가 우선이다.
           ⚠ 이게 없으면 재로그인 뒤 화면이 늘 '토리' 로 돌아갔다(2026-08-18 지적). */
        var charKey = kdMeta('kd-char-key');
        var charName = kdMeta('kd-char-name');

        if (charKey && charKey !== 'null') {
            /* 그림: data-kd-char="포즈" 가 붙은 <img> 를 고른 친구 것으로 바꾼다.
               없는 포즈 파일이면 토리 것으로 떨어뜨린다(캐릭터마다 포즈 수가 다르다). */
            new MutationObserver(function (recs) {
                for (var i = 0; i < recs.length; i++) {
                    for (var j = 0; j < recs[i].addedNodes.length; j++) swapChar(recs[i].addedNodes[j]);
                }
            }).observe(document.documentElement, { childList: true, subtree: true });

            function swapChar(root) {
                if (root.nodeType !== 1) return;
                var imgs = root.querySelectorAll ? root.querySelectorAll('[data-kd-char]') : [];
                if (root.hasAttribute && root.hasAttribute('data-kd-char')) {
                    imgs = [root].concat([].slice.call(imgs));
                }
                [].forEach.call(imgs, function (img) {
                    var pose = img.getAttribute('data-kd-char');
                    if (!pose) return;
                    img.onerror = function () {
                        this.onerror = null;
                        this.src = '/img/char-tori-' + pose + '.png';
                    };
                    img.src = '/img/char-' + charKey + '-' + pose + '.png';
                });
            }
        }

        if (charName && charName !== 'null') v.charName = charName;

        /* 성을 떼고 받침이 있으면 '이' 를 붙인다(성현이 / 지우) — auth-validate.js 도 이걸 쓴다 */
        var SURNAME2 = ['남궁', '황보', '제갈', '사공', '선우', '서문', '독고', '동방'];
        function givenName(full) {
            var n = (full || '').trim();
            if (n.length < 3) return n;
            return SURNAME2.indexOf(n.slice(0, 2)) !== -1 ? n.slice(2) : n.slice(1);
        }
        function callName(full) {
            var n = givenName(full);
            var i = n.charCodeAt(n.length - 1) - 0xAC00;
            return n + (i >= 0 && i < 11172 && i % 28 !== 0 ? '이' : '');
        }
        function ageOf(y, m, d) {
            var t = new Date(), age = t.getFullYear() - y;
            if (t.getMonth() + 1 < m || (t.getMonth() + 1 === m && t.getDate() < d)) age -= 1;
            return age;
        }
        /* 부를 때 쓰는 말 — 성을 뗀 이름에 아/야 를 붙인다.
           ⚠ callName() 결과("이진이")에 붙이면 "이진이야" 가 된다(2026-08-18 지적).
             부르는 말은 이를 붙이지 않은 이름에서 만든다. */
        function vocative(name) {
            var n = givenName(name);
            return n ? n + (hasJong(n) ? '아' : '야') : '';
        }
        window.kdName = { call: callName, age: ageOf, vocative: vocative };

        /* 아이 이름이 없어도(직접 URL 진입) 친구 이름은 채운다 — 둘은 출처가 다르다 */
        if (!v.name && !v.charName) return;

        /* 나이는 서버가 준 값이 우선 — 생년월일은 sessionStorage 에만 있다 */
        var age = (v.age !== undefined && v.age !== '' && !isNaN(v.age)) ? Number(v.age)
                : (v.birthY ? ageOf(v.birthY, v.birthM, v.birthD) : null);
        var text = {
            childName: v.name,
            childNameAge: v.name + (age === null ? '' : ' · ' + age + '세'),
            childCall: v.callName || callName(v.name),
            childVocative: v.vocative || vocative(v.name),
            childAge: age === null ? null : String(age),
            charName: v.charName
        };

        /* ---------- 한국어 조사 ----------
           JSP 는 "{c}가 깜짝 놀랐어요" 처럼 조사를 <span> **바깥** 글자로 찍어 둔다.
           이름만 갈아끼우면 '애칭가' 처럼 어긋난다(2026-08-18 지적) — 기본 캐릭터·이름이
           모두 받침이 없어 오래 드러나지 않았다. 이름을 넣은 뒤 뒤따르는 조사도 고친다.
           ⚠ '이랑' 은 '이' 로도 시작하므로 긴 것부터 본다. */
        var JOSA = [['이랑', '랑'], ['과', '와'], ['이', '가'], ['을', '를'], ['은', '는'], ['아', '야']];

        function hasJong(word) {
            if (!word) return false;
            var c = word.charAt(word.length - 1).charCodeAt(0) - 0xAC00;
            return c >= 0 && c < 11172 && c % 28 !== 0;
        }

        function fixJosa(el, word) {
            var n = el.nextSibling;
            if (!n || n.nodeType !== 3 || !word) return;

            var t = n.nodeValue;
            var jong = hasJong(word);

            for (var i = 0; i < JOSA.length; i++) {
                var withJ = JOSA[i][0], without = JOSA[i][1];

                if (t.indexOf(withJ) === 0) {
                    n.nodeValue = (jong ? withJ : without) + t.slice(withJ.length);
                    return;
                }
                if (t.indexOf(without) === 0) {
                    n.nodeValue = (jong ? withJ : without) + t.slice(without.length);
                    return;
                }
            }
        }

        function fill(root) {
            if (root.nodeType !== 1) return;
            var hooks = root.querySelectorAll('[data-kd]');
            for (var i = 0; i < hooks.length; i++) {
                var t = text[hooks[i].dataset.kd];
                if (!t) continue;

                /* 입력칸은 textContent 가 아니라 value 를 채워야 보인다(마이페이지 아이 이름) */
                if (hooks[i].tagName === 'INPUT') {
                    hooks[i].value = t;
                } else {
                    hooks[i].textContent = t;
                    fixJosa(hooks[i], t);
                }
            }
            var own = text[root.dataset && root.dataset.kd];
            if (own) {
                root.textContent = own;
                fixJosa(root, own);
            }
        }

        new MutationObserver(function (recs) {
            for (var i = 0; i < recs.length; i++) {
                for (var j = 0; j < recs[i].addedNodes.length; j++) fill(recs[i].addedNodes[j]);
            }
        }).observe(document.documentElement, { childList: true, subtree: true });
    })();
</script>
<script>
    (function () {
        var call = window.fetch;
        if (!call) return;

        var OPEN = /^\/$|^\/(login|signup|find-id|find-pw)(\/|$)/;

        window.fetch = function () {
            return call.apply(this, arguments).then(function (res) {
                if (res.status === 401 && !OPEN.test(location.pathname)) {
                    location.replace('/login');
                }
                return res;
            });
        };
    })();
</script>
<script src="/js/fit-frame.js?v=220"></script>
<script src="/js/auth-validate.js?v=294" defer></script>
<script src="/js/onb-select.js?v=220" defer></script>
<%-- kd-roadmap.js 는 대시보드 로드맵 카드·12주 모달만 그린다(주차 편집기는 2026-08-10 삭제).
     이제 /api/roadmap 에서 아이 맞춤 계획을 받아 오고, 못 받으면 내장 정석 커리큘럼으로 그린다. --%>
<%-- 로드맵이 준비되기 전에는 [학습 시작하기] 를 잠근다(이야기 생성중…).
     온보딩 뒤 로드맵 생성이 뒤에서 도는 동안 들어가면 계획 없이 이야기가 만들어진다. --%>
<script src="/js/kd-ready-gate.js?v=294" defer></script>
<%-- 학습 홈 최근 기록·대시보드 연속 이용을 실제 기록으로 채운다 --%>
<script src="/js/kd-summary.js?v=294" defer></script>
<%-- 성장 리포트 수치를 실제 학습 기록으로 채운다 --%>
<script src="/js/kd-report.js?v=294" defer></script>
<script src="/js/kd-roadmap.js?v=297" defer></script>
<%-- 온보딩 표정 등록 — 그 아이 기준값을 만들어 학습4 표정 판정에 쓴다.
     ⚠ auth-validate.js 의 kdOnbFaceNext() 를 감싸므로 반드시 **그 뒤에** 실행돼야 한다.
       둘 다 defer 라 문서 순서대로 도니 이 줄을 위로 올리지 말 것. --%>
<script src="/js/kd-face-calib.js?v=294" defer></script>
<%-- 온보딩 등록 — 체크리스트 답을 모으고, 완료 화면에서 서버에 아이를 만든다.
     ⚠ onb-select.js 의 kdSubmitOnbChecklist* 를 감싸므로 반드시 그 뒤에 실행돼야 한다. --%>
<script src="/js/kd-onboarding.js?v=294" defer></script>
