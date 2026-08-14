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

    /* 오답 모달의 앞 문장은 **누른 카드**의 특징이어야 한다.
       ⚠ 예전엔 JSP 가 화면 그릴 때 한 벌만 박아 둬서, 화나요를 눌러도 놀랐어요를 눌러도
         "기쁠 때는 입꼬리가 올라가" 가 떴다(2026-08-13 지적). 누를 때 갈아끼운다.
       카드에 data-trait 이 없으면(옛 마크업) 아래 표로 메운다. */
    var FEEL_TRAIT = {
        happy:    '기쁠 때는 입꼬리가 올라가.',
        sad:      '슬플 때는 눈썹이 아래로 처져.',
        angry:    '화날 때는 눈썹이 뾰족해져.',
        surprise: '놀랄 때는 눈이 동그래져.'
    };

    function paintTrait(pickEl) {
        if (!pickEl || !dim) return;

        var span = dim.querySelector('.clue span');
        if (!span) return;

        var key = pickEl.getAttribute('data-kd-key') || pickEl.getAttribute('data-wrong');
        var trait = pickEl.getAttribute('data-trait') || FEEL_TRAIT[key];

        if (trait) span.textContent = trait;
    }

    function openWrong(pickEl) {
        if (!dim) return false;
        paintTrait(pickEl);
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

    /* ---------- 카드 클릭 — 정답/오답을 **누를 때** 판정한다 ----------
       ⚠ 예전엔 화면이 뜰 때 [data-right]/[data-wrong] 을 찾아 카드마다 따로 핸들러를 달았다.
         그러면 나중에 attribute 가 바뀌어도 핸들러는 처음 붙은 자리에 그대로 남는다.
         AI 가 카드 구성을 정하면서(2026-08-13) story-session.js 가 늦게 정답 자리를 옮기는데,
         옛 방식에서는 **바뀌기 전 카드가 계속 정답으로 동작**했다.
       그래서 위임(delegation)으로 바꿨다 — 누르는 순간의 attribute 를 본다. */
    document.addEventListener('click', function (e) {
        var card = e.target.closest ? e.target.closest('.feel-card, .why-card') : null;
        if (!card) return;

        /* 카드 안의 소리 배지는 자기 핸들러에서 전파를 끊는다(아래) — 여기까지 오지 않는다 */
        if (card.hasAttribute('data-right')) {
            e.preventDefault();
            cheer(card, card.getAttribute('href'));
            return;
        }

        if (card.hasAttribute('data-wrong')) {
            if (openWrong(card)) e.preventDefault();
        }
    });

    /* ---------- [다시 들려줘] — 화면 글을 소리로 읽어 준다 ----------
       예전엔 핸들러가 없어 눌러도 아무 반응이 없었다(2026-08-10 지적).
       브라우저에 내장된 speechSynthesis 를 쓴다 — 외부 서비스·키·용량이 필요 없다.
       읽을 글은 화면마다 다르므로 '제목류' 를 위에서부터 모아 잇는다. */
    var SAY_SELECTORS = ['.story-title', '.story-sub', '.story-ask',
                         '.feel-recap', '.feel-q', '.feel-hint',
                         '.why-title', '.why-sub', '.why-hint', '.cam-title'];

    function screenText() {
        var out = [];
        SAY_SELECTORS.forEach(function (sel) {
            document.querySelectorAll(sel).forEach(function (el) {
                if (el.hidden) return;               /* 아직 안 보여 준 힌트까지 읽으면 답을 알려 주는 셈이다 */
                var t = (el.textContent || '').trim();
                if (t && out.indexOf(t) === -1) out.push(t);
            });
        });
        return out;                              /* 조각 배열 그대로 — mp3 가 문장 단위라 여기서 이으면 안 맞는다 */
    }

    /* ---------- 소리는 두 경로다 ----------
       ① 미리 뽑아 둔 mp3 (audio-map.js 의 window.KD_AUDIO — 문구 그대로가 열쇠다)
       ② 없으면 브라우저 낭독

       ①을 쓰는 이유는 음질보다 **예측 가능성**이다. 브라우저 낭독은 그 PC 에 깔린 음성에
       좌우돼서, 한국어 음성이 없는 기계에서는 영어 음성이 한글을 읽어 발음이 뭉갠다
       (이 개발 PC 에 깔린 음성은 Microsoft Heami 하나뿐이었다). 시연 중에 그게 터지면 못 고친다.
       ②를 지우지 않는 이유도 같다 — 문구를 고쳐 mp3 가 없어져도 조용해지지 않고 예전 소리로 돌아간다.
       ⚠ mp3 를 다시 뽑으면 AUDIO_V 를 올릴 것. 안 올리면 브라우저가 옛 소리를 계속 꺼낸다. */
    var AUDIO_BASE = '/audio/story/';
    var AUDIO_V = '?v=222';

    function clipFor(text) { return window.KD_AUDIO ? window.KD_AUDIO[text] : null; }

    var hasTTS = 'speechSynthesis' in window;
    var canSay = hasTTS || !!window.KD_AUDIO;
    var playing = null;                           /* 재생 중인 mp3 */
    var clearMark = null;                         /* 읽는 중 표시를 되돌리는 함수 */

    /* 지금 소리가 나가는 중인가 / 멈춰라 — 두 경로를 한 곳에서 본다.
       여기를 안 거치고 speechSynthesis 를 직접 부르면 mp3 는 안 멈춘다. */
    function isSpeaking() {
        return !!playing || (hasTTS && (speechSynthesis.speaking || speechSynthesis.pending));
    }

    function stopSpeaking() {
        if (playing) { playing.pause(); playing = null; }
        if (hasTTS) speechSynthesis.cancel();
        /* 끊었으면 표시도 같이 지운다 — 안 그러면 배지가 '읽는 중' 인 채로 굳는다 */
        if (clearMark) { clearMark(); clearMark = null; }
    }

    /* 낭독 음색 — 기본 음성이 "너무 기괴하다"는 지적(2026-08-10 피드백 3).
       아이 목소리 파일을 따로 만드는 대신 음높이를 올려 아이 톤에 가깝게 만든다.
       파일이 0개라 문구가 바뀌어도 따라오고, 감정 3벌 × 화면 10개를 다시 뽑을 일이 없다.
       ⚠ getVoices() 는 첫 호출에 **빈 배열**을 준다 — voiceschanged 에서 한 번 더 잡지 않으면
         조용히 기본(영어) 음성으로 읽어 발음이 뭉갠다. */
    var koVoice = null;

    function pickVoice() {
        var vs = speechSynthesis.getVoices();
        for (var i = 0; i < vs.length; i++) {
            if (/^ko/i.test(vs[i].lang)) { koVoice = vs[i]; return; }
        }
    }

    if (hasTTS) {                                 /* mp3 만 있고 낭독이 없는 브라우저도 있다 */
        pickVoice();
        speechSynthesis.addEventListener('voiceschanged', pickVoice);
    }

    /* 읽어 주기는 여기 한 곳 — [다시 들려줘] · 카드의 소리 배지 · 힌트가 같이 쓴다.
       el 을 주면 읽는 동안 is-speaking 이 붙고, label 까지 주면 글자도 잠깐 바뀐다. */
    /* 읽는 중 표시 — 두 경로가 같이 쓴다. 되돌리는 함수를 준다. */
    function mark(el, label) {
        var restore = function () {};
        if (el) {
            el.classList.add('is-speaking');
            if (label) el.textContent = '읽는 중…';
            restore = function () {
                el.classList.remove('is-speaking');
                if (label) el.textContent = label;
            };
        }
        clearMark = restore;
        return function () { restore(); if (clearMark === restore) clearMark = null; };
    }

    /* text 는 문장 하나여도 되고 조각 배열이어도 된다.
       화면 낭독은 제목·부제·질문이 각각 따로 뽑힌 mp3 라 배열로 받아 이어서 튼다. */
    function speak(text, el, label) {
        if (!canSay) return;
        var parts = (typeof text === 'string' ? [text] : (text || [])).filter(Boolean);
        if (!parts.length) return;
        stopSpeaking();                           /* 앞의 소리는 끊는다 — 두 소리가 겹치면 못 알아듣는다 */

        /* 한 조각이라도 mp3 가 없으면 통째로 브라우저 낭독으로 간다.
           절반은 사람 목소리, 절반은 로봇이면 안 하느니만 못하다. */
        var clips = [];
        for (var i = 0; i < parts.length; i++) {
            var c = clipFor(parts[i]);
            if (!c) return sayWithBrowser(parts.join(' '), el, label);
            clips.push(c);
        }

        var done = mark(el, label);
        var joined = parts.join(' ');
        var n = 0;

        function fallback() { playing = null; done(); sayWithBrowser(joined, el, label); }

        function next() {
            if (n >= clips.length) { playing = null; done(); return; }
            var a = new Audio(AUDIO_BASE + clips[n++] + AUDIO_V);
            playing = a;
            a.onended = next;
            a.onerror = fallback;                 /* 파일이 없거나 못 읽으면 조용해지면 안 된다 */
            var p = a.play();
            /* 자동재생 차단. mp3 는 speechSynthesis 보다 더 엄격하게 막힌다 —
               여기서 안 받으면 첫 화면이 통째로 무음이 된다. */
            if (p && p['catch']) p['catch'](fallback);
        }
        next();
    }

    function sayWithBrowser(text, el, label) {
        if (!hasTTS) return;
        var u = new SpeechSynthesisUtterance(text);
        u.lang = 'ko-KR';
        u.rate = 0.95;                            /* 아이가 따라올 수 있게 조금 느리게 */
        u.pitch = 1.15;                           /* 어른 목소리 그대로면 아이가 무서워한다.
                                                     1.4 는 "기괴하다"는 지적을 못 없앴다 — Web Speech 의 pitch 는
                                                     포먼트를 같이 올려 주지 않아, 크게 올릴수록 아이가 아니라
                                                     다람쥐 소리에 가까워진다. 아이 톤은 여기까지가 한계다.
                                                     제대로 된 아이 목소리는 mp3 를 미리 뽑는 수밖에 없다. */
        if (koVoice) u.voice = koVoice;           /* 한국어 음성이 없으면 브라우저 기본에 맡긴다 */
        var done = mark(el, label);
        u.onend = u.onerror = done;
        speechSynthesis.speak(u);
    }

    var listenBtns = document.querySelectorAll('.kd-sub-listen');
    if (listenBtns.length && canSay) {
        listenBtns.forEach(function (btn) {
            var label = btn.textContent;
            btn.addEventListener('click', function () {
                if (isSpeaking()) {                     /* 다시 누르면 멈춘다 */
                    stopSpeaking();
                    btn.textContent = label;
                    btn.classList.remove('is-speaking');
                    return;
                }
                speak(screenText(), btn, label);
            });
        });
        /* 화면을 떠날 때 소리가 따라다니면 안 된다 */
        window.addEventListener('pagehide', function () { stopSpeaking(); });

        /* ---------- 화면에 들어오면 바로 읽어 준다 (2026-08-11 요청) ----------
           글을 아직 못 읽는 아이가 대상이라 먼저 들려주는 게 기본이고,
           그래야 [다시 들려줘] 라는 이름이 말이 된다(전에는 '처음 들려줘' 가 없었다).
           ⚠ 브라우저 자동재생 정책 — 아직 아무것도 누르지 않은 문서에서는 낭독이 막힌다.
             막혔으면 아이가 화면 어디든 처음 누를 때 대신 읽어 준다. */
        var first = listenBtns[0];
        var firstLabel = first.textContent;

        function sayScreen() { speak(screenText(), first, firstLabel); }

        sayScreen();

        setTimeout(function () {
            if (isSpeaking()) return;                                          /* 잘 나갔다 */
            document.addEventListener('pointerdown', function once() {
                document.removeEventListener('pointerdown', once);
                sayScreen();
            });
        }, 500);
    }

    /* ---------- 카드의 소리 배지 ----------
       배지가 카드(<a>) 안의 span 이라 핸들러가 없는 동안 클릭이 카드로 새어
       **그 카드를 고른 것으로 처리됐다** — 정답 배지를 누르면 다음 화면으로 넘어가고
       오답 배지를 누르면 오답 모달이 떴다(2026-08-10 지적). 마음읽기 카드도 같은 구조다.
       여기서 전파를 끊고 그 카드 글자를 읽어 준다.
       ⚠ 접근성: 카드를 <div role="button"> 으로 바꾸기 전까지 배지는 마우스 전용이다
         (<a> 안에 button 을 넣으면 중첩 인터랙티브라 HTML 이 깨진다). */
    document.querySelectorAll('.feel-card .spk, .why-card .spk').forEach(function (sp) {
        sp.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            var card = sp.closest('.feel-card, .why-card'), words = [];
            if (card) {
                card.querySelectorAll('.nm, .ds').forEach(function (p) {
                    words.push((p.textContent || '').trim());
                });
            }
            speak(words, sp);                     /* 이름·설명이 각각 따로 뽑힌 mp3 라 이어 붙이지 않는다 */
        });
    });

    /* ---------- [잘 모르겠어] — 그 자리에서 짚어 준다 ----------
       마음 읽기에는 힌트 화면(학습2b)이 따로 있어 링크 그대로 두고, 나머지 화면은
       여기서 '어디를 보면 되는지' 를 짚어 준다. 예전엔 반응이 아예 없었다.
         왜?    → 힌트 문구를 보여 주고 읽어 준다(.why-hint)
         표정·행동 → 코너의 시범 카드를 두어 번 흔든다
         이야기  → 다음으로 가는 버튼 */
    function hintTarget() {
        return document.querySelector('.cam-hint')
            || document.querySelector('.story-cta');
    }

    document.querySelectorAll('button.kd-sub-hint').forEach(function (btn) {
        btn.addEventListener('click', function () {
            /* 이유 찾기 — 정답 카드를 짚어 주면 그건 힌트가 아니라 답이다(2026-08-10 지적).
               어디를 보면 되는지만 말해 준다. */
            var hint = document.querySelector('.why-hint');
            if (hint) {
                hint.hidden = false;
                speak(hint.textContent);
                return;
            }
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
    /* 이야기를 새로 시작하면 '이번 세션은 아직 안 셌다' 로 표시해 둔다 — 칭찬 화면을
       새로고침해도 두 번 세지 않게 하려는 것이다.
       ⚠ 예전엔 아동홈(.home-card)에서만 지웠다. 그런데 실제 진입은 학습 홈 → /story/scene 이라
         아동홈을 안 거친다 → 한 탭에서 kdDone 이 1 에서 멈춰 3단계에 영영 못 갔다
         (2026-08-10 "3번 해도 적용이 안 된다"의 원인). 이야기 시작 화면에서도 지운다. */
    if (document.querySelector('.home-card') || location.pathname === '/story/scene') {
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

    /* ---------- 표정·동작이 어긋났을 때 (2026-08-10 피드백 4) ----------
       "실제 카메라가 다른 표정을 잡으면 어떻게 되냐"에 대한 답이다.
       표정 인식은 아직 없으므로 첫 시도에 한 번 되짚어 주고, 다시 누르면 통과한다
       (온보딩 카메라 게이트도 '한 번 더 누르면 통과'라 흐름이 같다).
       ⚠ 인식이 붙으면 이 자리를 실제 판정 결과로 바꾼다 — 지금은 '틀렸을 때 무슨 일이
         일어나는지'를 보여 주는 자리지, 무조건 한 번 막으라는 규칙이 아니다. */
    var toastEl = document.querySelector('.child-toast');
    var toastTimer = null;

    function toast(msg) {
        if (!toastEl) return;
        toastEl.textContent = msg;
        toastEl.hidden = false;
        /* 같은 문구를 다시 띄울 때도 나타나는 동작이 걸리게 클래스를 뗐다 붙인다 */
        toastEl.classList.remove('is-on');
        void toastEl.offsetWidth;
        toastEl.classList.add('is-on');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(function () { toastEl.classList.remove('is-on'); }, 2800);
    }

    /* 감정별로 짚어 줄 곳이 다르다. 동작(act)은 감정과 무관하게 한 문구. */
    var FACE_TIP = {
        sad:   '입꼬리를 조금만 더 아래로 내려 볼까?',
        angry: '눈썹을 가운데로 모아 볼까?',
        happy: '입꼬리를 조금만 더 올려 볼까?'
    };

    var camCta = document.querySelector('.cam-actions .kd-cta');
    if (camCta && toastEl) {
        var isFaceStep = /\/story\/face/.test(location.pathname);
        var emo = (location.search.match(/[?&]emo=(\w+)/) || [])[1];
        var tip = isFaceStep
            ? (FACE_TIP[emo] || FACE_TIP.sad)
            : '팔을 조금만 더 크게 움직여 볼까?';
        var tried = false;

        camCta.addEventListener('click', function (e) {
            if (tried) return;                    /* 두 번째부터는 그대로 넘어간다 */
            tried = true;
            e.preventDefault();
            toast(tip);
            speak(tip);                           /* 글을 못 읽는 아이도 있다 */
        });
    }
}());
