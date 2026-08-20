<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "express"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 수치는 전부 예시값 — 백엔드가 붙으면 그대로 갈아끼운다.
     고정값 표: 표현 64 ▲4 · 8주 누적 +16 (다른 리포트 화면·대시보드와 일치해야 함) --%>
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
<%-- 2026-08-10 피드백 5 — 오른 이유를 숫자로.
     ⚠ 16/25 = 64%, 12/20 = 60% — 지수·▲4 와 산수가 맞다. 대시보드 KPI 도 +4%p 로 맞춰 뒀다. --%>
<p class="rpt-basis" data-rpt-basis="express"></p>

<section class="rpt-sec">
    <h2>표현 방법의 변화</h2>
    <%-- Figma(24:16727)는 두 막대가 46/32/22 로 복붙돼 있어 '변화'가 안 보인다.
         4주 전 값만 다르게 넣었다(몸짓↓ 말↑ = "몸짓 대신 말" 서사와 일치). 팀장 확인 대상 --%>
    <%-- ⚠ Figma 원본은 경계에서 색이 섞이는 연속 그라데이션이라 막대 전체에 하나를 깔았었다.
         그런데 색 경계가 실측값으로 고정이라 실제 비율과 어긋났다 — 50·10·40 인데 색은
         42·74 에서 바뀌어 숫자가 남의 색 위에 얹혀 보였다(2026-08-20 지적).
         지금은 조각마다 제 색을 갖는다(kd-report.js 의 METHOD_COLOR). 되돌리지 말 것. --%>
    <%-- 띠는 실제 미션 기록으로 그린다(kd-report.js) — 성공 여부가 아니라 '무엇으로 표현했는지' 다 --%>
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
    <%-- 막대 높이 = 값 × 4.5px (CTA 배너를 걷어낸 만큼 되키웠다 — 하단 레일 940) --%>
    <div class="rpt-bars" id="rptExpressBars"></div>
</section>

<%@ include file="../common/app-bottom.jsp" %>
