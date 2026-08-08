<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "ai"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ⚠ Figma에 이 화면은 없다(탭바에 'AI 피드백'만 존재) — 신규 설계.
     사회성 ▼3(지난주) 과 주간 변화의 +8(8주 누적)은 기간이 달라 모순이 아니다.
     ponytail: 코멘트·추천은 전부 예시 문구 — 실제로는 AI가 생성할 자리. --%>
<div class="app-head rpt-head">
    <h1>지우의 성장 리포트</h1>
    <p>관찰 8주차 · 매주 월요일에 갱신돼요</p>
</div>

<div class="rpt-tabsrow">
    <%@ include file="../common/report-tabs.jsp" %>
</div>

<div class="rpt-ai">
    <span class="ava"><img src="/img/tori.png" alt=""></span>
    <div class="tx">
        <h2>토리가 본 이번 주</h2>
        <p>이번 주 지우는 속상한 마음을 말로 표현하는 순간이 눈에 띄게 늘었어요.<br>
           몸짓으로만 보여 주던 감정을 "속상해"라고 말한 장면이 세 번 있었어요.</p>
    </div>
</div>

<section class="rpt-sec">
    <h2>지표별 AI 피드백</h2>
    <div class="rpt-cards">
        <div class="rpt-card">
            <p class="lb">감정 이해</p>
            <p class="v">72%<b class="rpt-up">▲2</b></p>
            <p class="ds">슬픔·놀람 표정 구분이 또렷해졌어요</p>
        </div>
        <div class="rpt-card">
            <p class="lb">감정 표현</p>
            <p class="v">64%<b class="rpt-up">▲4</b></p>
            <p class="ds">몸짓 대신 말로 표현하는 순간이 늘었어요</p>
        </div>
        <div class="rpt-card">
            <p class="lb">사회성</p>
            <p class="v">58%<b class="rpt-down">▼3</b></p>
            <p class="ds">새 친구 앞에선 아직 긴장하지만, 차례 지키기는 편안해졌어요</p>
        </div>
    </div>
</section>

<hr class="rpt-rule">

<section class="rpt-sec">
    <h2>다음 주 추천 활동</h2>
    <div class="rpt-todo">
        <div class="rpt-todo-item">
            <span class="no">1</span>
            <span class="tx"><b>스토리 '친구와 다툰 날' 함께 읽기</b><span class="ds">화남을 알아차리고 화해까지 이어가는 연습이에요</span></span>
        </div>
        <div class="rpt-todo-item">
            <span class="no">2</span>
            <span class="tx"><b>거울 표정 놀이 5분</b><span class="ds">슬픔과 놀람을 번갈아 지어 보며 표정 차이를 익혀요</span></span>
        </div>
        <div class="rpt-todo-item">
            <span class="no">3</span>
            <span class="tx"><b>가족 보드게임 한 판</b><span class="ds">차례를 기다리는 상황을 편안한 분위기에서 반복해요</span></span>
        </div>
    </div>
</section>

<p class="rpt-note">AI 분석은 학습 기록을 바탕으로 한 참고 정보예요. 발달에 관한 판단은 전문가와 상의해 주세요.</p>

<%@ include file="../common/app-bottom.jsp" %>
