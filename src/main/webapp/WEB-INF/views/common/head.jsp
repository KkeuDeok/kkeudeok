<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
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
<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
<link href="/css/bootstrap.min.css?v=299" rel="stylesheet">
<link href="/css/kkeudeok.css?v=294" rel="stylesheet">
<link href="/css/auth.css?v=294" rel="stylesheet">
<link href="/css/onboarding.css?v=294" rel="stylesheet">
<link href="/css/app.css?v=295" rel="stylesheet">
<link href="/css/report.css?v=294" rel="stylesheet">
<link href="/css/mypage.css?v=294" rel="stylesheet">
<link href="/css/child.css?v=294" rel="stylesheet">
<script>
    (function () {
        var q = new URLSearchParams(location.search), s = null;
        if (q.has('stage')) s = q.get('stage');
        else if (q.has('empty')) s = q.get('empty') === '0' ? '2' : '0';
        try {
            if (s !== null && '012'.indexOf(s) >= 0) sessionStorage.setItem('kdStage', s);
            s = sessionStorage.getItem('kdStage');
        } catch (e) { }
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

        var charKey = kdMeta('kd-char-key');
        var charName = kdMeta('kd-char-name');

        if (charKey && charKey !== 'null') {
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
        function vocative(name) {
            var n = givenName(name);
            return n ? n + (hasJong(n) ? '아' : '야') : '';
        }
        window.kdName = { call: callName, age: ageOf, vocative: vocative };

        if (!v.name && !v.charName) return;

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
<script src="/js/kd-ready-gate.js?v=294" defer></script>
<script src="/js/kd-summary.js?v=294" defer></script>
<script src="/js/kd-report.js?v=294" defer></script>
<script src="/js/kd-roadmap.js?v=297" defer></script>
<script src="/js/kd-face-calib.js?v=294" defer></script>
<script src="/js/kd-onboarding.js?v=294" defer></script>
