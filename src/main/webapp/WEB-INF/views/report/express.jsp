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
        <p class="v">64<span class="u">%</span><b class="delta rpt-up">▲4 · 지난주 대비</b></p>
    </div>
</div>
<%-- 2026-08-10 피드백 5 — 오른 이유를 숫자로.
     ⚠ 16/25 = 64%, 12/20 = 60% — 지수·▲4 와 산수가 맞다. 대시보드 KPI 도 +4%p 로 맞춰 뒀다. --%>
<p class="rpt-basis">이번 주 미션 25회 중 <b>16회 성공</b> (지난주 20회 중 12회) · 60% → 64%</p>

<section class="rpt-sec">
    <h2>표현 방법의 변화</h2>
    <%-- Figma(24:16727)는 두 막대가 46/32/22 로 복붙돼 있어 '변화'가 안 보인다.
         4주 전 값만 다르게 넣었다(몸짓↓ 말↑ = "몸짓 대신 말" 서사와 일치). 팀장 확인 대상 --%>
    <%-- Figma 원본은 구간마다 단색이 아니라 경계에서 색이 섞이는 연속 그라데이션이다
         (실측: 초록 #34a36a → 경계 60~80px 에서 혼합 → 파랑 #3d8fe0 → 혼합 → 청록 #199ea7).
         그래서 span 3개에 각각 색을 주지 않고 막대 전체에 그라데이션 하나를 깐다. --%>
    <div class="rpt-stackrow">
        <p class="cap">4주 전</p>
        <div class="rpt-stack rpt-stack--prev">
            <span style="width:28%">28%</span>
            <span style="width:38%">38%</span>
            <span style="width:34%">34%</span>
        </div>
    </div>
    <div class="rpt-stackrow">
        <p class="cap">이번 주</p>
        <div class="rpt-stack rpt-stack--now">
            <span style="width:46%">46%</span>
            <span style="width:32%">32%</span>
            <span style="width:22%">22%</span>
        </div>
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
    <div class="rpt-bars">
        <div class="rpt-bar"><b>32</b><span class="em-joy"  style="height:144px"></span><em>기쁨</em></div>
        <div class="rpt-bar"><b>24</b><span class="em-sad"  style="height:108px"></span><em>슬픔</em></div>
        <div class="rpt-bar"><b>18</b><span class="em-mad"  style="height:81px"></span><em>화남</em></div>
        <div class="rpt-bar"><b>12</b><span class="em-sup"  style="height:54px"></span><em>놀람</em></div>
        <div class="rpt-bar"><b>9</b><span class="em-help" style="height:40px"></span><em>도움요청</em></div>
    </div>
</section>

<%@ include file="../common/app-bottom.jsp" %>
