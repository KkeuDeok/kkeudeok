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

    /* 현재 몇 주차인지 — 학습 이력이 없어 계산할 근거가 없다.
       빈 상태(신규)면 시작 전(0), 아니면 예시값 7주차를 쓴다. 백엔드가 붙으면 여기만 바꾼다 */
    function curWeek() {
        return document.documentElement.dataset.kdEmpty ? 0 : 7;
    }

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

        /* 카드에는 4줄만 — 진행 중 주차 앞뒤로 잘라서 보여 준다 */
        var start = cur === 0 ? 0 : Math.max(0, Math.min(cur - 3, on.length - 4));
        short.textContent = '';
        on.slice(start, start + 4).forEach(function (w, i) {
            var idx = start + i;
            short.appendChild(liOf(idx + 1, w.topic, stateOf(idx, cur)));
        });

        var foot = document.getElementById('dashPlanFoot');
        if (foot) {
            foot.textContent = on.length + '주 과정 중 '
                + (cur === 0 ? '1주차 시작 전' : cur + '주차')
                + ' · 이번 주 ' + (cur === 0 ? 0 : 2) + ' / 3 완료';
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

    /* ---------- 마이페이지 편집기 ---------- */

    function editorRows() {
        return [].slice.call(document.querySelectorAll('#roadmapEdit .mp-week'));
    }

    /* 화면 -> 객체. 저장할 때와 안을 바꿀 때 모두 이걸로 현재 상태를 읽는다 */
    function readEditor() {
        var p = kdPlanLoad();
        p.weeks = editorRows().map(function (row) {
            return {
                topic: row.querySelector('select').value,
                on: row.querySelector('input[type=checkbox]').checked
            };
        });
        p.mode = document.querySelector('input[name="planMode"]:checked').value;
        return p;
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

    function markCustom() {
        var c = document.querySelector('input[name="planMode"][value="custom"]');
        if (c && !c.checked) c.checked = true;
    }

    function swap(i, j) {
        var p = readEditor();
        var t = p.weeks[i]; p.weeks[i] = p.weeks[j]; p.weeks[j] = t;
        markCustom();
        p.mode = 'custom';
        fillEditor(p);
    }

    function initEditor() {
        var box = document.getElementById('roadmapEdit');
        if (!box) return;
        var p = kdPlanLoad();

        document.querySelectorAll('input[name="planMode"]').forEach(function (r) {
            r.checked = (r.value === p.mode);
            r.addEventListener('change', function () {
                if (r.value === 'custom') return;      /* 직접 구성은 지금 값을 유지한다 */
                fillEditor({ weeks: preset(r.value) });
            });
        });

        box.addEventListener('change', function (e) {
            var row = e.target.closest('.mp-week');
            if (!row) return;
            if (e.target.type === 'checkbox') row.classList.toggle('is-off', !e.target.checked);
            markCustom();
        });

        box.addEventListener('click', function (e) {
            var btn = e.target.closest('.up, .dn');
            if (!btn) return;
            var rows = editorRows(), i = rows.indexOf(btn.closest('.mp-week'));
            swap(i, btn.classList.contains('up') ? i - 1 : i + 1);
        });

        var reset = document.getElementById('planReset');
        if (reset) {
            reset.addEventListener('click', function () {
                var r = document.querySelector('input[name="planMode"][value="ai"]');
                if (r) r.checked = true;
                fillEditor(defaults());
            });
        }

        var save = document.getElementById('planSave');
        if (save) {
            save.addEventListener('click', function () {
                kdPlanSave(readEditor());
                kdSaved('로드맵을 저장했어요');
            });
        }

        fillEditor(p);
    }

    function boot() { renderDash(); initEditor(); }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot);
    else boot();
})();
