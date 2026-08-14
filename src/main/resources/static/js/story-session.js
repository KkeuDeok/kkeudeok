/* AI 인터랙티브 스토리 학습 — 세션 연동.
 *
 * 하는 일 네 가지.
 *   1. 학습을 시작할 때 서버에서 세션과 첫 노드를 받아 온다
 *   2. 화면 글자를 AI 가 만든 문장으로 갈아끼운다
 *   3. 아이가 한 미션 결과를 **로컬에 쌓는다** (서버를 부르지 않는다)
 *   4. 학습이 끝날 때 쌓아 둔 배열을 **한 번에** 보낸다
 *
 * ⚠ 미션마다 서버를 부르지 않는 이유
 *   아이가 카드를 누를 때마다 통신이 끼면 화면이 멈칫한다. 아이 입장에서 그건
 *   '눌렀는데 아무 일도 안 일어남' 이고, 실제로 같은 이유로 오답 처리를 서버 왕복에서
 *   그 자리 모달로 바꾼 적이 있다(story.js 주석 참고). 통신은 시작과 끝, 두 번뿐이다.
 *
 * ⚠ 서버가 죽어도 학습은 돌아간다
 *   세션을 못 만들면 JSP 가 그린 기본 문구(내장 시나리오)가 그대로 남는다.
 *   글자만 못 갈아끼울 뿐 6단계는 끝까지 진행된다.
 *
 * story.js 와 역할이 갈린다 — 저쪽은 화면 동작(모달·낭독·애니메이션), 이쪽은 데이터다.
 */
(function () {
    'use strict';

    /* ---------- 저장소 ---------- */

    var KEY = 'kdStorySession';   /* 세션 한 건 (sessionStorage — 탭을 닫으면 사라진다) */

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

    /* ---------- 화면 ↔ 단계 ----------
       한 단계가 화면 두 개인 경우가 있다(마음 읽기 = feel + feel-hint).
       상황 선택(situation)은 단계가 아니라 표정과 행동 사이의 안내 화면이다. */
    var STAGE_BY_PATH = {
        '/story/scene': 'STORY',
        '/story/feel': 'MIND',
        '/story/feel-hint': 'MIND',
        '/story/why': 'CAUSE',
        '/story/face': 'EXPRESSION',
        '/story/act': 'ACTION',
        '/story/result': 'PRAISE'
    };

    var SEQ = { STORY: 1, MIND: 2, CAUSE: 3, EXPRESSION: 4, ACTION: 5, PRAISE: 6 };

    var path = location.pathname.replace(/\/$/, '');
    var stage = STAGE_BY_PATH[path] || null;

    /* ---------- 통신 ----------
       아이 화면이라 실패해도 조용히 넘어간다. 화면에 오류를 띄우지 않는다. */
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

    /* ---------- 세션 시작 ----------
       이야기 화면(/story/scene)에 들어온 순간이 한 편의 시작이다.
       ⚠ 아동홈(/story/home)이 아니다 — 학습 홈에서 곧바로 scene 으로 들어오는 길이 있어
         아동홈을 안 거치는 경우가 많다(story.js 가 같은 이유로 여기서 카운터를 지운다). */
    function startSession() {
        var emo = '';
        var note = '';
        try {
            emo = sessionStorage.getItem('kdDailyEmo') || '';
            note = sessionStorage.getItem('kdDailyNote') || '';
        } catch (e) { }

        /* 일상 입력을 건너뛰고 바로 시작한 경우 — 주소의 ?emo= 를 쓴다.
           그것도 없으면 서버가 기본값(슬픔)으로 만든다. */
        if (!emo) {
            emo = (location.search.match(/[?&]emo=(\w+)/) || [])[1] || '';
        }

        return post('/api/story/sessions', {
            emotion: emo,
            dailyInput: note
            /* childId 는 보내지 않는다 — 로그인이 붙기 전까지 서버가 테스트 아동을 쓴다 */
        }).then(function (res) {
            save({
                sessionId: res.sessionId,
                storySeq: res.storySeq,
                dailyGoal: res.dailyGoal,
                emotion: res.emotion,
                childCallName: res.childCallName,
                characterKey: res.characterKey,
                nodes: { STORY: res.node },
                results: [],
                finished: false
            });
            return res.node;
        });
    }

    /* ---------- 다음 노드 ----------
       직전 단계에서 아이가 무엇을 했는지 실어 보낸다. 그 반응을 근거로 이야기가 이어진다.
       ⚠ 여기서 보낸 반응은 저장되지 않는다. 저장은 끝날 때 한 번에 한다. */
    function fetchNext(s, fromStage) {
        var done = firstResultOf(s, fromStage);

        return post('/api/story/sessions/' + s.sessionId + '/next', {
            stageType: fromStage,
            missionType: done ? done.missionType : null,
            responseValue: done ? done.responseValue : null,
            success: done ? done.success : null
        }).then(function (res) {
            var cur = load() || s;                 /* 그 사이 결과가 쌓였을 수 있다 */
            cur.nodes[res.node.stageType] = res.node;
            save(cur);
            return res.node;
        });
    }

    function firstResultOf(s, stageType) {
        var seq = SEQ[stageType];
        for (var i = 0; i < s.results.length; i++) {
            if (s.results[i].nodeOrder === seq) return s.results[i];
        }
        return null;
    }

    /* ---------- 결과 쌓기 ----------
       ⚠ 첫 시도만 남긴다. 지금 화면은 틀리면 넘어가지 않고 다시 고르게 돼 있어서,
         마지막 결과로 남기면 전부 성공이 되어 성장 리포트가 무의미해진다.
         (2026-08-12 확정 — 재시도 횟수는 저장하지 않기로 했다) */
    function record(stageType, missionType, responseValue, success) {
        var s = load();
        if (!s || !stageType) return;

        if (firstResultOf(s, stageType)) return;   /* 이미 첫 시도가 있다 */

        s.results.push({
            nodeOrder: SEQ[stageType],
            missionType: missionType,
            responseValue: String(responseValue == null ? '' : responseValue),
            success: !!success
        });
        save(s);
    }

    /* 밖에서도 쓴다 — kd-mediapipe.js 가 표정·동작 판정 결과를 여기로 넘긴다 */
    window.kdStory = window.kdStory || {};
    window.kdStory.record = record;
    window.kdStory.stage = stage;

    /* ---------- 세션 종료 ----------
       칭찬 화면까지 왔으면 completed, 중간에 나갔으면 abandoned.
       ⚠ sendBeacon 을 쓰는 이유: [그만할래] 는 곧바로 다른 주소로 이동한다.
         일반 fetch 는 이동하는 순간 취소돼 결과가 통째로 날아간다. */
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

    /* ---------- 화면 글자 갈아끼우기 ----------
       JSP 가 그려 둔 기본 문구 위에 AI 문장을 덮는다. 못 받아 오면 기본 문구가 남는다. */
    function text(sel, value) {
        if (!value) return;
        var el = document.querySelector(sel);
        if (el) el.textContent = value;
    }

    /* "오늘 이야기 1 / 3" — JSP 는 1/3 로 박아 뒀다. 실제 순번으로 갈아끼운다.
       하루 세 편 권장이라 오른쪽 숫자는 서버가 주는 dailyGoal 을 쓴다. */
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
                paintCards('.why-card', node);
                break;

            case 'EXPRESSION':
            case 'ACTION':
                text('.cam-title', node.title);
                text('.cam-hint span', node.questionText);
                break;

            case 'PRAISE':
                text('.done-title', node.title);
                text('.done-sub', node.narration);
                break;
        }

        /* 표정·동작 코칭 문구는 story.js 가 토스트로 띄운다. 서버 문구로 갈아끼운다. */
        if (node.coachText) window.kdStory.coachText = node.coachText;
        if (node.hintText) window.kdStory.hintText = node.hintText;
    }

    /* 카드 글자를 갈아끼운다.
       ⚠ 카드의 정답/오답 표시(data-right · data-wrong)와 순서는 건드리지 않는다.
         JSP 가 이미 정답 자리를 잡아 뒀고, 서버도 같은 키 순서로 내려 준다.
         여기서 순서를 바꾸면 story.js 의 클릭 처리와 어긋난다. */
    function paintCards(selector, node) {
        var cards = document.querySelectorAll(selector);
        var options = node.options || [];
        if (!cards.length || !options.length) return;

        var s = load();
        var charKey = (s && s.characterKey) || 'tori';

        for (var i = 0; i < cards.length && i < options.length; i++) {
            var card = cards[i];
            var opt = options[i];

            var nm = card.querySelector('.nm');
            var ds = card.querySelector('.ds');
            if (nm && opt.label) nm.textContent = opt.label;
            if (ds && opt.description) ds.textContent = opt.description;

            /* 어떤 키를 고른 것인지 결과에 남기고, 카드 색도 이걸로 칠해진다
               (child.css 의 .feel-card[data-kd-key=...]). 색을 클래스로도 따로 맞추던 코드는
               지웠다 — 두 군데를 맞추다 어긋나면 기뻐요가 화나요 색으로 나왔다(2026-08-14). */
            card.setAttribute('data-kd-key', opt.key);

            /* ⚠ JSP 가 박아 둔 오답 문구는 지운다. 카드가 바뀌었는데 그대로 두면
                 놀랐어요 카드를 눌렀는데 "기쁠 때는…" 이 뜬다.
                 지우면 story.js 가 키로 알맞은 문구를 찾는다. */
            card.removeAttribute('data-trait');

            /* 카드 그림 — 키가 곧 포즈다. 없는 파일이면 토리 것으로 떨어뜨린다. */
            var ic = card.querySelector('.ic');
            if (ic && opt.pose) {
                ic.onerror = function () {
                    this.onerror = null;
                    this.src = '/img/char-tori-' + this.getAttribute('data-kd-char') + '.png';
                };
                ic.setAttribute('data-kd-char', opt.pose);
                ic.src = '/img/char-' + charKey + '-' + opt.pose + '.png';
            }

            /* ⚠ 정답 자리를 옮긴다.
               마음 읽기는 AI 가 카드 조합을 정하므로(2026-08-13) JSP 가 그려 둔 정답 자리와
               다를 수 있다. 안 옮기면 아이가 맞게 골라도 오답 모달이 뜬다.
               story.js 는 누르는 순간의 attribute 를 보도록 고쳐 뒀다. */
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

    /* 카드가 눌렸을 때 갈 주소를 다시 적는다.
       ⚠ 이게 없으면 정답을 눌러도 오답 모달이 뜬다(2026-08-14 지적).
         JSP 는 주소의 ?emo= 를 정답으로 믿고 카드마다 href 를 구워 둔다. 그런데 실제 정답은
         AI 가 정하고 자리도 섞이므로(shuffle), 정답 카드에 오답용 href 가 박혀 있게 된다.
         story.js 는 정답이라고 칭찬해 준 뒤 그 href 로 보내니 곧장 오답 화면이 열린다.
         data-right 만 옮기고 href 를 안 옮긴 것이 원인이었다. */
    function paintCardLinks(node, cards, options) {

        var here = node.stageType === 'MIND' ? 'feel'
                 : node.stageType === 'CAUSE' ? 'why' : null;
        var next = node.stageType === 'MIND' ? 'why'
                 : node.stageType === 'CAUSE' ? 'face' : null;

        if (!here) return;      // 카드가 없는 단계

        var answerKey = null;
        for (var i = 0; i < options.length; i++) {
            if (options[i].answer) answerKey = options[i].key;
        }

        /* 마음 읽기의 정답 키가 곧 이 이야기의 감정이다 — 뒤 화면들이 ?emo= 로 그림·문구를 고른다.
           주소에 실려 온 emo 는 앞 화면이 넘긴 값이라 AI 가 고른 감정과 다를 수 있다.
           이유 찾기(cause/other)는 감정이 아니므로 지금 emo 를 그대로 물려준다. */
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

    /* ---------- 미션 결과 잡기 ----------
       카드 클릭(CHOICE)은 여기서 잡는다. 표정·동작은 kd-mediapipe.js 가 넘긴다.
       story.js 도 같은 요소를 듣지만 서로 방해하지 않는다 — 저쪽은 화면, 이쪽은 기록이다. */
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

    /* ---------- 나가기 ----------
       [그만할래] · [이 동작은 하기 싫어] → 중도 이탈로 결과를 남기고 끝낸다. */
    function watchQuit() {
        document.querySelectorAll('.story-quit .yes').forEach(function (btn) {
            btn.addEventListener('click', function () { finish(false); });
        });
    }

    /* ---------- 시작 ---------- */
    function boot() {
        /* 상황 선택처럼 단계가 아닌 화면도 진도 배지는 맞춰 준다 */
        paintCount();

        if (!stage) return;                        /* 아동홈·상황 선택은 받아 올 노드가 없다 */

        watchChoices();
        watchQuit();

        var s = load();

        /* 이야기 화면 = 한 편의 시작.
           ⚠ 곧바로 새로 만들지 않는다. 오늘 그만둔 학습이 있으면 그걸 잇는다 —
             화면이 "지금까지 한 건 남아 있어요. 다음에 이어서 하면 돼요" 라고 약속했다. */
        if (stage === 'STORY') {
            clear();
            waiting(true);

            resumeOrStart().then(function () {
                waiting(false);
            }).catch(function (e) {
                waiting(false);
                /* 서버가 없거나 죽었다 — JSP 기본 문구로 그대로 진행한다 */
                console.warn('[kkeudeok] 세션을 시작하지 못해 내장 시나리오로 진행합니다', e);
            });
            return;
        }

        if (!s) return;                            /* 주소로 바로 들어온 경우 — 기본 문구 유지 */

        /* 미리 받아 둔 노드가 있으면 곧바로 그린다(대기 0초).
           없으면 지금 받아 온다 — 아이가 잠깐 기본 문구를 보다가 바뀐다. */
        if (s.nodes[stage]) {
            paint(s.nodes[stage]);
            prefetch(stage);
        } else {
            var prev = prevStageOf(stage);
            fetchNext(s, prev).then(function (node) {
                paint(node);
                prefetch(stage);
            }).catch(function (e) {
                console.warn('[kkeudeok] 다음 이야기를 받지 못했습니다', e);
            });
        }

        /* 칭찬 화면까지 왔다 = 한 편을 끝까지 마쳤다.
           ⚠ 곧바로 보낸다. 화면을 누르면 대시보드로 나가 버려 그때는 늦다. */
        if (stage === 'PRAISE') finish(true);
    }

    /* ---------- 이야기를 만드는 동안 ----------
       AI 가 이야기를 만드는 데 10초 남짓 걸린다. 그동안 JSP 가 그려 둔 내장 문구가 보이는데,
       story.js 가 화면에 들어오자마자 그걸 소리로 읽기 시작한다. 그러다 AI 문장이 도착해
       글자만 바뀌면 **읽던 것과 화면이 어긋난다** — 아이 입장에서 가장 혼란스러운 상황이다.

       그래서 만드는 동안에는 낭독을 멈추고 '준비 중'을 보여 준 뒤, 문장이 도착하면
       그때 처음부터 읽어 준다. */
    function waiting(on) {

        var main = document.querySelector('.child-main');
        if (!main) return;

        if (on) {
            try { speechSynthesis.cancel(); } catch (e) { }
            injectWaitStyle();
            main.classList.add('kd-waiting');
            dots(main, true);
            return;
        }

        main.classList.remove('kd-waiting');
        dots(main, false);

        /* 도착했으니 이제 읽어 준다 — [다시 들려줘] 를 눌러 story.js 의 낭독을 그대로 쓴다.
           ⚠ 브라우저 자동재생 정책에 막히면 조용히 넘어간다. 그때는 아이가 화면을
             처음 누를 때 story.js 가 대신 읽어 준다. */
        var btn = document.querySelector('.kd-sub-listen');
        if (btn) {
            setTimeout(function () { btn.click(); }, 150);
        }
    }

    /* 기다리는 동안 점 세 개.
       글자를 다 감춰 두면 10초 동안 화면이 멈춘 것처럼 보인다 — 아이도 보호자도
       "고장 났나?" 하고 뒤로 나가 버린다. 글자가 있던 자리(title 454)에 점만 놓아
       '지금 만드는 중' 을 보여 준다. 글씨를 다시 넣지 않는 이유는 2026-08-14 판단과 같다.
       ⚠ 점은 왔다갔다 튀지 않고 차례로 숨쉬듯 밝아지기만 한다 — 아이 화면 원칙(예측 가능). */
    function dots(main, on) {
        var el = main.querySelector('.kd-wait-dots');

        if (!on) {
            if (el) el.remove();
            return;
        }
        if (el) return;

        el = document.createElement('div');
        el.className = 'kd-wait-dots';
        el.setAttribute('aria-hidden', 'true');   /* 읽어 줄 내용이 아니다 */
        el.innerHTML = '<i></i><i></i><i></i>';
        main.appendChild(el);
    }

    function injectWaitStyle() {
        if (document.getElementById('kdWaitStyle')) return;

        var st = document.createElement('style');
        st.id = 'kdWaitStyle';
        st.textContent =
            /* 글자는 감추고 자리는 남긴다 — 문장이 도착할 때 화면이 덜컥 움직이지 않는다.
               "이야기를 만드는 중이에요" 안내는 걷어냈다(2026-08-14). 학습하기로 들어오자마자
               큰 글씨로 떠서 화면이 어수선했고, 어차피 배경과 캐릭터는 이미 그려져 있어
               잠깐 조용히 기다리는 편이 낫다. 자리를 남기는 규칙은 그대로 둔다. */
            '.child-main.kd-waiting .story-title,.child-main.kd-waiting .story-sub,' +
            '.child-main.kd-waiting .story-ask,.child-main.kd-waiting .story-cta{opacity:0}' +

            /* 점 세 개 — 제목 자리(454)에 맞춘 디자인 좌표. 색은 학습 흐름 주색을 그대로 쓴다. */
            '.kd-wait-dots{position:absolute;left:0;top:468px;width:100%;' +
            'display:flex;justify-content:center;gap:16px;pointer-events:none}' +
            '.kd-wait-dots i{width:16px;height:16px;border-radius:50%;' +
            'background:var(--kd-primary);opacity:.25;' +
            'animation:kd-wait-dot 1.5s ease-in-out infinite}' +
            '.kd-wait-dots i:nth-child(2){animation-delay:.25s}' +
            '.kd-wait-dots i:nth-child(3){animation-delay:.5s}' +
            '@keyframes kd-wait-dot{0%,100%{opacity:.25}50%{opacity:1}}' +

            /* 움직임을 줄여 달라고 한 기기에서는 켜져만 있게 둔다 */
            '@media (prefers-reduced-motion:reduce){' +
            '.kd-wait-dots i{animation:none;opacity:.6}}';

        document.head.appendChild(st);
    }

    /* ---------- 이어하기 ----------
       오늘 그만둔 학습이 있으면 그 화면으로 보내고, 없으면 새 이야기를 시작한다.

       ⚠ 오늘 것만 잇는다(서버가 그렇게 고른다). 며칠 지난 이야기를 다시 꺼내면
         아이가 무슨 이야기였는지 기억하지 못해 오히려 헷갈린다.
       ⚠ 앞서 한 미션 결과는 그만둘 때 이미 서버에 저장됐다. 그래서 로컬 results 는
         비운 채로 이어도 된다 — 서버가 이번에 보낸 노드만 지우고 넣기 때문에
         앞부분 기록이 덮이지 않는다. */
    function resumeOrStart() {
        return fetch('/api/story/sessions/resume')
            .then(function (res) { return res.ok ? res.json() : { found: false }; })
            .catch(function () { return { found: false }; })
            .then(function (r) {
                if (!r.found) {
                    return startSession().then(function (node) {
                        paint(node);
                        prefetch('STORY');
                    });
                }

                save({
                    sessionId: r.sessionId,
                    storySeq: r.storySeq,
                    dailyGoal: r.dailyGoal,
                    childCallName: r.childCallName,
                    characterKey: r.characterKey,
                    nodes: nodeMapOf(r.node),
                    results: [],
                    finished: false
                });

                /* 이미 이야기 화면이면 그대로 그리고, 아니면 하던 화면으로 보낸다 */
                if (!r.resumeScreen || r.resumeScreen === 'scene') {
                    paint(r.node);
                    prefetch('STORY');
                    return;
                }

                location.replace('/story/' + r.resumeScreen);
            });
    }

    function nodeMapOf(node) {
        var m = {};
        if (node && node.stageType) m[node.stageType] = node;
        return m;
    }

    /* 다음 화면을 미리 받아 둔다 — 아이가 지금 화면을 보고 있는 동안 통신이 끝난다.
       이게 없으면 화면을 넘길 때마다 2~4초씩 멈춘다. */
    function prefetch(fromStage) {
        var s = load();
        if (!s || fromStage === 'PRAISE') return;

        var order = ['STORY', 'MIND', 'CAUSE', 'EXPRESSION', 'ACTION', 'PRAISE'];
        var nextStage = order[order.indexOf(fromStage) + 1];
        if (!nextStage || s.nodes[nextStage]) return;

        fetchNext(s, fromStage).catch(function () { });
    }

    function prevStageOf(cur) {
        var order = ['STORY', 'MIND', 'CAUSE', 'EXPRESSION', 'ACTION', 'PRAISE'];
        return order[Math.max(0, order.indexOf(cur) - 1)];
    }

    /* 카드에 data-kd-key 를 붙이는 일이 paint 보다 늦으면 안 되므로,
       DOM 이 준비된 뒤에 시작한다. */
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boot);
    } else {
        boot();
    }
}());
