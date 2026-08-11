<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "understand"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ⚠ Figma에 이 화면은 없다(탭바에 '감정 이해'만 존재) — 기존 3화면 문법으로 신규 설계.
     이해도 5개 평균 = 72 로 지수와 일치.
     ponytail: 수치는 전부 예시값. --%>
<div class="app-head rpt-head">
    <h1><span data-kd="childCall">지우</span>의 성장 리포트</h1>
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
<%-- 지수가 "왜 올랐는지"가 화면에 없다는 지적(2026-08-10 피드백 5).
     공식만 적으면 절반만 답한 것이라 **계산이 되는 숫자**를 그대로 보여 준다.
     ⚠ 18/25 = 72%, 14/20 = 70% — 위 지수·▲2 와 산수가 맞아야 한다. 값을 바꿀 땐 같이 고칠 것. --%>
<p class="rpt-basis">이번 주 미션 25회 중 <b>18회 성공</b> (지난주 20회 중 14회) · 70% → 72%</p>

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

<%-- 문구는 하드코딩(고정). 이해도 최저 2개(도움요청·화남)에 맞춘 예시 --%>
<section class="rpt-sec">
    <h2>집에서 해볼 수 있어요</h2>
    <div class="rpt-tips">
        <div class="rpt-tip">
            <span class="chip">도움요청</span>
            <h3>"도와줘" 먼저 말해보기</h3>
            <p>블록이 안 끼워질 때 바로 도와주지 말고, 3초만 기다렸다가 "뭐라고 말하면 될까?" 하고 물어봐 주세요.</p>
        </div>
        <div class="rpt-tip">
            <span class="chip">화남</span>
            <h3>화난 마음에 이름 붙이기</h3>
            <p>아이가 화를 낼 때 "속상했구나"처럼 감정 이름을 대신 말해주면, 표정과 감정을 연결하는 데 도움이 돼요.</p>
        </div>
    </div>
</section>

<%@ include file="../common/app-bottom.jsp" %>
