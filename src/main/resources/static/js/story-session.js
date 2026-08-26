(function () {
    'use strict';

    var KEY = 'kdStorySession';

    function load() {
        try {
            return JSON.parse(sessionStorage.getItem(KEY)) || null;
        } catch (e) {
            return null;
        }
    }

    function save(s) {
        try {
            sessionStorage.setItem(KEY, JSON.stringify(s));
        } catch (e) { }
    }

    function clear() {
        try {
            sessionStorage.removeItem(KEY);
        } catch (e) { }
    }

    var STAGE_BY_PATH = {
        '/story/scene': 'STORY',
        '/story/feel': 'MIND',
        '/story/feel-hint': 'MIND',
        '/story/why': 'CAUSE',
        '/story/face': 'EXPRESSION',
        '/story/act': 'ACTION',
        '/story/result': 'PRAISE'
    };

    var ORDER = ['STORY', 'MIND', 'CAUSE', 'EXPRESSION', 'ACTION', 'PRAISE'];

    function seqOf(stageType) {
        return ORDER.indexOf(stageType) + 1;
    }

    var path = location.pathname.replace(/\/$/, '');
    var stage = STAGE_BY_PATH[path] || null;

    if (!stage) {
        document.documentElement.dataset.kdNode = 'ready';
    }

    function post(url, body) {
        return fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body || {})
        }).then(function (res) {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            return res.json();
        });
    }

    /* ---------- 세션 ---------- */

    function startSession() {
        var emo = '';
        var note = '';
        try {
            emo = sessionStorage.getItem('kdDailyEmo') || '';
            note = sessionStorage.getItem('kdDailyNote') || '';
        } catch (e) { }

        if (!emo) {
            emo = (location.search.match(/[?&]emo=(\w+)/) || [])[1] || '';
        }

        return post('/api/story/sessions', {
            emotion: emo,
            dailyInput: note
        }).then(function (res) {
            try {
                sessionStorage.removeItem('kdDailyNote');
                sessionStorage.removeItem('kdDailyEmo');
            } catch (e) { }

            save({
                sessionId: res.sessionId,
                storySeq: res.storySeq,
                dailyGoal: res.dailyGoal,
                emotion: res.emotion,
                childCallName: res.childCallName,
                characterKey: res.characterKey,
                gesture: res.gesture,
                nodes: { STORY: res.node },
                results: [],
                finished: false
            });
            return res.node;
        });
    }

    function fetchNext(s, fromStage) {
        var done = firstResultOf(s, fromStage);

        return post('/api/story/sessions/' + s.sessionId + '/next', {
            stageType: fromStage,
            missionType: done ? done.missionType : null,
            responseValue: done ? done.responseValue : null,
            success: done ? done.success : null
        }).then(function (res) {
            var cur = load() || s;
            cur.nodes[res.node.stageType] = res.node;
            save(cur);
            return res.node;
        });
    }

    function firstResultOf(s, stageType) {
        var seq = seqOf(stageType);
        for (var i = 0; i < s.results.length; i++) {
            if (s.results[i].nodeOrder === seq) return s.results[i];
        }
        return null;
    }

    function record(stageType, missionType, responseValue, success) {
        var s = load();
        if (!s || !stageType) return;

        if (firstResultOf(s, stageType)) return;

        s.results.push({
            nodeOrder: seqOf(stageType),
            missionType: missionType,
            responseValue: String(responseValue == null ? '' : responseValue),
            success: !!success
        });
        save(s);
    }

    window.kdStory = window.kdStory || {};
    window.kdStory.record = record;
    window.kdStory.stage = stage;

    function finish(completed) {
        var s = load();
        if (!s || s.finished) return;

        s.finished = true;
        save(s);

        var url = '/api/story/sessions/' + s.sessionId + '/finish';
        var body = JSON.stringify({ completed: !!completed, results: s.results });

        var sent = false;
        if (navigator.sendBeacon) {
            sent = navigator.sendBeacon(url, new Blob([body], { type: 'application/json' }));
        }

        if (!sent) {
            fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: body,
                keepalive: true
            }).catch(function () { });
        }

        clear();
    }

    window.kdStory.finish = finish;

    function text(sel, value) {
        if (!value) return;
        var el = document.querySelector(sel);
        if (el) el.textContent = value;
    }

    var charKey = (load() || {}).characterKey || 'tori';

    function setChar(img, pose) {
        if (!img || !pose) return;

        img.onerror = function () {
            this.onerror = null;
            this.src = '/img/char-tori-' + pose + '.png';
        };
        img.setAttribute('data-kd-char', pose);
        img.src = '/img/char-' + charKey + '-' + pose + '.png';
    }

    function paintCount() {
        var s = load();
        if (!s || !s.storySeq) return;

        var label = '오늘 이야기 ' + s.storySeq + ' / ' + (s.dailyGoal || 3);

        document.querySelectorAll('.child-count, .pick-badge').forEach(function (el) {
            el.textContent = label;
        });
    }

    function paint(node) {
        paintCount();
        if (!node) return;

        switch (node.stageType) {

            case 'STORY':
                text('.story-title', node.title);
                text('.story-sub', node.narration);
                text('.story-ask', node.questionText);
                break;

            case 'MIND':
                text('.feel-recap', node.narration);
                text('.feel-q', node.title);
                paintCards('.feel-card', node);
                break;

            case 'CAUSE':
                text('.why-title', node.title);
                text('.why-sub', node.questionText);
                text('.why-hint', node.hintText);
                text('.story-modal .clue', node.coachText);
                paintCards('.why-card', node);
                break;

            case 'EXPRESSION':
            case 'ACTION':
                text('.cam-title', node.title);
                text('.cam-hint span', node.questionText);
                paintGuide(node);
                break;

            case 'PRAISE':
                text('.done-title', node.title);
                break;
        }

        paintPose(node.charPose);

        if (node.coachText) window.kdStory.coachText = node.coachText;
        if (node.hintText) window.kdStory.hintText = node.hintText;
    }

    var POSE_TARGETS = '.story-art img, .feel-art img, .cam-box .demo, .done-art img';

    function paintPose(pose) {
        if (!pose) return;

        document.querySelectorAll(POSE_TARGETS).forEach(function (img) {
            setChar(img, pose);
        });
    }

    function paintGuide(node) {

        var pose = node.targetValue;
        if (!pose) return;
        var steps = document.querySelector('.cam-hint-text');

        if (steps) {
            var how = HOW_TO[pose] || HOW_TO.comfort;

            text('.cam-hint-text .gt', how.title);

            var ol = steps.querySelector('.gs');
            if (ol) {
                ol.textContent = '';
                how.steps.forEach(function (line) {
                    var li = document.createElement('li');
                    li.textContent = line;
                    ol.appendChild(li);
                });
            }
            return;
        }

        setChar(document.querySelector('.cam-hint img'), pose);
    }

    var HOW_TO = {
        comfort: {
            title: '토닥토닥 해 볼까?',
            steps: ['한 손을 들어요', '친구 쪽으로 가까이', '위아래로 토닥토닥']
        },
        sorry: {
            title: '미안하다고 해 볼까?',
            steps: ['두 손을 가슴 앞에 모아요', '고개를 살짝 숙여도 좋아요']
        },
        celebrate: {
            title: '같이 축하해 볼까?',
            steps: ['손을 활짝 펴요', '두 손을 얼굴 위로 번쩍', '한 손만 들고 흔들어도 좋아요']
        },
        wave: {
            title: '손 흔들어 인사해 볼까?',
            steps: ['손을 활짝 펴요', '좌우로 흔들어요', '앉아서 해도 괜찮아요']
        }
    };

    function paintCards(selector, node) {
        var cards = document.querySelectorAll(selector);
        var options = node.options || [];
        if (!cards.length || !options.length) return;

        for (var i = 0; i < cards.length && i < options.length; i++) {
            var card = cards[i];
            var opt = options[i];

            var nm = card.querySelector('.nm');
            var ds = card.querySelector('.ds');
            if (nm && opt.label) nm.textContent = opt.label;
            if (ds && opt.description) ds.textContent = opt.description;

            card.setAttribute('data-kd-key', opt.key);

            card.removeAttribute('data-trait');

            setChar(card.querySelector('.ic'), opt.pose);

            if (opt.answer) {
                card.setAttribute('data-right', '');
                card.removeAttribute('data-wrong');
            } else {
                card.setAttribute('data-wrong', opt.key);
                card.removeAttribute('data-right');
            }
        }

        paintCardLinks(node, cards, options);
    }

    function paintCardLinks(node, cards, options) {

        var here = node.stageType === 'MIND' ? 'feel'
                 : node.stageType === 'CAUSE' ? 'why' : null;
        var next = node.stageType === 'MIND' ? 'why'
                 : node.stageType === 'CAUSE' ? 'face' : null;

        if (!here) return;

        var answerKey = null;
        for (var i = 0; i < options.length; i++) {
            if (options[i].answer) answerKey = options[i].key;
        }

        var cur = new URLSearchParams(location.search).get('emo') || 'sad';
        var emo = (node.stageType === 'MIND' && answerKey) ? answerKey : cur;

        for (var j = 0; j < cards.length && j < options.length; j++) {
            var opt = options[j];
            cards[j].setAttribute('href', opt.answer
                ? '/story/' + next + '?emo=' + encodeURIComponent(emo)
                : '/story/' + here + '?emo=' + encodeURIComponent(emo) +
                  '&wrong=' + encodeURIComponent(opt.key));
        }
    }

    function watchChoices() {
        if (stage !== 'MIND' && stage !== 'CAUSE') return;

        document.querySelectorAll('[data-right], [data-wrong]').forEach(function (card) {
            card.addEventListener('click', function () {
                var key = card.getAttribute('data-kd-key')
                    || card.getAttribute('data-wrong')
                    || 'cause';
                record(stage, 'CHOICE', key, card.hasAttribute('data-right'));
            });
        });
    }

    function watchQuit() {
        document.querySelectorAll('.story-quit .yes').forEach(function (btn) {
            btn.addEventListener('click', function () { finish(false); });
        });
    }

    function ready() {
        if (document.documentElement.dataset.kdNode === 'ready') return;
        document.documentElement.dataset.kdNode = 'ready';
        document.dispatchEvent(new CustomEvent('kd-node-ready'));
    }

    setTimeout(ready, 20000);

    /* ---------- 시작 ---------- */

    var PICK_ACT = {
        comfort:   { line: '따뜻하게 토닥여 주러 가볼까요?', go: '토닥토닥 해주기' },
        sorry:     { line: '미안한 마음을 전하러 가볼까요?', go: '미안하다고 말하기' },
        celebrate: { line: '함께 축하해주러 가볼까요?',      go: '함께 축하해주기' },
        wave:      { line: '먼저 다가가 인사해 볼까요?',      go: '손 흔들어 인사하기' }
    };

    function paintPick() {
        var s = load();
        if (!s) return;

        var act = PICK_ACT[s.gesture];
        if (!act) return;

        text('.pick-card .txt .ac', act.line);
        text('.pick-card .go', act.go);
    }

    function boot() {
        paintCount();

        if (path === '/story/situation') {
            paintPick();
            return;
        }

        if (!stage) return;

        watchChoices();
        watchQuit();

        if (stage === 'STORY') {
            clear();
            waiting(true);

            resumeOrStart().then(function () {
                waiting(false);
            }).catch(function (e) {
                console.warn('[kkeudeok] 세션을 시작하지 못해 학습 홈으로 되돌립니다', e);
                try { sessionStorage.setItem('kdStartFail', '1'); } catch (x) { }
                location.replace('/learn');
            });
            return;
        }

        var s = load();

        if (!s) {
            ready();
            return;
        }

        if (s.nodes[stage]) {
            paint(s.nodes[stage]);
            ready();
            prefetch(stage);
        } else {
            fetchNext(s, prevStageOf(stage)).then(function (node) {
                paint(node);
                ready();
                prefetch(stage);
            }).catch(function (e) {
                console.warn('[kkeudeok] 다음 이야기를 받지 못했습니다', e);
                ready();
            });
        }

        if (stage === 'PRAISE') finish(true);
    }

    function waiting(on) {

        if (on) {
            try { speechSynthesis.cancel(); } catch (e) { }
            return;
        }

        ready();
    }

    function resumeOrStart() {
        var hasNote = false;
        try {
            hasNote = !!(sessionStorage.getItem('kdDailyNote')
                    || sessionStorage.getItem('kdDailyEmo'));
        } catch (e) { }

        if (hasNote) {
            return fresh();
        }

        return fetch('/api/story/sessions/resume')
            .then(function (res) { return res.ok ? res.json() : { found: false }; })
            .catch(function () { return { found: false }; })
            .then(function (r) {
                if (!r.found) return fresh();

                save({
                    sessionId: r.sessionId,
                    storySeq: r.storySeq,
                    dailyGoal: r.dailyGoal,
                    childCallName: r.childCallName,
                    characterKey: r.characterKey,
                    gesture: r.gesture,
                    nodes: nodeMapOf(r.node),
                    results: [],
                    finished: false
                });

                charKey = r.characterKey || charKey;

                if (!r.resumeScreen || r.resumeScreen === 'scene') {
                    paint(r.node);
                    prefetch('STORY');
                    return;
                }

                location.replace('/story/' + r.resumeScreen);
            });
    }

    function fresh() {
        return startSession().then(function (node) {
            charKey = (load() || {}).characterKey || charKey;
            paint(node);
            prefetch('STORY');
        });
    }

    function nodeMapOf(node) {
        var m = {};
        if (node && node.stageType) m[node.stageType] = node;
        return m;
    }

    function prefetch(fromStage) {
        var s = load();
        if (!s || fromStage === 'PRAISE') return;

        var nextStage = ORDER[ORDER.indexOf(fromStage) + 1];
        if (!nextStage || s.nodes[nextStage]) return;

        fetchNext(s, fromStage).catch(function () { });
    }

    function prevStageOf(cur) {
        return ORDER[Math.max(0, ORDER.indexOf(cur) - 1)];
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boot);
    } else {
        boot();
    }
}());
