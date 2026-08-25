(function () {
    'use strict';

    if (location.pathname.replace(/\/$/, '') !== '/dashboard') return;

    var KD_STAGES = [];
    var KD_WEEKS = [];

    var serverWeek = null;

    var doneWeeks = {};
    var todayDone = 0;
    var dailyGoal = 3;

    var CHIP = { done: '완료', soon: '미완료' };

    function curWeek() { return serverWeek !== null ? serverWeek : 0; }

    function stage() { return +(document.documentElement.dataset.kdStage || 2); }

    function rows() { return stage() === 0 ? 8 : (stage() === 1 ? 7 : 4); }

    function stageNameAt(idx) {
        var wk = 0;
        for (var i = 0; i < KD_STAGES.length; i++) {
            wk += KD_STAGES[i][1];
            if (idx < wk) return KD_STAGES[i][0];
        }
        return '';
    }

    function stateOf(idx) {
        return doneWeeks[idx + 1] ? 'done' : 'soon';
    }

    function liOf(no, topic, st) {
        var li = document.createElement('li');
        li.innerHTML = '<span class="no no-' + st + '">' + no + '</span>'
            + '<span class="t"></span>'
            + '<span class="chip chip-' + st + '">' + CHIP[st] + '</span>';
        li.querySelector('.t').textContent = topic;
        return li;
    }

    var LOAD_TAU_MS = 30000;
    var LOAD_CEIL = 96;
    var LOAD_TICK_MS = 700;

    var LOAD_STEPS = [
        [0, '아이 정보를 살펴보고 있어요'],
        [22, '12주 계획의 뼈대를 잡고 있어요'],
        [55, '주차별 학습 주제를 고르고 있어요'],
        [82, '계획을 마지막으로 다듬고 있어요']
    ];

    var LOAD_GRACE_MS = 700;

    var loadStartedAt = 0;
    var loadTimer = null;
    var loadArm = null;

    function loadMsg(pct) {
        if (pct >= 100) return '12주 계획이 준비됐어요';

        var text = LOAD_STEPS[0][1];
        for (var i = 0; i < LOAD_STEPS.length; i++) {
            if (pct >= LOAD_STEPS[i][0]) text = LOAD_STEPS[i][1];
        }
        return text;
    }

    function loadPct() {
        var passed = Date.now() - loadStartedAt;
        return Math.min(LOAD_CEIL, Math.round(LOAD_CEIL * (1 - Math.exp(-passed / LOAD_TAU_MS))));
    }

    function loadBox(host, before) {
        var box = host.querySelector('.plan-load');
        if (box) return box;

        box = document.createElement('div');
        box.className = 'plan-load';
        box.setAttribute('role', 'progressbar');
        box.setAttribute('aria-valuemin', '0');
        box.setAttribute('aria-valuemax', '100');
        box.innerHTML = '<div class="plan-load-hd"><span class="m"></span><b class="p">0%</b></div>'
            + '<div class="plan-load-track"><i></i></div>';

        host.insertBefore(box, before || null);
        return box;
    }

    function loadBoxes() {
        var out = [];

        var plan = document.querySelector('.dash-plan');
        if (plan) out.push(loadBox(plan, document.getElementById('dashPlanFoot')));

        var dlg = document.querySelector('.dash-dlg');
        if (dlg) out.push(loadBox(dlg, dlg.querySelector('button')));

        return out;
    }

    function paintLoad(pct) {
        var text = loadMsg(pct);

        loadBoxes().forEach(function (box) {
            box.setAttribute('aria-valuenow', pct);
            box.querySelector('.plan-load-track i').style.width = pct + '%';
            box.querySelector('.plan-load-hd .p').textContent = pct + '%';
            box.querySelector('.plan-load-hd .m').textContent = text;
        });
    }

    function dropLoad() {
        document.querySelectorAll('.plan-load').forEach(function (box) {
            if (box.parentNode) box.parentNode.removeChild(box);
        });
    }

    function startLoad() {
        if (loadTimer) return;

        loadStartedAt = Date.now();
        paintLoad(loadPct());

        loadTimer = setInterval(function () { paintLoad(loadPct()); }, LOAD_TICK_MS);
    }

    function armLoad() {
        if (loadArm || loadTimer) return;

        loadArm = setTimeout(function () {
            loadArm = null;
            startLoad();
        }, LOAD_GRACE_MS);
    }

    function endLoad(done) {
        if (loadArm) {
            clearTimeout(loadArm);
            loadArm = null;
        }

        if (!loadTimer) {
            dropLoad();
            return;
        }

        clearInterval(loadTimer);
        loadTimer = null;

        if (!done) {
            dropLoad();
            return;
        }

        paintLoad(100);
        setTimeout(dropLoad, 450);
    }

    function renderWaiting() {
        var foot = document.getElementById('dashPlanFoot');
        var n = rows();

        ['dashPlanList', 'dlgPlanList'].forEach(function (id) {
            var ul = document.getElementById(id);
            if (!ul) return;

            ul.textContent = '';
            for (var i = 0; i < n; i++) {
                var li = document.createElement('li');
                li.className = 'plan-wait';
                li.innerHTML = '<span class="no no-soon">' + (i + 1) + '</span>'
                    + '<span class="t"><em class="plan-skel"></em></span>';
                li.querySelector('.plan-skel').style.width = (48 + (i * 37) % 34) + '%';
                ul.appendChild(li);
            }
        });

        if (foot) foot.textContent = '';

        armLoad();
    }

    function renderDash() {
        var short = document.getElementById('dashPlanList');
        if (!short) return;

        if (!KD_WEEKS.length) { renderWaiting(); return; }

        var cur = curWeek();
        var n = rows();
        var start = cur === 0 ? 0 : Math.max(0, Math.min(cur - 3, KD_WEEKS.length - n));

        short.textContent = '';
        KD_WEEKS.slice(start, start + n).forEach(function (topic, i) {
            short.appendChild(liOf(start + i + 1, topic, stateOf(start + i)));
        });

        var foot = document.getElementById('dashPlanFoot');
        if (foot) {
            foot.textContent = KD_WEEKS.length + '주 과정 중 '
                + (cur === 0 ? '1주차 시작 전' : cur + '주차')
                + ' · 오늘 ' + todayDone + ' / ' + dailyGoal + ' 편 완료';
        }

        var full = document.getElementById('dlgPlanList');
        if (full) {
            var seen = '';
            full.textContent = '';

            KD_WEEKS.forEach(function (topic, i) {
                var st = stageNameAt(i);
                if (st !== seen) {
                    seen = st;
                    var hd = document.createElement('li');
                    hd.className = 'plan-stage';
                    hd.textContent = st;
                    full.appendChild(hd);
                }
                full.appendChild(liOf(i + 1, topic, stateOf(i)));
            });
        }
    }

    function applyRoadmap(view) {
        if (!view || !view.plan || !view.plan.weeks || !view.plan.weeks.length) return;

        KD_WEEKS = view.plan.weeks.map(function (w) { return w.topic; });

        if (view.plan.stages && view.plan.stages.length) {
            KD_STAGES = view.plan.stages.map(function (s) { return [s.name, s.weeks]; });
        }

        if (view.currentWeek) serverWeek = view.currentWeek;

        doneWeeks = {};
        (view.doneWeeks || []).forEach(function (no) { doneWeeks[no] = true; });

        todayDone = view.todayDone || 0;
        if (view.dailyGoal) dailyGoal = view.dailyGoal;

        var badge = document.getElementById('dashPlanType');
        if (badge) {
            badge.textContent = 'AI 맞춤';
            badge.hidden = false;
        }

        endLoad(true);
        renderDash();
    }

    var EVERY_MS = 3000;
    var MAX_TRIES = 200;
    var tries = 0;

    function loadRoadmap() {
        fetch('/api/roadmap')
            .then(function (r) { return (r.ok && r.status !== 204) ? r.json() : null; })
            .then(function (view) {
                if (view && view.plan && view.plan.weeks && view.plan.weeks.length) {
                    applyRoadmap(view);
                    return;
                }

                renderWaiting();
                tries += 1;

                if (tries < MAX_TRIES) {
                    setTimeout(loadRoadmap, EVERY_MS);
                } else {
                    endLoad(false);

                    var foot = document.getElementById('dashPlanFoot');
                    if (foot) foot.textContent = '로드맵을 만들지 못했어요. 새로고침해 주세요';
                }
            })
            .catch(function (e) {
                console.warn('[kkeudeok] 로드맵을 받지 못했습니다 — 준비되면 다시 그려집니다', e);
            });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadRoadmap);
    } else {
        loadRoadmap();
    }
}());
