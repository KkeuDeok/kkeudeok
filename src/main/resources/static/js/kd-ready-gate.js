(function () {
    'use strict';

    var SELECTOR = 'a.kd-btn[href="/learn"], a.kd-btn[href^="/story/scene"]';

    var PATHS = ['/dashboard', '/learn'];
    if (PATHS.indexOf(location.pathname.replace(/\/$/, '')) < 0) return;

    var WAIT_TEXT = '이야기 생성중…';

    var EVERY_MS = 3000;
    var MAX_TRIES = 200;

    var tries = 0;
    var locked = [];

    injectStyle();
    noticeIfBounced();
    check();

    function noticeIfBounced() {

        var bounced = false;
        try {
            bounced = sessionStorage.getItem('kdStartFail') === '1';
            sessionStorage.removeItem('kdStartFail');
        } catch (e) { }

        if (!bounced) return;

        var box = document.createElement('p');
        box.className = 'kd-gate-notice';
        box.textContent = '이야기를 아직 만들지 못했어요. 잠시 뒤 다시 눌러 주세요.';

        var host = document.querySelector('.learn-row');

        if (host && host.parentNode) {
            host.parentNode.insertBefore(box, host);
        } else {
            document.body.insertBefore(box, document.body.firstChild);
        }
    }

    function check() {
        fetch('/api/roadmap/status')
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (d) {
                if (d && d.ready) { unlock(); return; }

                if (d && d.state === 'NO_CHILD') { unlock(); return; }

                lock();
                tries += 1;

                if (tries >= MAX_TRIES) {
                    label('이야기를 못 만들었어요');
                    return;
                }

                setTimeout(check, EVERY_MS);
            })
            .catch(function () {
                unlock();
            });
    }

    function lock() {
        if (locked.length) return;

        document.querySelectorAll(SELECTOR).forEach(function (a) {
            locked.push({ el: a, text: a.textContent, href: a.getAttribute('href') });
            a.classList.add('kd-gate-wait');
            a.setAttribute('aria-disabled', 'true');
            a.removeAttribute('href');
            a.textContent = WAIT_TEXT;
        });
    }

    function label(text) {
        locked.forEach(function (o) { o.el.textContent = text; });
    }

    function unlock() {
        locked.forEach(function (o) {
            o.el.classList.remove('kd-gate-wait');
            o.el.removeAttribute('aria-disabled');
            o.el.setAttribute('href', o.href);
            o.el.textContent = o.text;
        });
        locked = [];
    }

    function injectStyle() {
        if (document.getElementById('kdGateStyle')) return;

        var st = document.createElement('style');
        st.id = 'kdGateStyle';
        st.textContent =
            '.kd-gate-wait{pointer-events:none;cursor:default;opacity:.6;' +
            'animation:kdGateBreath 1.6s ease-in-out infinite}' +
            '@keyframes kdGateBreath{0%,100%{opacity:.5}50%{opacity:.8}}' +
            '.kd-gate-notice{max-width:1200px;margin:12px auto 0;padding:12px 16px;' +
            'border-radius:12px;background:#fff6ea;color:#8a5a00;font-size:14px;text-align:center}';

        document.head.appendChild(st);
    }
}());
