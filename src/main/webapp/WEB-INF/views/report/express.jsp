<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "express"; %>
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
        <p class="lb">감정 표현 지수</p>
        <p class="v" data-rpt="express">-<span class="u">%</span><b class="delta"></b></p>
    </div>
</div>

<p class="rpt-basis" data-rpt-basis="express"></p>
<section class="rpt-sec">
    <h2>표현 방법의 변화</h2>
    <div class="rpt-stackrow">
        <p class="cap">지난주</p>
        <div class="rpt-stack rpt-stack--prev" id="rptMethodPrev"></div>
    </div>
    <div class="rpt-stackrow">
        <p class="cap">이번 주</p>
        <div class="rpt-stack rpt-stack--now" id="rptMethodNow"></div>
    </div>
    <div class="rpt-legend">
        <span><i style="background:#34a36a"></i>말</span>
        <span><i style="background:#3d8fe0"></i>표정</span>
        <span><i style="background:#0fa394"></i>몸짓</span>
    </div>
</section>

<section class="rpt-sec">
    <h2>감정별 표현 빈도</h2>
    <div class="rpt-bars" id="rptExpressBars"></div>
</section>

<%@ include file="../common/app-bottom.jsp" %>
