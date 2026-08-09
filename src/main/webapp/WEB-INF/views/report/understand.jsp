<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "understand"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ⚠ Figma에 이 화면은 없다(탭바에 '감정 이해'만 존재) — 기존 3화면 문법으로 신규 설계.
     이해도 5개 평균 = 72 로 지수와 일치. 장면 리스트는 학습 홈 최근 기록과 동일 데이터.
     ponytail: 수치는 전부 예시값. --%>
<div class="app-head rpt-head">
    <h1><span data-kd="childCall">지우</span>의 성장 리포트</h1>
    <p>관찰 8주차 · 매주 월요일에 갱신돼요</p>
</div>

<div class="rpt-tabsrow">
    <%@ include file="../common/report-tabs.jsp" %>
</div>
<%@ include file="../common/report-empty.jsp" %>


<div class="rpt-score">
    <div>
        <p class="lb">감정 이해 지수</p>
        <p class="v">72<span class="u">%</span><b class="delta rpt-up">▲2 · 지난주 대비</b></p>
    </div>
    <p class="note">슬픔과 놀람 표정 구분이 또렷해졌어요</p>
</div>

<section class="rpt-sec">
    <h2>감정별 이해도</h2>
    <div class="rpt-hbars">
        <div class="rpt-hbar"><span class="lb">기쁨</span><div class="track"><span class="em-joy" style="width:91%"></span></div><span class="pct">91%</span></div>
        <div class="rpt-hbar"><span class="lb">슬픔</span><div class="track"><span class="em-sad" style="width:78%"></span></div><span class="pct">78%</span></div>
        <div class="rpt-hbar"><span class="lb">놀람</span><div class="track"><span class="em-sup" style="width:70%"></span></div><span class="pct">70%</span></div>
        <div class="rpt-hbar"><span class="lb">화남</span><div class="track"><span class="em-mad" style="width:62%"></span></div><span class="pct">62%</span></div>
        <div class="rpt-hbar"><span class="lb">도움요청</span><div class="track"><span class="em-help" style="width:59%"></span></div><span class="pct">59%</span></div>
    </div>
</section>

<hr class="rpt-rule">

<section class="rpt-sec">
    <h2>이번 주 이해한 장면</h2>
    <div class="rpt-scenes">
        <div class="rpt-scene">
            <p class="t">친구가 내 블록을 무너뜨렸어요</p>
            <span class="chip">화남 이해</span>
            <span class="d">7.22</span>
        </div>
        <div class="rpt-scene">
            <p class="t">처음 간 곳에서 길을 잃을 뻔했어요</p>
            <span class="chip">놀람 이해</span>
            <span class="d">7.21</span>
        </div>
        <div class="rpt-scene">
            <p class="t">놀이터에서 차례를 기다렸어요</p>
            <span class="chip">기쁨 이해</span>
            <span class="d">7.19</span>
        </div>
    </div>
</section>

<div class="rpt-cta">
    <span class="ic"></span>
    <p>아직 어려운 감정은 '도움요청'이에요 · 다음 이야기 '친구와 다툰 날'로 연습을 이어가요</p>
    <a class="kd-btn kd-btn-primary" href="/learn">학습 홈 가기</a>
</div>

<%@ include file="../common/app-bottom.jsp" %>
