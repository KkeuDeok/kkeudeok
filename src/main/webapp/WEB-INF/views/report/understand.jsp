<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "understand"; %>
<%@ include file="../common/app-top.jsp" %>

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

<p class="rpt-basis" data-rpt-basis="understand"></p>
<section class="rpt-sec">
    <h2>감정별 이해도</h2>
    <div class="rpt-hbars" id="rptEmotionBars"></div>
</section>

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
