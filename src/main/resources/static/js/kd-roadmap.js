/* 학습 로드맵(12주 커리큘럼) 한 곳 관리 — 마스터 주제·프리셋·저장·렌더.
   보호자가 마이페이지 > 학습 로드맵 탭에서 고친 결과를 대시보드가 그대로 읽는다.

   ⚠ 저장소가 온보딩 입력(sessionStorage.kdOnb)과 다르다. 의도한 분리다 —
     온보딩 입력은 그 세션에서만 쓰는 임시값이고, 로드맵은 한 번 정하면 계속 유지돼야 하는 설정이다.
   ⚠ 백엔드가 붙으면 kdPlanLoad/kdPlanSave 두 함수만 서버 호출로 바꾸면 된다. */
(function () {
    'use strict';

    var KEY = 'kdPlan';
    var WEEKS = 12;

    /* 주제 후보 — 드롭다운 목록. 프리셋에 쓰이는 항목은 전부 여기 있어야 한다 */
    var KD_TOPICS = [
        '표정 알아보기', '기쁨 알아차리기', '슬픔 알아차리기', '화남 알아차리기',
        '놀람 알아차리기', '무서움 알아차리기', '표정으로 표현하기', '몸짓으로 표현하기',
        '슬픔 감정 표현하기', '기쁨 감정 표현하기', '복합 감정 이해하기',
        '친구 위로하기', '차례 지키기', '다툰 뒤 화해하기', '도움 요청하기',
        '마음 이야기 나누기', '눈 맞추고 대화하기', '먼저 말 걸어보기',
        '상황과 감정 잇기', '가정에서 연습하기', '학교에서 연습하기', '스스로 돌아보기'
    ];

    /* 12주 프리셋 2벌.
       ai       = 체크리스트 기반 개인화안(온보딩 '  AI 맞춤 커리큘럼' 6단계를 12주로 편 것)
       standard = 일반 순서(온보딩 '정석 커리큘럼')
       ponytail: 실제 추천 로직은 백엔드 몫이라 지금은 고정 프리셋이다 */
    var KD_PRESET = {
        ai: ['표정 알아보기', '기쁨 알아차리기', '슬픔 알아차리기', '화남 알아차리기',
            '표정으로 표현하기', '몸짓으로 표현하기', '슬픔 감정 표현하기',
            '친구 위로하기', '차례 지키기', '다툰 뒤 화해하기', '도움 요청하기', '마음 이야기 나누기'],
        standard: ['표정 알아보기', '기쁨 알아차리기', '슬픔 알아차리기', '화남 알아차리기',
            '놀람 알아차리기', '무서움 알아차리기', '표정으로 표현하기', '몸짓으로 표현하기',
            '상황과 감정 잇기', '친구 위로하기', '눈 맞추고 대화하기', '스스로 돌아보기']
    };

    function preset(mode) {
        return (KD_PRESET[mode] || KD_PRESET.ai).map(function (t) { return { topic: t, on: true }; });
    }

    /* custom 은 '지금 고른 안'과 별개로 보관한다.
       예전에는 weeks 하나만 들고 있어 정석·AI 를 누르는 순간 직접 만든 구성이 사라졌다(2026-08-09 지적).
       이제 mode 가 무엇이든 custom 은 남고, 삭제 버튼으로만 지운다. */
    function defaults() { return { mode: 'ai', focus: '감정 표현', weeks: preset('ai'), custom: null }; }

    /* 저장값이 깨졌거나(수기 편집·구버전) 개수가 안 맞으면 기본값으로 되돌린다 */
    function kdPlanLoad() {
        try {
            var p = JSON.parse(localStorage.getItem(KEY));
            if (!p || !Array.isArray(p.weeks) || p.weeks.length !== WEEKS) return defaults();
            return p;
        } catch (e) { return defaults(); }
    }

    function kdPlanSave(p) {
        try { localStorage.setItem(KEY, JSON.stringify(p)); } catch (e) { }
    }

    window.kdPlanLoad = kdPlanLoad;
    window.kdPlanSave = kdPlanSave;
    window.KD_PRESET_OF = preset;

    /* 현재 몇 주차인지 — 학습 이력이 없어 계산할 근거가 없다. 사용 단계로 대신한다.
       0 신규=시작 전 · 1 시작함=1주차 · 2 익숙함=예시값 7주차. 백엔드가 붙으면 여기만 바꾼다 */
    function stage() { return +(document.documentElement.dataset.kdStage || 2); }
    function curWeek() { return [0, 1, 7][stage()]; }

    /* ---------- 대시보드 렌더 ---------- */

    var CHIP = { done: '완료', now: '진행중', soon: '예정' };

    function stateOf(idx, cur) {          /* idx 는 0부터 */
        if (cur === 0) return 'soon';
        if (idx + 1 < cur) return 'done';
        if (idx + 1 === cur) return 'now';
        return 'soon';
    }

    function liOf(no, topic, st) {
        var li = document.createElement('li');
        li.innerHTML = '<span class="no no-' + st + '">' + no + '</span>'
            + '<span class="t"></span>'
            + '<span class="chip chip-' + st + '">' + CHIP[st] + '</span>';
        li.querySelector('.t').textContent = topic;   /* 주제는 사용자가 고른 값이라 textContent 로 */
        return li;
    }

    function renderDash() {
        var short = document.getElementById('dashPlanList');
        if (!short) return;
        var p = kdPlanLoad(), cur = curWeek();
        var on = p.weeks.filter(function (w) { return w.on; });

        /* 카드에는 진행 중 주차 앞뒤로 잘라서 보여 준다.
           줄 수는 **옆 카드 높이에 맞춰** 정한다 — 짧으면 로드맵 카드만 작아져 나란히 안 맞는다
           (2026-08-10 지적: 1단계에서 왼쪽 500 / 로드맵 346 으로 어긋나 있었다).
           0단계 8줄(620) · 1단계 7줄(560) · 2단계는 위에 지표행이 있어 4줄(346). */
        var n = stage() === 0 ? 8 : (stage() === 1 ? 7 : 4);
        var start = cur === 0 ? 0 : Math.max(0, Math.min(cur - 3, on.length - n));
        short.textContent = '';
        on.slice(start, start + n).forEach(function (w, i) {
            var idx = start + i;
            short.appendChild(liOf(idx + 1, w.topic, stateOf(idx, cur)));
        });

        var foot = document.getElementById('dashPlanFoot');
        if (foot) {
            foot.textContent = on.length + '주 과정 중 '
                + (cur === 0 ? '1주차 시작 전' : cur + '주차')
                + ' · 이번 주 ' + [0, 1, 2][stage()] + ' / 3 완료';
        }

        var full = document.getElementById('dlgPlanList');
        if (full) {
            full.textContent = '';
            on.forEach(function (w, i) { full.appendChild(liOf(i + 1, w.topic, stateOf(i, cur))); });
        }
        var lead = document.getElementById('dlgPlanLead');
        if (lead) {
            lead.textContent = '1순위 목표는 ' + p.focus + '예요. '
                + (cur === 0 ? '아직 시작 전이에요.' : '지금은 ' + cur + '주차예요.');
        }
    }

    /* ---------- 마이페이지 카드 3장 ---------- */

    /* 카드는 12주를 그대로 보여 준다.
       6단계로 묶어 보여 줬더니 카드 아래가 반쯤 비어 '더 길게 할 거면 7·8·9·10 도 나와야 한다'는
       지적(2026-08-09). 12줄이면 카드 높이(650)와도 맞고 정본 데이터와 화면이 1:1 이 된다.
       대신 단계 설명줄은 뺐다 — 12줄 × 2줄이면 카드가 두 배가 된다. */
    function weekRows(weeks) {
        return weeks.filter(function (w) { return w.on; })
            .map(function (w, i) { return [w.topic, (i + 1) + '주']; });
    }

    /* 카드에는 앞 8줄만. 12줄을 다 넣으면 카드가 676 이 되어 저장 버튼이 995 —
       세로 예산(1024) 여유가 29px 밖에 안 남는다. 나머지는 '외 N주 더' 한 줄로 접고
       전체는 편집 모달에서 본다(2026-08-09 결정). */
    var CARD_ROWS = 8;

    function fillSteps(box, rows) {
        box.textContent = '';
        rows.slice(0, CARD_ROWS).forEach(function (st, i) {
            var d = document.createElement('div');
            d.className = 'onb-step-item';
            d.innerHTML = '<span class="no">' + (i + 1) + '</span>'
                + '<div class="hd"><p class="t"></p><span class="wk"></span></div>';
            d.querySelector('.t').textContent = st[0];
            d.querySelector('.wk').textContent = st[1];
            box.appendChild(d);
        });
        var rest = rows.length - CARD_ROWS;
        if (rest > 0) {
            var more = document.createElement('p');
            more.className = 'steps-more';
            more.textContent = '외 ' + rest + '주 더 — 눌러서 전체 보기';
            box.appendChild(more);
        }
    }

    function renderCards(p) {
        var boxes = document.querySelectorAll('#roadmapPick .steps');
        if (!boxes.length) return;
        boxes.forEach(function (box) {
            var kind = box.dataset.plan;
            if (kind === 'custom') {
                /* 만든 적이 없으면 카드 자체가 없다. 한 번 만들면 어떤 안을 고르든 계속 남는다 */
                box.closest('.mp-plan').hidden = !p.custom;
                if (p.custom) fillSteps(box, weekRows(p.custom));
            } else {
                fillSteps(box, weekRows(preset(kind)));
            }
        });
    }

    /* ---------- 마이페이지 편집기(모달) ---------- */

    function editorRows() {
        return [].slice.call(document.querySelectorAll('#roadmapEdit .mp-week'));
    }

    /* 모달 화면 -> 12주 배열 */
    function readEditorWeeks() {
        return editorRows().map(function (row) {
            return {
                topic: row.querySelector('select').value,
                on: row.querySelector('input[type=checkbox]').checked
            };
        });
    }

    /* 객체 -> 화면. select 는 값만 바꾸면 onb-select.js 가 만든 커스텀 드롭다운 글씨가 안 따라오므로
       kdBuildSelects() 로 다시 그린다(onb-select.js 가 노출해 준다) */
    function fillEditor(p) {
        editorRows().forEach(function (row, i) {
            var w = p.weeks[i];
            row.querySelector('select').value = w.topic;
            row.querySelector('input[type=checkbox]').checked = w.on;
            row.classList.toggle('is-off', !w.on);
        });
        if (window.kdBuildSelects) window.kdBuildSelects();
        syncArrows();
    }

    /* 첫 행의 ▲, 마지막 행의 ▼ 는 누를 데가 없다 */
    function syncArrows() {
        var rows = editorRows();
        rows.forEach(function (row, i) {
            row.querySelector('.up').disabled = (i === 0);
            row.querySelector('.dn').disabled = (i === rows.length - 1);
        });
    }

    /* ---------- 끌어서 순서 바꾸기 ----------
       HTML5 draggable 을 안 쓴다 — auth-validate.js 가 전역에서 dragstart 를 막고 있고,
       그건 이미지 끌기 방지용이라 예외를 뚫느니 포인터 이벤트로 만드는 편이 안전하다.
       12행뿐이라 매 이동마다 전체를 다시 그려도 무겁지 않다. */
    function initDrag(box, onDrop) {
        var from = -1, row = null;

        box.addEventListener('pointerdown', function (e) {
            /* 손잡이(⣿)와 번호 둘 다 잡는 자리 — 16px 손잡이만으로는 어디를 잡는지 안 보인다.
               드롭다운·▲▼·토글은 각자 할 일이 있으니 제외된다 */
            var grip = e.target.closest('.grip, .no');
            if (!grip) return;
            row = grip.closest('.mp-week');
            if (!row) return;
            from = editorRows().indexOf(row);
            row.classList.add('is-drag');
            grip.setPointerCapture(e.pointerId);
            e.preventDefault();          /* 끌 때 글자가 선택되지 않게 */
        });

        box.addEventListener('pointermove', function (e) {
            if (from < 0) return;
            var over = target(e.clientY);
            editorRows().forEach(function (r) { r.classList.toggle('is-over', r === over && r !== row); });
        });

        function target(y) {
            /* 2열 그리드라 x 를 무시하면 엉뚱한 열에 놓인다 — 세로로 가장 가까운 행을 찾되
               같은 열(왼쪽 1~6 / 오른쪽 7~12)만 후보로 둔다 */
            var rows = editorRows(), best = null, gap = 1e9;
            var col = rows.indexOf(row) < 6 ? 0 : 1;
            rows.forEach(function (r, i) {
                if ((i < 6 ? 0 : 1) !== col) return;
                var b = r.getBoundingClientRect(), d = Math.abs((b.top + b.bottom) / 2 - y);
                if (d < gap) { gap = d; best = r; }
            });
            return best;
        }

        function end(e) {
            if (from < 0) return;
            var over = target(e.clientY), to = editorRows().indexOf(over);
            editorRows().forEach(function (r) { r.classList.remove('is-drag', 'is-over'); });
            if (to >= 0 && to !== from) onDrop(from, to);
            from = -1; row = null;
        }

        box.addEventListener('pointerup', end);
        box.addEventListener('pointercancel', end);
    }

    /* 화면에서 지금 고른 안 */
    function pickedMode() {
        var r = document.querySelector('input[name="planMode"]:checked');
        return r ? r.value : 'ai';
    }

    function initEditor() {
        var pick = document.getElementById('roadmapPick');
        if (!pick) return;

        /* 편집 중인 값 — 모달에서 [적용] 하기 전까지 저장하지 않는다 */
        var draft = kdPlanLoad();

        function paint() {
            document.querySelectorAll('input[name="planMode"]').forEach(function (r) {
                r.checked = (r.value === draft.mode);
            });
            renderCards(draft);
        }

        /* 정석·AI 를 고르면 그 프리셋으로 12주를 덮는다. 직접 구성은 지금 값을 유지 */
        document.querySelectorAll('input[name="planMode"]').forEach(function (r) {
            r.addEventListener('change', function () {
                draft.mode = r.value;
                draft.weeks = (r.value === 'custom') ? draft.custom.slice() : preset(r.value);
                renderCards(draft);
            });
        });

        var dlg = document.getElementById('dlgWeeks');

        function openWeeks() {
            fillEditor({ weeks: draft.custom || draft.weeks });
            dlg.showModal();
        }

        document.getElementById('planEdit').addEventListener('click', openWeeks);

        /* 카드를 누르면 그 안이 선택되고 12주 전체가 열린다 — 카드에는 8줄만 보이므로
           '외 N주 더' 를 확인할 길이 이것뿐이다(2026-08-09 지적).
           라디오 change 가 먼저 돌아 draft 는 이미 그 안으로 바뀐 뒤다.
           삭제(×)는 stopPropagation 으로 여기 안 걸린다. */
        pick.querySelectorAll('.mp-plan').forEach(function (card) {
            card.addEventListener('click', function () {
                if (!card.hidden) setTimeout(openWeeks, 0);
            });
        });


        document.getElementById('weeksCancel').addEventListener('click', function () { dlg.close(); });

        document.getElementById('weeksApply').addEventListener('click', function () {
            draft.custom = readEditorWeeks();
            draft.weeks = draft.custom.slice();
            draft.mode = 'custom';          /* 손으로 고친 순간 직접 구성이 된다 */
            paint();
            dlg.close();
        });

        dlg.addEventListener('click', function (e) {
            var btn = e.target.closest('.up, .dn');
            if (!btn) return;
            var rows = editorRows(), i2 = rows.indexOf(btn.closest('.mp-week'));
            var j2 = btn.classList.contains('up') ? i2 - 1 : i2 + 1;
            if (j2 < 0 || j2 > 11) return;
            var w = readEditorWeeks();
            w.splice(j2, 0, w.splice(i2, 1)[0]);
            fillEditor({ weeks: w });
        });

        /* 끌어 놓기 — 자리 맞바꾸기가 아니라 뽑아서 그 자리에 끼운다.
           맞바꾸면 12주차를 1주차로 보낼 때 사이 항목들이 뒤죽박죽 된다 */
        initDrag(dlg, function (from, to) {
            var w = readEditorWeeks();
            w.splice(to, 0, w.splice(from, 1)[0]);
            fillEditor({ weeks: w });
        });

        dlg.addEventListener('change', function (e) {
            var row = e.target.closest('.mp-week');
            if (row && e.target.type === 'checkbox') row.classList.toggle('is-off', !e.target.checked);
        });

        document.getElementById('planReset').addEventListener('click', function () {
            draft = defaults();
            paint();
        });

        /* 직접 만든 구성만 지운다 — 지우면 고른 안은 AI 로 돌아간다 */
        var del = document.getElementById('planDelete');
        if (del) del.addEventListener('click', function (e) {
            e.stopPropagation();            /* 카드 클릭(편집 열기)까지 타지 않게 */
            draft.custom = null;
            draft.mode = 'ai';
            draft.weeks = preset('ai');
            paint();
        });

        document.getElementById('planSave').addEventListener('click', function () {
            draft.mode = pickedMode();
            kdPlanSave(draft);
            kdSaved('로드맵을 저장했어요');
        });

        paint();

        /* /mypage/roadmap#weeks 로 들어오면 편집 화면이 바로 열린다.
           캡처·시연에서 '주차별 고르기'를 한 번에 보여줄 때 쓴다. */
        if (location.hash === '#weeks') openWeeks();
    }

    function boot() { renderDash(); initEditor(); }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
    else boot();
})();
