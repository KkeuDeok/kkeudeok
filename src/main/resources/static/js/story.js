(function () {
    'use strict';

    var dim = document.querySelector('.story-dim');

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

    function clearPicks() {
        document.querySelectorAll('.feel-card.is-pick, .why-card.is-pick')
            .forEach(function (el) { el.classList.remove('is-pick'); });
    }

    function replay(el) {
        el.classList.remove('is-on');
        void el.offsetWidth;
        el.classList.add('is-on');
    }

    function openWrong(pickEl) {
        if (!dim) return false;

        paintTrait(pickEl);
        clearPicks();
        if (pickEl) pickEl.classList.add('is-pick');

        dim.hidden = false;
        replay(dim);

        var btn = dim.querySelector('.ft a');
        if (btn) btn.focus();
        return true;
    }

    function closeWrong() {
        if (!dim) return;
        dim.classList.remove('is-on');
        dim.hidden = true;
        clearPicks();
    }

    if (dim) {
        dim.querySelectorAll('[data-close]').forEach(function (b) {
            b.addEventListener('click', function (e) { e.preventDefault(); closeWrong(); });
        });

        dim.addEventListener('click', function (e) { if (e.target === dim) closeWrong(); });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && !dim.hidden) closeWrong();
        });
    }

    var mic = document.querySelector('.why-mic');
    var listen = document.querySelector('.why-listen');
    if (mic && listen) {
        mic.addEventListener('click', function (e) {
            e.preventDefault();
            var on = listen.hidden;
            listen.hidden = !on;
            mic.hidden = on;
        });
        listen.addEventListener('click', function () {
            listen.hidden = true;
            mic.hidden = false;
        });
    }

    function cheer(card, href) {
        card.classList.add('is-right');

        var tag = document.createElement('span');
        tag.className = 'kd-yay';
        tag.textContent = '맞았어요!';
        card.appendChild(tag);

        document.querySelectorAll('.feel-card, .why-card').forEach(function (el) {
            if (el !== card) el.classList.add('is-dim');
            el.style.pointerEvents = 'none';
        });

        setTimeout(function () { location.href = href; }, 1100);
    }

    document.addEventListener('click', function (e) {
        var card = e.target.closest ? e.target.closest('.feel-card, .why-card') : null;
        if (!card) return;

        if (card.hasAttribute('data-right')) {
            e.preventDefault();
            cheer(card, card.getAttribute('href'));
            return;
        }

        if (card.hasAttribute('data-wrong')) {
            if (openWrong(card)) e.preventDefault();
        }
    });

    var SAY_SELECTORS = ['.story-title', '.story-sub', '.story-ask',
                         '.feel-recap', '.feel-q', '.feel-hint',
                         '.why-title', '.why-sub', '.why-hint', '.cam-title',
                         '.done-title'];

    var STEP_PATHS = ['/story/scene', '/story/feel', '/story/feel-hint',
                      '/story/why', '/story/face', '/story/act', '/story/result'];

    var isStep = STEP_PATHS.indexOf(location.pathname.replace(/\/$/, '')) >= 0;

    var AUDIO_BASE = '/audio/story/';
    var AUDIO_V = '?v=222';

    var hasTTS = 'speechSynthesis' in window;
    var canSay = hasTTS || !!window.KD_AUDIO;
    var playing = null;
    var clearMark = null;

    function screenText() {
        var out = [];
        SAY_SELECTORS.forEach(function (sel) {
            document.querySelectorAll(sel).forEach(function (el) {
                if (el.hidden) return;
                var t = (el.textContent || '').trim();
                if (t && out.indexOf(t) === -1) out.push(t);
            });
        });
        return out;
    }

    function clipFor(text) { return window.KD_AUDIO ? window.KD_AUDIO[text] : null; }

    function forSpeech(parts) {
        var out = [];

        for (var i = 0; i < parts.length; i++) {
            var t = String(parts[i])
                .replace(/…+/g, '.')
                .replace(/[~·∙•―—–]+/g, ' ')
                .replace(/\.{2,}/g, '.')
                .replace(/\s+/g, ' ')
                .trim();

            if (!t) continue;
            if (!/[.!?]$/.test(t)) t += '.';

            out.push(t);
        }

        return out.join(' ');
    }

    function isSpeaking() {
        return !!playing || (hasTTS && (speechSynthesis.speaking || speechSynthesis.pending));
    }

    function stopSpeaking() {
        if (playing) { playing.pause(); playing = null; }
        if (hasTTS) { stopKeepAlive(); speechSynthesis.cancel(); }
        if (clearMark) { clearMark(); clearMark = null; }
    }

    var keepAlive = null;

    function stopKeepAlive() {
        if (keepAlive) { clearInterval(keepAlive); keepAlive = null; }
    }

    function startKeepAlive(v) {
        stopKeepAlive();

        if (!v || v.localService !== false) return;

        keepAlive = setInterval(function () {
            if (!speechSynthesis.speaking) { stopKeepAlive(); return; }
            speechSynthesis.resume();
        }, 8000);
    }

    /* 한국어 여자 목소리 우선순위. 순서를 바꾸면 아이가 듣는 목소리가 바뀐다. */
    var FEMALE_RANK = [
        /SunHi/i,                            /* Edge · Azure 신경망 */
        /(JiMin|SeoHyeon|YuJin|SoonBok)/i,   /* 같은 계열의 다른 여자 목소리 */
        /Google.*(한국|Korean)/i,            /* Chrome 원격 */
        /Heami/i,                            /* Windows 기본 */
        /Yuna|유나/i                         /* macOS · iOS */
    ];

    /* ⚠ 남자 목소리다. 자연스럽다는 이유로 뽑으면 화면마다 성별이 바뀐다. */
    var MALE = /(InJoon|BongJin|GookMin|Hyunsu|Minsik)/i;

    var NATURAL = /(Natural|Neural|Online)/i;

    var PIN_KEY = 'kdVoicePinned2';

    var RATE = 1;
    var PITCH = 1.2;
    var PLAIN_PITCH = 1;

    function readLS(key) {
        try { return localStorage.getItem(key) || ''; } catch (e) { return ''; }
    }

    function writeLS(key, value) {
        try { localStorage.setItem(key, value); } catch (e) { }
    }

    function voiceScore(v) {
        if (!/^ko/i.test(v.lang || '')) return -1;

        var name = v.name || '';

        if (MALE.test(name)) return -1;

        var rank = 0;

        for (var i = 0; i < FEMALE_RANK.length; i++) {
            if (FEMALE_RANK[i].test(name)) { rank = FEMALE_RANK.length - i; break; }
        }

        var tier = NATURAL.test(name) ? 3 : (v.localService === false ? 2 : 1);

        return tier * 10 + rank;
    }

    function pitchFor(v) {
        return v && NATURAL.test(v.name || '') ? PITCH : PLAIN_PITCH;
    }

    function byName(list, lower) {
        for (var i = 0; i < list.length; i++) {
            if ((list[i].name || '').toLowerCase().indexOf(lower) >= 0) return list[i];
        }
        return null;
    }

    var koVoice = null;

    /* 한 번 고른 목소리를 기억한다 — 화면마다 다시 고르면 목록이 준비된 정도에 따라
       그때그때 다른 목소리가 뽑혀 이야기 중간에 성별이 바뀐다. */
    function pickVoice() {
        var vs = speechSynthesis.getVoices();
        if (!vs.length) return;

        var wanted = readLS('kdVoice').toLowerCase();

        if (wanted) {
            var manual = byName(vs, wanted);
            if (manual) { koVoice = manual; return; }
        }

        var kept = readLS(PIN_KEY);

        if (kept) {
            var same = byName(vs, kept.toLowerCase());
            if (same && voiceScore(same) > 0) { koVoice = same; return; }
        }

        var best = null;
        var bestScore = 0;

        for (var i = 0; i < vs.length; i++) {
            var s = voiceScore(vs[i]);
            if (s > bestScore) { bestScore = s; best = vs[i]; }
        }

        koVoice = best;

        if (best) writeLS(PIN_KEY, best.name);
    }

    /* 목록이 아직 안 왔는데 말하면 브라우저 기본 목소리(남자일 수 있다)로 나간다.
       첫 문장이 그렇게 새는 것을 막는다. */
    function whenVoices(fn) {
        if (!hasTTS || speechSynthesis.getVoices().length) { fn(); return; }

        var fired = false;

        function go() {
            if (fired) return;
            fired = true;
            speechSynthesis.removeEventListener('voiceschanged', go);
            fn();
        }

        speechSynthesis.addEventListener('voiceschanged', go);
        setTimeout(go, 1500);
    }

    window.kdVoices = function () {
        return speechSynthesis.getVoices()
            .filter(function (v) { return /^ko/i.test(v.lang || ''); })
            .map(function (v) {
                return v.name + '  [' + v.lang + (v.localService ? ' · 로컬' : ' · 온라인') + ']'
                    + (koVoice && v.name === koVoice.name ? '  ← 지금 쓰는 목소리' : '');
            });
    };

    window.kdVoiceReset = function () {
        try {
            localStorage.removeItem(PIN_KEY);
            localStorage.removeItem('kdVoice');
        } catch (e) { }

        koVoice = null;
        pickVoice();

        return koVoice ? koVoice.name : '(고르지 못함)';
    };

    if (hasTTS) {
        pickVoice();
        speechSynthesis.addEventListener('voiceschanged', pickVoice);
    }

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

    function speak(text, el, label) {
        if (!canSay) return;

        var parts = (typeof text === 'string' ? [text] : (text || [])).filter(Boolean);
        if (!parts.length) return;

        stopSpeaking();

        var clips = [];
        for (var i = 0; i < parts.length; i++) {
            var c = clipFor(parts[i]);
            if (!c) return sayWithBrowser(forSpeech(parts), el, label);
            clips.push(c);
        }

        var done = mark(el, label);
        var joined = forSpeech(parts);
        var n = 0;

        function fallback() { playing = null; done(); sayWithBrowser(joined, el, label); }

        function next() {
            if (n >= clips.length) { playing = null; done(); return; }

            var a = new Audio(AUDIO_BASE + clips[n++] + AUDIO_V);
            playing = a;
            a.onended = next;
            a.onerror = fallback;

            var p = a.play();
            if (p && p['catch']) p['catch'](fallback);
        }

        next();
    }

    function sayWithBrowser(text, el, label) {
        if (!hasTTS) return;

        var done = mark(el, label);

        whenVoices(function () {
            pickVoice();

            var u = new SpeechSynthesisUtterance(text);
            u.lang = 'ko-KR';

            if (koVoice) u.voice = koVoice;

            u.rate = tuned('kdRate', RATE);
            u.pitch = tuned('kdPitch', pitchFor(koVoice));

            u.onend = u.onerror = function () { stopKeepAlive(); done(); };

            speechSynthesis.cancel();
            setTimeout(function () {
                speechSynthesis.speak(u);
                startKeepAlive(koVoice);
            }, 120);
        });
    }

    function tuned(key, fallback) {
        var raw;
        try { raw = localStorage.getItem(key); } catch (e) { return fallback; }

        var n = parseFloat(raw);
        return isNaN(n) ? fallback : n;
    }

    var listenBtns = document.querySelectorAll('.kd-sub-listen');

    if (listenBtns.length && canSay) {
        listenBtns.forEach(function (btn) {
            var label = btn.textContent;
            btn.addEventListener('click', function () {
                if (isSpeaking()) {
                    stopSpeaking();
                    btn.textContent = label;
                    btn.classList.remove('is-speaking');
                    return;
                }
                speak(screenText(), btn, label);
            });
        });
    }

    if (canSay && isStep) {

        if (hasTTS) speechSynthesis.cancel();

        window.addEventListener('pagehide', function () { stopSpeaking(); });

        var first = listenBtns[0] || null;
        var firstLabel = first ? first.textContent : null;

        function sayScreen() { speak(screenText(), first, firstLabel); }

        function afterReady(fn) {
            if (document.documentElement.dataset.kdNode !== 'pending') {
                fn();
                return;
            }

            var fired = false;

            function go() {
                if (fired) return;
                fired = true;
                document.removeEventListener('kd-node-ready', go);
                fn();
            }

            document.addEventListener('kd-node-ready', go);
            setTimeout(go, 5000);
        }

        afterReady(function () {
            sayScreen();

            setTimeout(function () {
                if (isSpeaking()) return;
                document.addEventListener('pointerdown', function once() {
                    document.removeEventListener('pointerdown', once);
                    sayScreen();
                });
            }, 500);
        });
    }

    document.querySelectorAll('.feel-card .spk, .why-card .spk').forEach(function (sp) {
        sp.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();

            var card = sp.closest('.feel-card, .why-card');
            var words = [];

            if (card) {
                card.querySelectorAll('.nm, .ds').forEach(function (p) {
                    words.push((p.textContent || '').trim());
                });
            }

            speak(words, sp);
        });
    });

    function hintTarget() {
        return document.querySelector('.cam-hint')
            || document.querySelector('.story-cta');
    }

    document.querySelectorAll('button.kd-sub-hint').forEach(function (btn) {
        btn.addEventListener('click', function () {
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

    var ask = document.querySelector('.story-quit');

    function openAsk() {
        if (!ask) return false;

        ask.hidden = false;
        replay(ask);

        var no = ask.querySelector('[data-ask-no]');
        if (no) no.focus();
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

    if (document.querySelector('.home-card') || location.pathname === '/story/scene') {
        try { sessionStorage.removeItem('kdCounted'); } catch (e) { }
    }

    var doneStage = document.querySelector('.done-stage');

    if (doneStage) {
        try {
            if (!sessionStorage.getItem('kdCounted')) {
                sessionStorage.setItem('kdCounted', '1');
                localStorage.setItem('kdDone', String((+(localStorage.getItem('kdDone') || 0) || 0) + 1));
            }
        } catch (e) { }

        doneStage.addEventListener('click', function () {
            location.href = doneStage.getAttribute('data-home');
        });
    }
}());
