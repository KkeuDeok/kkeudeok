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
        <p class="v" data-rpt="understand">-<span class="u">%</span><b class="delta"></b></p>
    </div>
</div>
<%-- 지수가 "왜 올랐는지"가 화면에 없다는 지적(2026-08-10 피드백 5).
     계산이 되는 숫자를 그대로 보여 준다 — 문장은 서버가 만들어 내려보낸다(ReportService).
     ⚠ 여기에 숫자를 다시 박지 말 것. 지수와 산수가 어긋나는 순간 리포트를 못 믿게 된다. --%>
<p class="rpt-basis" data-rpt-basis="understand"></p>

<section class="rpt-sec">
    <h2>감정별 이해도</h2>
    <%-- 막대는 실제 고르기 미션 결과로 그린다(kd-report.js). 기록이 없으면 안내 한 줄만 남는다 --%>
    <div class="rpt-hbars" id="rptEmotionBars"></div>
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
