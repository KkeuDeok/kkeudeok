/* 대시보드 '주차별 로드맵' 카드와 [전체 보기] 12주 모달을 그린다.
   2026-08-10: 로드맵을 **고르고 고치는 화면 두 개**(온보딩 로드맵 선택 · 마이페이지 학습 로드맵 탭)를
   사용자 요청으로 삭제했다. 그래서 커리큘럼은 이제 고정 1벌(AI 추천)이고 저장소도 쓰지 않는다
   — 예전의 localStorage.kdPlan · 주차 편집기 · 정석/직접구성 선택은 전부 지웠다(git 이력에 있다).
   ⚠ 백엔드가 붙으면 아래 두 상수를 서버 값으로 바꾸면 된다. */
(function () {
    'use strict';

    /* 커리큘럼 정본 = 6단계 [이름, 주수]. 합 12.
       삭제한 온보딩 로드맵 화면의 'AI 맞춤 커리큘럼' 문구를 그대로 옮겨 둔 것이다. */
    var KD_STAGES = [
        ['감정 표현 집중',  3],
        ['감정 이해 심화',  2],
        ['사회적 상호작용', 2],
        ['공감 실전 연습',  2],
        ['일상 적용',      2],
        ['자기 평가',      1]
    ];

    /* 12주 세부 주제 — 위 단계를 주차로 편 것이다. **순서가 곧 단계 배분**이라
       KD_STAGES 의 주수(3·2·2·2·2·1)와 어긋나면 소제목이 엉뚱한 줄에 붙는다. */
    var KD_WEEKS = [
        '표정으로 표현하기', '몸짓으로 표현하기', '슬픔 감정 표현하기',   /* 감정 표현 집중 */
        '복합 감정 이해하기', '상황과 감정 잇기',                        /* 감정 이해 심화 */
        '먼저 말 걸어보기', '눈 맞추고 대화하기',                        /* 사회적 상호작용 */
        '친구 위로하기', '다툰 뒤 화해하기',                             /* 공감 실전 연습 */
        '가정에서 연습하기', '학교에서 연습하기',                        /* 일상 적용 */
        '스스로 돌아보기'                                               /* 자기 평가 */
    ];

    var FOCUS = '감정 표현';        /* 모달 첫 줄의 '1순위 목표' */

    /* 주차(0부터)가 속한 단계 이름 — 12주 목록에 소제목을 끼울 자리를 정한다 */
    function stageNameAt(idx) {
        var wk = 0;
        for (var i = 0; i < KD_STAGES.length; i++) {
            wk += KD_STAGES[i][1];
            if (idx < wk) return KD_STAGES[i][0];
        }
        return '';
    }

    /* 현재 몇 주차인지 — 학습 이력이 없어 계산할 근거가 없다. 사용 단계로 대신한다.
       0 신규=시작 전 · 1 시작함=1주차 · 2 익숙함=예시값 7주차. 백엔드가 붙으면 여기만 바꾼다 */
    function stage() { return +(document.documentElement.dataset.kdStage || 2); }
    function curWeek() { return [0, 1, 7][stage()]; }

    /* 2026-08-10 요청: 상태는 **완료 / 미완료 둘뿐**이다. '진행중'은 뺐다 —
       진행 중인 주차도 아직 안 끝난 것이므로 미완료로 묶인다. */
    var CHIP = { done: '완료', soon: '미완료' };

    function stateOf(idx, cur) {          /* idx 는 0부터 */
        return idx + 1 < cur ? 'done' : 'soon';
    }

    function liOf(no, topic, st) {
        var li = document.createElement('li');
        li.innerHTML = '<span class="no no-' + st + '">' + no + '</span>'
            + '<span class="t"></span>'
            + '<span class="chip chip-' + st + '">' + CHIP[st] + '</span>';
        li.querySelector('.t').textContent = topic;
        return li;
    }

    function renderDash() {
        var short = document.getElementById('dashPlanList');
        if (!short) return;
        var cur = curWeek();

        /* 카드에는 진행 중 주차 앞뒤로 잘라서 보여 준다.
           줄 수는 **옆 카드 높이에 맞춰** 정한다 — 짧으면 로드맵 카드만 작아져 나란히 안 맞는다
           (2026-08-10 지적: 1단계에서 왼쪽 500 / 로드맵 346 으로 어긋나 있었다).
           0단계 8줄(620) · 1단계 7줄(560) · 2단계는 위에 지표행이 있어 4줄(346). */
        var n = stage() === 0 ? 8 : (stage() === 1 ? 7 : 4);
        var start = cur === 0 ? 0 : Math.max(0, Math.min(cur - 3, KD_WEEKS.length - n));
        short.textContent = '';
        KD_WEEKS.slice(start, start + n).forEach(function (topic, i) {
            var idx = start + i;
            short.appendChild(liOf(idx + 1, topic, stateOf(idx, cur)));
        });

        var foot = document.getElementById('dashPlanFoot');
        if (foot) {
            foot.textContent = KD_WEEKS.length + '주 과정 중 '
                + (cur === 0 ? '1주차 시작 전' : cur + '주차')
                + ' · 이번 주 ' + [0, 1, 2][stage()] + ' / 3 완료';
        }

        /* 전체 보기 모달은 12주를 다 보여 준다 — 여기에만 단계 소제목을 끼운다 */
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
                full.appendChild(liOf(i + 1, topic, stateOf(i, cur)));
            });
        }

        var lead = document.getElementById('dlgPlanLead');
        if (lead) {
            lead.textContent = '1순위 목표는 ' + FOCUS + '예요. '
                + (cur === 0 ? '아직 시작 전이에요.' : '지금은 ' + cur + '주차예요.');
        }
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', renderDash);
    else renderDash();
})();
