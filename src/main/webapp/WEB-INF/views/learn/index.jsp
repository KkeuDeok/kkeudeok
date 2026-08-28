<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "학습 홈"; String appNav = "learn"; %>
<%@ include file="../common/app-top.jsp" %>

<div class="app-head app-head-lg">
    <h1>학습 홈</h1>
    <p>오늘 학습을 시작하고 일상을 기록해요</p>
</div>

<div class="learn-hero">
    <span class="art">
        <img class="bg" src="/img/learn-hero-bg2.jpg?v=130" alt="">
        <img class="ch" data-kd-char="wave" src="/img/char-tori-wave.png" alt="">
    </span>
    <div class="txt">
        <h2>AI 스토리 학습</h2>
        <p>AI가 만든 상황 이야기로 감정을 배워요</p>
        <p class="min">하루 세 편 권장</p>
        <a class="kd-btn kd-btn-primary" href="/story/scene">이야기 시작하기</a>
    </div>
</div>
<div class="learn-row">
    <section class="learn-daily">
        <h2 class="learn-sec">오늘의 일상 입력 <span class="sec-opt">선택</span></h2>

        <div class="learn-entry" id="dailyEmpty">
            <span class="ic ic-pen"></span>
            <p class="t">오늘은 아직 기록하지 않았어요</p>
            <p class="d">오늘 있었던 일을 남기면 아이에게 딱 맞는 이야기를 만들어요</p>
            <button type="button" class="kd-btn kd-btn-primary" onclick="dlgDaily.showModal()">오늘의 일상 기록하기</button>
        </div>

        <div class="learn-entry learn-entry-done" id="dailyDone" hidden style="cursor:pointer" onclick="kdDailyPeek(event)">
            <span class="ic ic-check"></span>
            <p class="t">오늘 기록을 남겼어요</p>
            <p class="d" id="dailySummary"></p>
            <p class="d" id="dailyFull" hidden></p>
            <div class="row">
                <button type="button" class="kd-btn kd-btn-outline" onclick="kdDailyNew()">다시 쓰기</button>
                <a class="kd-btn kd-btn-primary" id="dailyStart" href="/story/scene">이 이야기로 학습 시작</a>
            </div>
        </div>
    </section>

    <section class="learn-recent">
        <h2 class="learn-sec">최근 학습 기록</h2>
        <div class="kd-empty kd-no-data">
            <span class="ic kd-empty-ic-list"></span>
            <p class="t">아직 학습 기록이 없어요</p>
            <p class="d">이야기를 한 편 마치면 날짜와 결과가 여기에 남아요</p>
        </div>

        <table id="learnRecentTable" hidden>
            <thead>
            <tr><th class="c-no">No</th><th class="c-date">날짜</th><th class="c-story">스토리</th><th class="c-state">상태</th></tr>
            </thead>
            <tbody id="learnRecentBody"></tbody>
        </table>
    </section>
</div>

<div class="learn-flow">
    <h2 class="learn-sec learn-sec-plain">스토리 학습은 이렇게 진행돼요</h2>
    <ol>
        <li><span class="no">1</span><b>상황 이야기 제시</b><span class="ds">AI가 상황을 이야기 형태로 제시</span></li>
        <li><span class="no">2</span><b>표정·동작·음성 반응 인식</b><span class="ds">아동의 반응을 분석하여 이해도 파악</span></li>
        <li><span class="no">3</span><b>분기 진행 / 코칭 피드백</b><span class="ds">반응에 따라 스토리가 분기되고 코칭 제공</span></li>
        <li><span class="no">4</span><b>성장 리포트 반영</b><span class="ds">학습 결과가 성장 리포트에 반영</span></li>
    </ol>
</div>

<dialog id="dlgDaily" class="terms-dialog daily-modal">
    <div class="hd">
        <h2>오늘의 일상 입력</h2>
        <button type="button" class="x" aria-label="닫기" onclick="dlgDaily.close()"></button>
    </div>
    <div class="bd">
        <h3>오늘은 어떤 일이 있었나요?</h3>

        <div class="daily-emos" id="dailyEmos">
            <label><img src="/img/face-happy.png" alt=""><span class="nm">기쁨</span><input type="radio" name="emotion" value="기쁨" checked></label>
            <label><img src="/img/face-sad.png" alt=""><span class="nm">슬픔</span><input type="radio" name="emotion" value="슬픔"></label>
            <label><img src="/img/face-angry.png" alt=""><span class="nm">화남</span><input type="radio" name="emotion" value="화남"></label>
            <label><img src="/img/face-surprise.png" alt=""><span class="nm">놀람</span><input type="radio" name="emotion" value="놀람"></label>
            <label><img src="/img/face-neutral.png" alt=""><span class="nm">무표정</span><input type="radio" name="emotion" value="무표정"></label>
        </div>
        <p class="daily-lb">무슨 일이 있었나요?</p>

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
    (function () {
        var text = document.getElementById('dailyText');
        var count = document.getElementById('dailyCount');

        text.addEventListener('input', function () {
            count.textContent = text.value.length + ' / 200';
        });
    })();

    function kdDailyNew() {
        document.getElementById('dailyText').value = '';
        document.getElementById('dailyCount').textContent = '0 / 200';
        dlgDaily.showModal();
    }

    function kdDailyPeek(e) {
        if (e.target.closest('button, a')) return;
        var full = document.getElementById('dailyFull');
        if (!full.textContent) return;
        full.hidden = !full.hidden;
    }

    function kdDailySave() {
        var emo = document.querySelector('#dailyEmos input:checked');
        var text = document.getElementById('dailyText');

        var memo = text.value.trim();
        var full = document.getElementById('dailyFull');

        full.textContent = memo ? '"' + memo + '"' : '';
        full.hidden = true;

        var shortMemo = memo.length > 40 ? memo.slice(0, 40).trim() + '…' : memo;

        document.getElementById('dailySummary').textContent =
            shortMemo ? emo.value + ' · "' + shortMemo + '"' : emo.value;

        var KD_EMO = { '기쁨': 'happy', '화남': 'angry', '놀람': 'surprise' };
        document.getElementById('dailyStart').href =
            '/story/scene?emo=' + (KD_EMO[emo.value] || 'sad');

        try {
            sessionStorage.setItem('kdDailyEmo', emo.value);
            sessionStorage.setItem('kdDailyNote', memo);
        } catch (e) { }
        document.getElementById('dailyEmpty').hidden = true;
        document.getElementById('dailyDone').hidden = false;
        dlgDaily.close();

        if (document.documentElement.dataset.kdStage === '0') {
            try { sessionStorage.setItem('kdDaily', '1'); } catch (e) { }
            document.documentElement.dataset.kdStage = '1';
        }
    }
</script>

<script>
    if (document.documentElement.dataset.kdStage === '1') {
        document.getElementById('dailySummary').textContent =
            '기쁨 · "선생님이 칭찬해 주셔서 어깨가 으쓱했어요"';
        document.getElementById('dailyFull').textContent =
            '"선생님이 칭찬해 주셔서 어깨가 으쓱했어요"';
        document.getElementById('dailyEmpty').hidden = true;
        document.getElementById('dailyDone').hidden = false;
    }
</script>

<%@ include file="../common/app-bottom.jsp" %>
