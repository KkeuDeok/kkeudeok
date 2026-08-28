<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "social"; %>
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
        <p class="lb">사회성 지수</p>
        <p class="v" data-rpt="social">-<span class="u">%</span><b class="delta"></b></p>
    </div>
</div>
<p class="rpt-basis" data-rpt-basis="social"></p>
<section class="rpt-sec">
    <h2>발달 영역별 지수</h2>
    <div class="rpt-radar">
        <svg width="848" height="560" viewBox="-20 -20 480 360" role="img"
             id="rptRadar" aria-label="체크리스트 영역별 지수">
            <g id="rptRadarBody"></g>
        </svg>
        <div class="rpt-radar-legend">
            <span><i class="ln"></i>온보딩 체크리스트 기준</span>
        </div>
    </div>
</section>

<%@ include file="../common/app-bottom.jsp" %>
