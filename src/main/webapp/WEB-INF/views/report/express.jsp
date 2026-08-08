<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "express"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 수치는 전부 예시값 — 백엔드가 붙으면 그대로 갈아끼운다.
     고정값 표: 표현 64 ▲4 · 8주 누적 +16 (다른 리포트 화면·대시보드와 일치해야 함) --%>
<div class="app-head rpt-head">
    <h1>지우의 성장 리포트</h1>
    <p>관찰 8주차 · 매주 월요일에 갱신돼요</p>
</div>

<div class="rpt-tabsrow">
    <%@ include file="../common/report-tabs.jsp" %>
</div>

<div class="rpt-score">
    <div>
        <p class="lb">감정 표현 지수</p>
        <p class="v">64<span class="u">%</span><b class="delta rpt-up">▲4 · 지난주 대비</b></p>
    </div>
</div>

<section class="rpt-sec">
    <h2>표현 방법의 변화</h2>
    <%-- Figma(24:16727)는 두 막대가 46/32/22 로 복붙돼 있어 '변화'가 안 보인다.
         4주 전 값만 다르게 넣었다(몸짓↓ 말↑ = "몸짓 대신 말" 서사와 일치). 팀장 확인 대상 --%>
    <div class="rpt-stackrow">
        <p class="cap">4주 전</p>
        <div class="rpt-stack">
            <span class="st-talk" style="width:28%">28%</span>
            <span class="st-face" style="width:38%">38%</span>
            <span class="st-body" style="width:34%">34%</span>
        </div>
    </div>
    <div class="rpt-stackrow">
        <p class="cap">이번 주</p>
        <div class="rpt-stack">
            <span class="st-talk" style="width:46%">46%</span>
            <span class="st-face" style="width:32%">32%</span>
            <span class="st-body" style="width:22%">22%</span>
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
    <%-- 막대 높이 = 값 × 3.2px (1024 한 화면에 담기게 축소) --%>
    <div class="rpt-bars">
        <div class="rpt-bar"><b>32</b><span class="em-joy"  style="height:102px"></span><em>기쁨</em></div>
        <div class="rpt-bar"><b>24</b><span class="em-sad"  style="height:77px"></span><em>슬픔</em></div>
        <div class="rpt-bar"><b>18</b><span class="em-mad"  style="height:58px"></span><em>화남</em></div>
        <div class="rpt-bar"><b>12</b><span class="em-sup"  style="height:38px"></span><em>놀람</em></div>
        <div class="rpt-bar"><b>9</b><span class="em-help" style="height:29px"></span><em>도움요청</em></div>
    </div>
</section>

<hr class="rpt-rule">

<%-- Figma 는 이 아래가 빈 공간 — 학습 유도 배너는 웹에서 추가한 것 --%>
<div class="rpt-cta">
    <span class="ic"></span>
    <p>몸짓 대신 말로 표현하는 순간이 늘었어요 · 다음 이야기에서 '속상해' 말하기를 연습해요</p>
    <a class="kd-btn kd-btn-primary" href="/learn">학습 홈 가기</a>
</div>

<%@ include file="../common/app-bottom.jsp" %>
