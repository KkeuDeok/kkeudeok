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
    <%-- 배경은 사용자가 제공한 수채화 그림(learn-hero-bg.png), 캐릭터는 기존 사람 토리를 겹친다.
         제공 그림에는 아이가 없어서 배경만 쓰면 배너가 비어 보인다. --%>
    <span class="art">
        <img class="bg" src="/img/learn-hero-bg.png" alt="">
        <img class="ch" src="/img/char-tori-full.png" alt="">
    </span>
    <div class="txt">
        <h2>AI 스토리 학습</h2>
        <p>AI가 만든 상황 이야기로 감정을 배워요</p>
        <p class="min">예상 소요 약 7분</p>
        <%-- TODO: 학습 흐름 첫 화면(섹션 07)이 아직 없어 연결할 곳이 없다 --%>
        <a class="kd-btn kd-btn-primary" href="#">이야기 시작하기</a>
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
        <div class="learn-entry learn-entry-done" id="dailyDone" hidden>
            <span class="ic ic-check"></span>
            <p class="t">오늘 기록을 남겼어요</p>
            <p class="d" id="dailySummary"></p>
            <div class="row">
                <button type="button" class="kd-btn kd-btn-outline" onclick="dlgDaily.showModal()">다시 쓰기</button>
                <%-- TODO: 학습 흐름 화면 생기면 연결 --%>
                <a class="kd-btn kd-btn-primary" href="#">이 이야기로 학습 시작</a>
            </div>
        </div>

        <p class="learn-note">기록은 이야기 생성에만 쓰이고 30일 후 자동 삭제돼요</p>
    </section>

    <section class="learn-recent">
        <h2 class="learn-sec">최근 학습 기록</h2>
        <table>
            <thead>
            <tr><th class="c-no">No</th><th class="c-date">날짜</th><th class="c-story">스토리</th><th class="c-state">상태</th></tr>
            </thead>
            <tbody>
            <tr><td class="c-no">1</td><td class="c-date">7.22</td><td class="c-story">친구가 내 블록을 무너뜨렸어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">2</td><td class="c-date">7.21</td><td class="c-story">처음 간 곳에서 길을 잃을 뻔했어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">3</td><td class="c-date">7.19</td><td class="c-story">놀이터에서 차례를 기다렸어요</td><td class="c-state"><span class="chip chip-alt">다른 방식 선택</span></td></tr>
            <tr><td class="c-no">4</td><td class="c-date">7.18</td><td class="c-story">동생이 내 장난감을 가져갔어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">5</td><td class="c-date">7.16</td><td class="c-story">블록으로 높은 탑을 쌓았어요</td><td class="c-state"><span class="chip chip-cont">이어서 하기</span></td></tr>
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
    <p>이번 주 학습을 3번 함께했어요 · 다음 이야기는 '친구와 다툰 날'이에요</p>
    <%-- TODO: 학습 흐름 화면 생기면 연결 --%>
    <a class="kd-btn kd-btn-primary" href="#">학습 시작</a>
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

        <div class="learn-chips" id="dailyChips">
            <label><input type="radio" name="situation" value="유치원" checked><span>유치원</span></label>
            <label><input type="radio" name="situation" value="친구랑 다퉜어"><span>친구랑 다퉜어</span></label>
            <label><input type="radio" name="situation" value="가족 나들이"><span>가족 나들이</span></label>
            <label><input type="radio" name="situation" value="새로운 곳"><span>새로운 곳</span></label>
        </div>

        <div class="daily-emos" id="dailyEmos">
            <label><img src="/img/face-happy.png" alt=""><span class="nm">기쁨</span><input type="radio" name="emotion" value="기쁨" checked></label>
            <label><img src="/img/face-sad.png" alt=""><span class="nm">슬픔</span><input type="radio" name="emotion" value="슬픔"></label>
            <label><img src="/img/face-angry.png" alt=""><span class="nm">화남</span><input type="radio" name="emotion" value="화남"></label>
            <label><img src="/img/face-surprise.png" alt=""><span class="nm">놀람</span><input type="radio" name="emotion" value="놀람"></label>
            <label><img src="/img/face-neutral.png" alt=""><span class="nm">무표정</span><input type="radio" name="emotion" value="무표정"></label>
        </div>

        <div class="daily-memo">
            <textarea id="dailyText" maxlength="200" placeholder="유치원에서 선생님이 칭찬해 주셔서 어깨가 으쓱했어요"></textarea>
            <span class="daily-count" id="dailyCount">0 / 200</span>
        </div>
    </div>

    <div class="ft">
        <button type="button" class="kd-btn kd-btn-outline" onclick="dlgDaily.close()">취소</button>
        <button type="button" class="kd-btn kd-btn-primary" onclick="kdDailySave()">저장</button>
    </div>
</dialog>

<script>
    /* 상황 칩 -> 예시 문구.
       감정은 칩을 따라 바뀌지 않는다 — 기본값 기쁨으로 두고 보호자가 직접 고른다(사용자 요청).
       ponytail: 백엔드가 붙으면 이 표는 서버에서 내려주면 된다. */
    var KD_DAILY = {
        '유치원':        '유치원에서 선생님이 칭찬해 주셔서 어깨가 으쓱했어요',
        '친구랑 다퉜어': '블록놀이하다 민수가 내 성을 무너뜨려서 속상했어요',
        '가족 나들이':   '할머니 댁에 갔는데 강아지가 갑자기 짖어서 깜짝 놀랐어요',
        '새로운 곳':     '처음 간 수영장이 너무 시끄러워서 나가고 싶어 했어요'
    };

    (function () {
        var text = document.getElementById('dailyText');
        var count = document.getElementById('dailyCount');

        document.getElementById('dailyChips').addEventListener('change', function (e) {
            var ex = KD_DAILY[e.target.value];
            if (ex) text.placeholder = ex;
        });

        text.addEventListener('input', function () {
            count.textContent = text.value.length + ' / 200';
        });
    })();

    function kdDailySave() {
        var chip = document.querySelector('#dailyChips input:checked');
        var emo = document.querySelector('#dailyEmos input:checked');
        var text = document.getElementById('dailyText');
        var memo = text.value.trim() || text.placeholder;

        document.getElementById('dailySummary').textContent =
            chip.value + ' · ' + emo.value + ' · "' + memo + '"';
        document.getElementById('dailyEmpty').hidden = true;
        document.getElementById('dailyDone').hidden = false;
        dlgDaily.close();
    }
</script>

<%@ include file="../common/app-bottom.jsp" %>
