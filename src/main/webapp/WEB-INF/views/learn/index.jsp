<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "학습 홈"; String appNav = "learn"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 칩·표·문구는 전부 Figma 예시값이다. 백엔드가 붙으면 그대로 갈아끼운다.
     Figma 원본(307:871)은 본문이 1115 라 하단 배너가 프레임 밖으로 잘려 있다.
     웹에서는 .app-main 이 세로로 스크롤되므로 잘리지 않고 끝까지 보인다. --%>
<div class="app-head app-head-lg">
    <h1>학습 홈</h1>
    <p>오늘 학습을 시작하고 일상을 기록해요</p>
</div>

<div class="learn-hero">
    <%-- 배경은 사용자가 제공한 수채화 그림(learn-hero-bg2.jpg), 캐릭터는 기존 사람 토리를 겹친다.
         제공 그림에는 아이가 없어서 배경만 쓰면 배너가 비어 보인다. --%>
    <span class="art">
        <img class="bg" src="/img/learn-hero-bg2.jpg?v=130" alt="">
        <img class="ch" data-kd-char="wave" src="/img/char-tori-wave.png" alt="">
    </span>
    <div class="txt">
        <h2>AI 스토리 학습</h2>
        <p>AI가 만든 상황 이야기로 감정을 배워요</p>
        <%-- 2026-08-10 피드백 8 — "매일 하는 건지 루틴인지 기준이 있냐"는 지적.
             확정: **하루 한 편 권장**. 이 기준을 전 화면 문구가 따른다. --%>
        <p class="min">예상 소요 약 7분 · 하루 한 편 권장</p>
        <%-- 2026-08-10 피드백 7 — "장애 종류에 따라 학습을 다르게 하는지, 왜 구분했는지".
             팀장 확정: 자폐면 사회성·감정 표현 시나리오를 더 넣는다. 화면은 새로 만들지 않고
             고른 유형이 학습에 반영된다는 사실만 여기서 밝힌다. 아래 스크립트가 켠다. --%>
        <p class="learn-fit" id="learnFit" hidden></p>
        <a class="kd-btn kd-btn-primary" href="/story/scene">이야기 시작하기</a>
    </div>
</div>

<div class="learn-row">
    <section class="learn-daily">
        <h2 class="learn-sec">오늘의 일상 입력</h2>

        <%-- 기록 전 상태 — 버튼을 누르면 모달이 열린다.
             전에는 여기 textarea 가 바로 박혀 있어 '메모장'처럼 보였다. --%>
        <div class="learn-entry" id="dailyEmpty">
            <span class="ic ic-pen"></span>
            <p class="t">오늘은 아직 기록하지 않았어요</p>
            <p class="d">오늘 있었던 일을 남기면 아이에게 딱 맞는 이야기를 만들어요</p>
            <button type="button" class="kd-btn kd-btn-primary" onclick="dlgDaily.showModal()">오늘의 일상 기록하기</button>
        </div>

        <%-- 기록 후 상태 — Figma 에 없는 추가분.
             "다음으로 넘어가는 느낌"을 만들기 위한 블록이라 통째로 빼기 쉽게 분리해 뒀다. --%>
        <div class="learn-entry learn-entry-done" id="dailyDone" hidden style="cursor:pointer" onclick="kdDailyPeek(event)">
            <span class="ic ic-check"></span>
            <p class="t">오늘 기록을 남겼어요</p>
            <p class="d" id="dailySummary"></p>
            <%-- 카드를 누르면 펼쳐지는 원문. 요약은 40자에서 잘리니 여기서 전체를 보여 준다. --%>
            <p class="d" id="dailyFull" hidden></p>
            <div class="row">
                <button type="button" class="kd-btn kd-btn-outline" onclick="kdDailyNew()">다시 쓰기</button>
                <%-- 고른 감정이 그대로 학습 흐름의 감정 벌로 이어진다 — href 는 kdDailySave() 가 채운다 --%>
                <a class="kd-btn kd-btn-primary" id="dailyStart" href="/story/scene">이 이야기로 학습 시작</a>
            </div>
        </div>
    </section>

    <section class="learn-recent">
        <h2 class="learn-sec">최근 학습 기록</h2>
        <%-- 기록이 0건일 때 — 표 헤더만 덩그러니 남으면 남의 화면처럼 보인다 --%>
        <div class="kd-empty kd-no-data">
            <span class="ic kd-empty-ic-list"></span>
            <p class="t">아직 학습 기록이 없어요</p>
            <p class="d">이야기를 한 편 마치면 날짜와 결과가 여기에 남아요</p>
        </div>

        <table class="kd-hide0">
            <thead>
            <tr><th class="c-no">No</th><th class="c-date">날짜</th><th class="c-story">스토리</th><th class="c-state">상태</th></tr>
            </thead>
            <%-- 1단계 — 이야기 한 편만 마친 상태 --%>
            <tbody class="kd-s1">
            <tr><td class="c-no">1</td><td class="c-date">오늘</td><td class="c-story">친구가 내 블록을 무너뜨렸어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            </tbody>
            <tbody class="kd-has-data">
            <tr><td class="c-no">1</td><td class="c-date">7.22</td><td class="c-story">친구가 내 블록을 무너뜨렸어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">2</td><td class="c-date">7.21</td><td class="c-story">처음 간 곳에서 길을 잃을 뻔했어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">3</td><td class="c-date">7.19</td><td class="c-story">놀이터에서 차례를 기다렸어요</td><td class="c-state"><span class="chip chip-cont">미완료</span></td></tr>
            <tr><td class="c-no">4</td><td class="c-date">7.18</td><td class="c-story">동생이 내 장난감을 가져갔어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">5</td><td class="c-date">7.16</td><td class="c-story">블록으로 높은 탑을 쌓았어요</td><td class="c-state"><span class="chip chip-cont">미완료</span></td></tr>
            </tbody>
        </table>
    </section>
</div>

<div class="learn-flow">
    <h2 class="learn-sec learn-sec-plain">스토리 학습은 이렇게 진행돼요</h2>
    <ol>
        <li><span class="no">1</span><b>상황 이야기 제시</b><span class="ds">AI가 상황을 이야기 형태로 제시</span></li>
        <li><span class="no">2</span><b>표정·동작·음성 반응 인식</b><span class="ds">아동의 반응을 분석하여 이해도 파악</span></li>
        <li><span class="no">3</span><b>분기 진행 / 코칭 피드백</b><span class="ds">반응에 따라 스토리가 분기되고 코칭 제공</span></li>
        <li><span class="no">4</span><b>세션 결과 요약</b><span class="ds">이번 학습의 결과를 정리</span></li>
        <li><span class="no">5</span><b>성장 리포트 반영</b><span class="ds">학습 결과가 성장 리포트에 반영</span></li>
    </ol>
</div>

<div class="learn-cta">
    <span class="ic"></span>
    <%-- 세 문구 모두 '하루 한 편' 기준으로 통일했다(2026-08-10 피드백 8).
         전에는 다음에 뭘 하면 되는지가 kd-has-data 에만 있었다. --%>
    <p class="kd-has-data" id="ctaDone">오늘 한 편을 마쳤어요 · 내일 새 이야기가 열려요 (지난 기록은 그대로 남아요)</p>
    <p class="kd-no-data">하루 한 편이 기본이에요 · 오늘의 첫 이야기를 시작해 볼까요?</p>
    <p class="kd-s1">오늘 기록을 남겼어요 · 이어서 오늘의 한 편을 해 볼까요?</p>
    <%-- 오늘 몫을 끝낸 뒤에도 더 할 수 있다 — 막지 않고 글자만 바꾼다.
         ⚠ 버튼 자체를 두 개로 두면 `.learn-cta .kd-btn` 이 2개로 잡혀
           숨은 쪽이 먼저 걸린다(검증 스크립트가 클릭 못 함). 버튼은 하나, 글자만 교체.
         .kd-hide2 = 0·1 단계에서만 보임 (kkeudeok.css 단계 규칙) --%>
    <a class="kd-btn kd-btn-primary" href="/story/scene">
        <span class="kd-hide2">학습 시작</span><span class="kd-has-data">한 번 더 하기</span>
    </a>
</div>

<%-- 오늘의 일상 입력 모달 (Figma 24:20619).
     .terms-dialog 를 같이 붙여야 backdrop 과 zoom 상쇄를 물려받는다 — 빼면 배율이 두 번 걸려 작아진다.
     상황 칩은 Figma 서버 렌더에는 없지만 사용자 요청으로 모달 안에 넣었다. --%>
<dialog id="dlgDaily" class="terms-dialog daily-modal">
    <div class="hd">
        <h2>오늘의 일상 입력</h2>
        <button type="button" class="x" aria-label="닫기" onclick="dlgDaily.close()"></button>
    </div>

    <div class="bd">
        <h3>오늘은 어떤 일이 있었나요?</h3>

        <%-- 2026-08-10 피드백 6 — 상황 칩(유치원·친구랑 다퉜어…) 4개를 걷어냈다.
             무엇을 고르는 칸인지 안 읽혀 헷갈린다는 지적이었고, 실제 역할은
             아래 메모의 예시 문구를 바꾸는 것뿐이라 지워도 잃는 기능이 없다.
             대신 남은 두 칸에 라벨을 달아 무엇을 넣는 칸인지 분명히 했다. --%>
        <p class="daily-lb">아이의 기분</p>

        <div class="daily-emos" id="dailyEmos">
            <label><img src="/img/face-happy.png" alt=""><span class="nm">기쁨</span><input type="radio" name="emotion" value="기쁨" checked></label>
            <label><img src="/img/face-sad.png" alt=""><span class="nm">슬픔</span><input type="radio" name="emotion" value="슬픔"></label>
            <label><img src="/img/face-angry.png" alt=""><span class="nm">화남</span><input type="radio" name="emotion" value="화남"></label>
            <label><img src="/img/face-surprise.png" alt=""><span class="nm">놀람</span><input type="radio" name="emotion" value="놀람"></label>
            <label><img src="/img/face-neutral.png" alt=""><span class="nm">무표정</span><input type="radio" name="emotion" value="무표정"></label>
        </div>

        <%-- 팀장 확정: 일일 회고는 선택 입력이다("꼭 안 적어도 됨") --%>
        <p class="daily-lb">무슨 일이 있었나요? <span class="opt">선택 · 안 적어도 괜찮아요</span></p>

        <div class="daily-memo">
            <textarea id="dailyText" maxlength="200" aria-label="오늘 있었던 일"
                      placeholder="유치원에서 선생님이 칭찬해 주셔서 어깨가 으쓱했어요"></textarea>
            <span class="daily-count" id="dailyCount">0 / 200</span>
        </div>
    </div>

    <div class="ft">
        <button type="button" class="kd-btn kd-btn-outline" onclick="dlgDaily.close()">취소</button>
        <button type="button" class="kd-btn kd-btn-primary" onclick="kdDailySave()">저장</button>
    </div>
</dialog>

<script>
    /* 상황 칩이 사라지면서 예시 문구 표(KD_DAILY)도 같이 걷어냈다 —
       칩의 유일한 역할이 placeholder 를 바꾸는 것이었다. 이제 예시는 고정 한 개다. */
    (function () {
        var text = document.getElementById('dailyText');
        var count = document.getElementById('dailyCount');

        text.addEventListener('input', function () {
            count.textContent = text.value.length + ' / 200';
        });
    })();

    /* '다시 쓰기'는 말 그대로 새로 쓰는 것 — 지난 내용을 비우고 연다 (2026-08-10 지적). */
    function kdDailyNew() {
        document.getElementById('dailyText').value = '';
        document.getElementById('dailyCount').textContent = '0 / 200';
        dlgDaily.showModal();
    }

    /* 기록 카드를 누르면 원문을 폈다 접는다. 카드 안의 버튼·링크 클릭은 그대로 통과시킨다. */
    function kdDailyPeek(e) {
        if (e.target.closest('button, a')) return;
        var full = document.getElementById('dailyFull');
        if (!full.textContent) return;      /* 메모는 선택 입력 — 비어 있으면 펼칠 게 없다 */
        full.hidden = !full.hidden;
    }

    function kdDailySave() {
        var emo = document.querySelector('#dailyEmos input:checked');
        var text = document.getElementById('dailyText');

        /* ⚠ 예전엔 `text.value.trim() || text.placeholder` 였다 — 빈칸으로 저장하면
           예시문("유치원에서 선생님이 칭찬…")이 **진짜 기록인 것처럼** 카드에 박혔다.
           메모는 선택 입력이므로 비었으면 비운 채로 둔다. */
        var memo = text.value.trim();
        var full = document.getElementById('dailyFull');

        full.textContent = memo ? '"' + memo + '"' : '';
        full.hidden = true;

        /* 요약 한 줄에 통째로 붙이던 탓에 길게 쓰면 카드가 늘어지고 글이 흘러넘쳤다
           (2026-08-10 지적). 여기서는 앞부분만 보여 준다 — 원문은 카드를 누르면 펼쳐진다. */
        var shortMemo = memo.length > 40 ? memo.slice(0, 40).trim() + '…' : memo;

        document.getElementById('dailySummary').textContent =
            shortMemo ? emo.value + ' · "' + shortMemo + '"' : emo.value;

        /* 고른 감정 → 학습1 감정 벌. 슬픔·놀람·무표정은 아직 벌이 없어 슬픔으로 보낸다. */
        var KD_EMO = { '기쁨': 'happy', '화남': 'angry' };
        document.getElementById('dailyStart').href =
            '/story/scene?emo=' + (KD_EMO[emo.value] || 'sad');

        document.getElementById('dailyEmpty').hidden = true;
        document.getElementById('dailyDone').hidden = false;
        dlgDaily.close();

        /* 실제로 기록을 남겼으면 '아무것도 없음(0)' 은 더 이상 맞지 않다 -> 1단계로 올린다.
           이야기를 끝까지 마치면 story.js 가 localStorage.kdDone 을 올려 **한 편에 2단계**가 된다
           (2026-08-10 요청으로 3회 → 1회).
           ⚠ 예전엔 여기서 sessionStorage.kdStage 에 썼다 — 그건 head.jsp 의 **시연 스위치**라
             한 번 쓰면 학습 횟수 계산을 영영 가로챈다. 자기 키(kdDaily)로 분리했다. */
        if (document.documentElement.dataset.kdStage === '0') {
            try { sessionStorage.setItem('kdDaily', '1'); } catch (e) { }
            document.documentElement.dataset.kdStage = '1';
        }
    }
</script>

<script>
    /* 1단계는 '이미 오늘 기록을 남긴' 상태다 — 두 카드의 hidden 을 단계에 맞춰 정한다.
       (0·2 단계는 JSP 기본값 그대로 = 아직 기록 안 함) */
    if (document.documentElement.dataset.kdStage === '1') {
        document.getElementById('dailySummary').textContent =
            '기쁨 · "선생님이 칭찬해 주셔서 어깨가 으쓱했어요"';
        document.getElementById('dailyFull').textContent =
            '"선생님이 칭찬해 주셔서 어깨가 으쓱했어요"';
        document.getElementById('dailyEmpty').hidden = true;
        document.getElementById('dailyDone').hidden = false;
    }
</script>

<script>
    /* 고른 장애 유형이 학습에 실제로 반영된다는 것을 화면이 말하게 한다(2026-08-10 피드백 7).
       팀장 확정: 자폐면 사회성·감정 표현 시나리오를 더 넣는다. 화면은 새로 만들지 않는다.
       유형은 온보딩이 sessionStorage.kdOnb 에 문자열 하나로 넣어 둔다.
       concat 으로 받는 건 중복 진단 시절의 옛 저장값(배열)이 아직 남아 있을 수 있어서다.
       ponytail: 백엔드가 붙으면 이 판단은 서버가 내려주면 된다. */
    (function () {
        var types = [];
        try {
            types = [].concat(JSON.parse(sessionStorage.getItem('kdOnb') || '{}').disType || []);
        } catch (e) { }
        if (types.indexOf('자폐 장애') < 0) return;

        var fit = document.getElementById('learnFit');
        fit.textContent = '자폐 유형에 맞춰 사회성·감정 표현 이야기가 더 들어가요';
        fit.hidden = false;

        /* 오늘 몫을 끝냈을 때 '내일 뭘 하는지'까지 알려 준다 */
        document.getElementById('ctaDone').textContent =
            '오늘 한 편을 마쳤어요 · 내일은 \'놀이터에서 차례 기다리기\'(사회성) 이야기예요';
    })();
</script>

<%@ include file="../common/app-bottom.jsp" %>
