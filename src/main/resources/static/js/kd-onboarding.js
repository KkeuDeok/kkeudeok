// 온보딩 등록
(function () {
    'use strict';

    var ONB_KEY = 'kdOnb';
    var CALIB_KEY = 'kdCalibPending';

    function flushCalib() {

        var rows;
        try {
            rows = JSON.parse(sessionStorage.getItem(CALIB_KEY)) || [];
        } catch (e) {
            rows = [];
        }

        if (!rows.length) return Promise.resolve();

        var left = [];

        var chain = rows.reduce(function (prev, r) {
            return prev.then(function () {
                return fetch('/api/calib', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ emotion: r.emotion, shapes: r.shapes })
                }).then(function (res) {
                    if (res.status !== 200) left.push(r);
                }).catch(function () {
                    left.push(r);
                });
            });
        }, Promise.resolve());

        return chain.then(function () {
            try {
                if (left.length) {
                    sessionStorage.setItem(CALIB_KEY, JSON.stringify(left));
                } else {
                    sessionStorage.removeItem(CALIB_KEY);
                }
            } catch (e) { }

            console.info('[kkeudeok] 등록한 표정 ' + (rows.length - left.length)
                    + '/' + rows.length + '개를 저장했습니다');
        });
    }

    function readOnb() {
        try {
            return JSON.parse(sessionStorage.getItem(ONB_KEY)) || {};
        } catch (e) {
            return {};
        }
    }

    function saveOnb(patch) {
        var v = readOnb();
        Object.keys(patch).forEach(function (k) { v[k] = patch[k]; });
        try { sessionStorage.setItem(ONB_KEY, JSON.stringify(v)); } catch (e) { }
    }

    var DOMAINS = {
        '/onboarding/checklist': [
            { domain: '감정 이해', names: ['q1', 'q2', 'q3'] },
            { domain: '감정 표현', names: ['q4', 'q5', 'q6'] }
        ],
        '/onboarding/checklist-2': [
            { domain: '감정 조절', names: ['q7', 'q8', 'q9'] },
            { domain: '사회적 상호작용', names: ['q10', 'q11', 'q12'] }
        ]
    };

    function collectChecklist() {
        var groups = DOMAINS[location.pathname.replace(/\/$/, '')];
        if (!groups) return;

        var answers = readOnb().checklist || [];

        var mine = {};
        groups.forEach(function (g) { mine[g.domain] = true; });
        answers = answers.filter(function (a) { return !mine[a.domain]; });

        groups.forEach(function (g) {
            g.names.forEach(function (name, i) {
                var picked = document.querySelector('input[name="' + name + '"]:checked');
                if (!picked) return;
                answers.push({ domain: g.domain, questionNo: i + 1, score: +picked.value });
            });
        });

        saveOnb({ checklist: answers });
    }

    ['kdSubmitOnbChecklist', 'kdSubmitOnbChecklist2'].forEach(function (fn) {
        var orig = window[fn];
        if (typeof orig !== 'function') return;

        window[fn] = function () {
            collectChecklist();
            return orig.apply(this, arguments);
        };
    });

    window.kdOnbSubmit = function (next) {

        var v = readOnb();

        var body = {
            name: v.name,
            birthY: v.birthY, birthM: v.birthM, birthD: v.birthD,
            gender: v.gender,
            disType: v.disType,
            disLevel: v.disLevel,
            charKey: v.charKey,
            nickname: v.nickname,
            checklist: v.checklist || []
        };

        var btn = document.querySelector('.onb-actions .kd-btn-primary');
        var label = btn ? btn.textContent : '';
        if (btn) { btn.disabled = true; btn.textContent = '준비하는 중…'; }

        return fetch('/api/onboarding/child', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        }).then(function (res) {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            return res.json();
        }).then(function (r) {
            console.info('[kkeudeok] 온보딩 등록 완료 — childId=' + r.childId);
            return flushCalib();
        }).catch(function (e) {
            console.warn('[kkeudeok] 온보딩을 저장하지 못했습니다', e);
        }).then(function () {
            if (btn) { btn.disabled = false; btn.textContent = label; }
            if (typeof next === 'function') next();
        });
    };
}());
