/* 아동 학습 흐름(스토리) 전용 동작.
   원칙: 화면을 새로 띄우지 않는다 — 아이 입장에서 페이지가 통째로 바뀌면 '새로고침'처럼 보여
   방금 뭘 눌렀는지 알 수 없다. 눌린 자리에서 바로 반응이 보여야 한다.
   JS 가 없으면 링크·주소가 그대로 동작하므로(점진적 향상) 화면이 깨지지는 않는다. */
(function () {
    'use strict';

    /* ---------- 오답 카드 → 그 자리에서 모달 ----------
       예전엔 ?wrong= 을 붙여 서버가 다시 그렸는데, 화면이 깜빡여서 아이가
       "졸려서를 눌렀더니 새로고침만 됐다"고 느꼈다. 이제 클릭을 가로채 모달만 띄운다. */
    var dim = document.querySelector('.story-dim');

    function openWrong(pickEl) {
        if (!dim) return false;
        document.querySelectorAll('.feel-card.is-pick, .why-card.is-pick')
            .forEach(function (el) { el.classList.remove('is-pick'); });
        if (pickEl) pickEl.classList.add('is-pick');
        dim.hidden = false;
        /* 다시 열 때도 등장 동작이 걸리게 클래스를 한 번 뗐다 붙인다 */
        dim.classList.remove('is-on');
        void dim.offsetWidth;
        dim.classList.add('is-on');
        var btn = dim.querySelector('.ft a');
        if (btn) btn.focus();
        return true;
    }

    function closeWrong() {
        if (!dim) return;
        dim.classList.remove('is-on');
        dim.hidden = true;
        document.querySelectorAll('.feel-card.is-pick, .why-card.is-pick')
            .forEach(function (el) { el.classList.remove('is-pick'); });
    }

    document.querySelectorAll('[data-wrong]').forEach(function (card) {
        card.addEventListener('click', function (e) {
            if (openWrong(card)) e.preventDefault();
        });
    });

    if (dim) {
        dim.querySelectorAll('[data-close]').forEach(function (b) {
            b.addEventListener('click', function (e) { e.preventDefault(); closeWrong(); });
        });
        /* 바깥을 눌러도 닫힌다 — 아이가 모달에 갇히면 안 된다 */
        dim.addEventListener('click', function (e) { if (e.target === dim) closeWrong(); });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && !dim.hidden) closeWrong();
        });
    }

    /* ---------- [말로 알려줄래!] → 그 자리에서 듣는 중 ----------
       화면을 새로 띄우지 않고 버튼만 파형으로 바꾼다. 다시 누르면 되돌아온다. */
    var mic = document.querySelector('.why-mic');
    var listen = document.querySelector('.why-listen');
    if (mic && listen) {
        mic.addEventListener('click', function (e) {
            e.preventDefault();
            var on = listen.hidden;
            listen.hidden = !on;
            mic.hidden = on;
            /* TODO: 실제 음성 인식(STT)이 붙으면 여기서 시작·중지를 건다 */
        });
        listen.addEventListener('click', function () {
            listen.hidden = true;
            mic.hidden = false;
        });
    }

    /* ---------- 정답 카드 → 맞았다고 알려 준 뒤 넘어간다 ----------
       예전엔 그냥 링크라 바로 다음 화면으로 넘어갔다. 오답에는 모달이 뜨는데 정답에는
       아무 말이 없어, 아이가 맞혔는지 모른 채 넘어갔다(2026-08-10 지적).
       ⚠ 화면을 새로 띄우지 않는다 — 카드 자리에서 표시하고 잠깐 뒤에 이동한다. */
    function cheer(card, href) {
        card.classList.add('is-right');
        var tag = document.createElement('span');
        tag.className = 'kd-yay';
        tag.textContent = '맞았어요!';
        card.appendChild(tag);
        /* 다른 카드는 눌리지 않게 — 연타로 두 번 넘어가는 걸 막는다 */
        document.querySelectorAll('.feel-card, .why-card').forEach(function (el) {
            if (el !== card) el.classList.add('is-dim');
            el.style.pointerEvents = 'none';
        });
        setTimeout(function () { location.href = href; }, 1100);
    }

    document.querySelectorAll('[data-right]').forEach(function (card) {
        card.addEventListener('click', function (e) {
            e.preventDefault();
            cheer(card, card.getAttribute('href'));
        });
    });

    /* ---------- [다시 들려줘] — 화면 글을 소리로 읽어 준다 ----------
       예전엔 핸들러가 없어 눌러도 아무 반응이 없었다(2026-08-10 지적).
       브라우저에 내장된 speechSynthesis 를 쓴다 — 외부 서비스·키·용량이 필요 없다.
       읽을 글은 화면마다 다르므로 '제목류' 를 위에서부터 모아 잇는다. */
    var SAY_SELECTORS = ['.story-title', '.story-sub', '.story-ask',
                         '.feel-recap', '.feel-q', '.feel-hint',
                         '.why-title', '.why-sub', '.cam-title'];

    function screenText() {
        var out = [];
        SAY_SELECTORS.forEach(function (sel) {
            document.querySelectorAll(sel).forEach(function (el) {
                var t = (el.textContent || '').trim();
                if (t && out.indexOf(t) === -1) out.push(t);
            });
        });
        return out.join(' ');
    }

    var listenBtns = document.querySelectorAll('.kd-sub-listen');
    if (listenBtns.length && 'speechSynthesis' in window) {
        listenBtns.forEach(function (btn) {
            var label = btn.textContent;
            btn.addEventListener('click', function () {
                if (speechSynthesis.speaking) {          /* 다시 누르면 멈춘다 */
                    speechSynthesis.cancel();
                    btn.textContent = label;
                    btn.classList.remove('is-speaking');
                    return;
                }
                var text = screenText();
                if (!text) return;
                var u = new SpeechSynthesisUtterance(text);
                u.lang = 'ko-KR';
                u.rate = 0.95;                            /* 아이가 따라올 수 있게 조금 느리게 */
                btn.textContent = '읽는 중…';
                btn.classList.add('is-speaking');
                u.onend = u.onerror = function () {
                    btn.textContent = label;
                    btn.classList.remove('is-speaking');
                };
                speechSynthesis.speak(u);
            });
        });
        /* 화면을 떠날 때 소리가 따라다니면 안 된다 */
        window.addEventListener('pagehide', function () { speechSynthesis.cancel(); });
    }

    /* ---------- [잘 모르겠어] — 그 자리에서 짚어 준다 ----------
       마음 읽기에는 힌트 화면(학습2b)이 따로 있어 링크 그대로 두고, 나머지 화면은
       여기서 '어디를 보면 되는지' 를 짚어 준다. 예전엔 반응이 아예 없었다.
         왜?    → 정답 카드에 테두리
         표정·행동 → 코너의 시범 카드를 두어 번 흔든다
         이야기  → 다음으로 가는 버튼 */
    function hintTarget() {
        return document.querySelector('.why-card:not([data-wrong])')
            || document.querySelector('.cam-hint')
            || document.querySelector('.story-cta');
    }

    document.querySelectorAll('button.kd-sub-hint').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var t = hintTarget();
            if (!t) return;
            t.classList.remove('kd-point');
            void t.offsetWidth;
            t.classList.add('kd-point');
            setTimeout(function () { t.classList.remove('kd-point'); }, 2400);
        });
    });

    /* ---------- 학습 종료 확인 — [다음에 할래] · [이 동작은 하기 싫어] ----------
       둘 다 예전엔 핸들러가 없어 눌러도 아무 반응이 없었다(2026-08-10 지적).
       바로 끝내지 않고 물어보는 이유: 아이가 실수로 눌러 학습이 통째로 끝나면 안 된다.
       ⚠ '이 동작은 하기 싫어' 는 예전에 칭찬 화면으로 직행했다 — 안 했는데 칭찬을 받는 셈이라
         칭찬을 건너뛰고 대시보드로 나간다(사용자 확정). */
    var ask = document.querySelector('.story-quit');

    function openAsk() {
        if (!ask) return false;
        ask.hidden = false;
        ask.classList.remove('is-on');
        void ask.offsetWidth;
        ask.classList.add('is-on');
        var no = ask.querySelector('[data-ask-no]');
        if (no) no.focus();                 /* 기본 초점은 '더 할래' — 실수로 끝나지 않게 */
        return true;
    }

    function closeAsk() {
        if (!ask) return;
        ask.classList.remove('is-on');
        ask.hidden = true;
    }

    document.querySelectorAll('[data-ask]').forEach(function (b) {
        b.addEventListener('click', function (e) { if (openAsk()) e.preventDefault(); });
    });

    if (ask) {
        ask.querySelectorAll('[data-ask-no]').forEach(function (b) {
            b.addEventListener('click', function (e) { e.preventDefault(); closeAsk(); });
        });
        ask.addEventListener('click', function (e) { if (e.target === ask) closeAsk(); });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && !ask.hidden) closeAsk();
        });
    }

    /* ---------- 세션 결과 — 아무 데나 누르면 학습을 마친다 ----------
       ⚠ 아동홈으로 보내면 안 된다. 아동홈은 학습을 '시작'하는 입구라 곧바로 같은 이야기가
         다시 돌아 무한 루프가 된다(실제로 그렇게 만들었다가 지적받음).
       Figma 의 "3초 뒤 자동" 도 안 쓴다 — 아이가 칭찬을 다 보기 전에 넘어간다. */
    /* 아동홈에 들어오면 '이번 세션은 아직 안 셌다' 로 표시해 둔다 — 칭찬 화면을 새로고침해도
       두 번 세지 않게 하려는 것이다. */
    if (document.querySelector('.home-card')) {
        try { sessionStorage.removeItem('kdCounted'); } catch (e) { }
    }

    var done = document.querySelector('.done-stage');
    if (done) {
        /* 칭찬 화면까지 왔으면 한 세션을 마친 것 — 대시보드 단계가 이 값으로 올라간다
           (head.jsp 가 kdDone 을 읽는다). 0회 0단계 · 1~2회 1단계 · 3회 이상 2단계. */
        try {
            if (!sessionStorage.getItem('kdCounted')) {
                sessionStorage.setItem('kdCounted', '1');
                localStorage.setItem('kdDone', String((+(localStorage.getItem('kdDone') || 0) || 0) + 1));
            }
        } catch (e) { }
        done.addEventListener('click', function () {
            location.href = done.getAttribute('data-home');
        });
    }
}());
