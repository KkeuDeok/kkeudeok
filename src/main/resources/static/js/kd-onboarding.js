/* 온보딩 등록 — 화면이 모아 둔 값을 서버에 한 번에 보낸다.
 *
 * 흐름
 *   아이 프로필 → 캐릭터 → 체크리스트 1·2 → 표정 등록 → 완료
 *   각 화면은 sessionStorage(kdOnb)에 담기만 하고, **완료 화면에서 한 번에** 보낸다.
 *   중간에 보내면 뒤로 가서 고쳤을 때 반쯤 저장된 아이가 남는다.
 *
 * ⚠ 표정 등록만 예외다(kd-face-calib.js). 표정은 그 화면을 떠나면 다시 잡을 수 없어
 *   찍는 즉시 보낸다. 그때는 아직 아이가 없을 수 있는데, 서버가 세션의 아이 →
 *   등록된 첫 아이 순으로 붙인다.
 *
 * ⚠ 회원 기능과 붙는 자리
 *   로그인이 세션에 회원 번호를 넣어 주면(SessionKeys.MEMBER_ID) 그 회원의 아이로 등록된다.
 *   지금은 비어 있어 서버가 자리를 채우는 회원을 만든다.
 */
(function () {
    'use strict';

    var ONB_KEY = 'kdOnb';

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

    /* ---------- 체크리스트 ----------
       화면은 4개 영역 × 3문항, 7단계 척도다.
       ⚠ 낮을수록 잘한다(왼쪽 '그렇다'=1). 서버도 그렇게 읽는다 — 뒤집으면 로드맵이 정반대로 짜인다. */
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

        /* 다시 답하면 그 영역만 갈아끼운다 — 앞 화면 답을 지우면 안 된다 */
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

    /* 체크리스트 화면의 [다음]을 감싼다 — 넘어가기 전에 답을 담는다 */
    ['kdSubmitOnbChecklist', 'kdSubmitOnbChecklist2'].forEach(function (fn) {
        var orig = window[fn];
        if (typeof orig !== 'function') return;
        window[fn] = function () {
            collectChecklist();
            return orig.apply(this, arguments);
        };
    });

    /* ---------- 완료 화면 — 서버에 등록 ----------
       등록이 끝나야 로드맵이 생기고, 대시보드 카드가 채워진다. */
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
        }).catch(function (e) {
            /* 등록에 실패해도 화면은 막지 않는다. 대시보드는 예시값으로라도 뜬다.
               ⚠ 다만 그 상태로 학습을 시작하면 '등록된 첫 아이'로 붙는다. */
            console.warn('[kkeudeok] 온보딩을 저장하지 못했습니다', e);
        }).then(function () {
            if (btn) { btn.disabled = false; btn.textContent = label; }
            if (typeof next === 'function') next();
        });
    };
}());
