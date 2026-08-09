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
        '감정 표현하기 — 슬픔 연습', '감정 표현하기 — 기쁨 연습', '복합 감정 이해하기',
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
            '표정으로 표현하기', '몸짓으로 표현하기', '감정 표현하기 — 슬픔 연습',
            '친구 위로하기', '차례 지키기', '다툰 뒤 화해하기', '도움 요청하기', '마음 이야기 나누기'],
        standard: ['표정 알아보기', '기쁨 알아차리기', '슬픔 알아차리기', '화남 알아차리기',
            '놀람 알아차리기', '무서움 알아차리기', '표정으로 표현하기', '몸짓으로 표현하기',
            '상황과 감정 잇기', '친구 위로하기', '눈 맞추고 대화하기', '스스로 돌아보기']
    };

    /* 카드에 보여 줄 6단계 요약 — 온보딩 로드맵 비교 화면(Figma 306:75)의 문구를 그대로 쓴다.
       12주 배열과 별개인 이유: 정석·AI 는 '단계 이름'이 기획 문구라 주차 주제에서 뽑아낼 수 없다.
       직접 구성 카드만 저장된 12주를 2주씩 묶어 만든다(makeCustomSteps). */
    var KD_STEPS = {
        standard: [
            ['감정 인식 기초', '기본 6가지 감정 알기', '1-2주'],
            ['감정 표현 연습', '표정/몸짓 따라하기', '3-4주'],
            ['상황별 감정 이해', '상황과 감정 연결하기', '5-6주'],
            ['공감 반응 연습', '위로/격려 표현하기', '7-8주'],
            ['사회적 행동 적용', '실생활 사회 기술', '9-10주'],
            ['종합 복습', '전체 내용 통합', '11-12주']
        ],
        ai: [
            ['감정 표현 집중', '표현이 약한 부분 우선 강화', '1-3주'],
            ['감정 이해 심화', '복합 감정 이해하기', '4-5주'],
            ['사회적 상호작용', '친구와 소통하기', '6-7주'],
            ['공감 실전 연습', '실제 상황 역할극', '8-9주'],
            ['일상 적용', '가정/학교 연계 활동', '10-11주'],
            ['자기 평가', '스스로 돌아보기', '12주']
        ]
    };

    function preset(mode) {
        return (KD_PRESET[mode] || KD_PRESET.ai).map(function (t) { return { topic: t, on: true }; });
    }

    function defaults() { return { mode: 'ai', focus: '감정 표현', weeks: preset('ai') }; }

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
           0단계는 아래 카드행이 통째로 빠져 카드가 커지므로 8줄로 채운다 */
        var n = stage() === 0 ? 8 : 4;
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

    /* 직접 구성 카드 — 켜 둔 주차를 2개씩 묶어 6줄로. 12주가 아니어도(토글로 뺐어도) 나눠 떨어진다 */
    function makeCustomSteps(p) {
        var on = p.weeks.filter(function (w) { return w.on; });
        var per = Math.max(1, Math.ceil(on.length / 6)), out = [];
        for (var i = 0; i < on.length; i += per) {
            var g = on.slice(i, i + per);
            var from = i + 1, to = Math.min(i + per, on.length);
            out.push([g[0].topic, g.slice(1).map(function (w) { return w.topic; }).join(' · ') || '한 주 집중',
                (from === to ? from : from + '-' + to) + '주']);
            if (out.length === 6) break;
        }
        return out;
    }

    function fillSteps(box, steps) {
        box.textContent = '';
        steps.forEach(function (st, i) {
            var d = document.createElement('div');
            d.className = 'onb-step-item';
            d.innerHTML = '<span class="no">' + (i + 1) + '</span>'
                + '<div class="hd"><p class="t"></p><span class="wk"></span></div><p class="d"></p>';
            d.querySelector('.t').textContent = st[0];
            d.querySelector('.d').textContent = st[1];
            d.querySelector('.wk').textContent = st[2];
            box.appendChild(d);
        });
    }

    function renderCards(p) {
        var boxes = document.querySelectorAll('#roadmapPick .steps');
        if (!boxes.length) return;
        boxes.forEach(function (box) {
            var kind = box.dataset.plan;
            if (kind === 'custom') {
                /* 아직 손대지 않았으면 비워 둔다 — CSS 가 안내 문구를 대신 보여 준다 */
                if (p.mode === 'custom') fillSteps(box, makeCustomSteps(p));
                else box.textContent = '';
            } else {
                fillSteps(box, KD_STEPS[kind]);
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
                if (r.value !== 'custom') draft.weeks = preset(r.value);
                renderCards(draft);
            });
        });

        var dlg = document.getElementById('dlgWeeks');

        document.getElementById('planEdit').addEventListener('click', function () {
            fillEditor(draft);
            dlg.showModal();
        });

        document.getElementById('weeksCancel').addEventListener('click', function () { dlg.close(); });

        document.getElementById('weeksApply').addEventListener('click', function () {
            draft.weeks = readEditorWeeks();
            draft.mode = 'custom';          /* 손으로 고친 순간 직접 구성이 된다 */
            paint();
            dlg.close();
        });

        dlg.addEventListener('click', function (e) {
            var btn = e.target.closest('.up, .dn');
            if (!btn) return;
            var rows = editorRows(), i2 = rows.indexOf(btn.closest('.mp-week'));
            var j2 = btn.classList.contains('up') ? i2 - 1 : i2 + 1;
            var w = readEditorWeeks(), t = w[i2]; w[i2] = w[j2]; w[j2] = t;
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

        document.getElementById('planSave').addEventListener('click', function () {
            draft.mode = pickedMode();
            kdPlanSave(draft);
            kdSaved('로드맵을 저장했어요');
        });

        paint();
    }

    function boot() { renderDash(); initEditor(); }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
    else boot();
})();
