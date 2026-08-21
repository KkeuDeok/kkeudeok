(function () {
    'use strict';

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
                li.innerHTML = '<span class="no no-soon">' + (i + 1) + '</span><span class="t"></span>';
                li.querySelector('.t').textContent = i === 0 ? '로드맵 생성중…' : '';
                ul.appendChild(li);
            }
        });

        if (foot) foot.textContent = '아이에게 맞는 12주 계획을 만들고 있어요';
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

        renderDash();
    }

    var EVERY_MS = 3000;
    var MAX_TRIES = 200;
    var tries = 0;

    function loadRoadmap() {
        renderDash();

        fetch('/api/roadmap')
            .then(function (r) { return (r.ok && r.status !== 204) ? r.json() : null; })
            .then(function (view) {
                if (view && view.plan && view.plan.weeks && view.plan.weeks.length) {
                    applyRoadmap(view);
                    return;
                }

                tries += 1;

                if (tries < MAX_TRIES) {
                    setTimeout(loadRoadmap, EVERY_MS);
                } else {
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
