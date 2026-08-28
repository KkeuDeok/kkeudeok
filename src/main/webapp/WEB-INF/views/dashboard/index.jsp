<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "메인 대시보드"; String appNav = "dashboard"; %>
<%@ include file="../common/app-top.jsp" %>

<div class="app-head">
    <h1><span data-kd="childCall">지우</span>의 이번 주</h1>
</div>

<div class="dash-kpis">
    <div class="dash-kpi">
        <p class="lb">감정 표현</p>
        <p class="v" data-rpt="express">-<span class="u">%</span></p>
        <div class="track"><span class="c-express" data-rpt-bar="express"></span></div>
        <p class="cap">지난주 대비 <b data-rpt-delta="express"></b></p>
    </div>
    <div class="dash-kpi">
        <p class="lb">감정 이해</p>
        <p class="v" data-rpt="understand">-<span class="u">%</span></p>
        <div class="track"><span class="c-understand" data-rpt-bar="understand"></span></div>
        <p class="cap">지난주 대비 <b data-rpt-delta="understand"></b></p>
    </div>
    <div class="dash-kpi">
        <p class="lb">사회성</p>
        <p class="v" data-rpt="social">-<span class="u">%</span></p>
        <div class="track"><span class="c-social" data-rpt-bar="social"></span></div>
        <p class="cap">지난주 대비 <b data-rpt-delta="social"></b></p>
    </div>
</div>

<div class="dash-row">
    <div class="dash-chart kd-no-data">
        <div class="kd-empty">
            <span class="ic kd-empty-ic-seed"></span>
            <p class="t">아직 성장 데이터가 없어요</p>
            <p class="d">첫 이야기를 마치면 감정 이해 · 표현 · 사회성이 주차별로 여기에 쌓여요</p>
            <a class="kd-btn kd-btn-primary" href="/learn">학습 시작하기</a>
        </div>
        <img class="ch" data-kd-char="wave" src="/img/char-tori-wave.png" alt="">
    </div>

    <div class="dash-chart kd-s1">
        <div class="kd-empty">
            <span class="ic kd-empty-ic-seed"></span>
            <p class="t">오늘 기록을 남겼어요</p>
            <p class="d">이야기를 <b>한 편</b> 마치면 감정 이해 · 표현 · 사회성 지수가 계산돼요</p>
        </div>
    </div>

    <section class="dash-card dash-plan">
        <div class="hd">
            <h2>주차별 로드맵</h2>
            <button type="button" class="lk" onclick="dlgRoadmap.showModal()">전체 보기</button>
        </div>
        <ol id="dashPlanList"></ol>
        <p class="foot" id="dashPlanFoot"></p>
    </section>
</div>
<div class="dash-bottom">
    <section class="dash-today kd-hide0">
        <h2>오늘의 추천 학습</h2>
        <p>오늘은 친구 마음을 알아보고, 표정으로 마음을 전해 봐요</p>
        <a class="kd-btn kd-btn-primary" href="/learn">학습 시작하기</a>
    </section>
    <section class="dash-streak">
        <p class="big" id="dashStreak"><b>-</b><span>불러오는 중…</span></p>
        <div class="dash-week" id="dashWeek">
            <div><p class="d">월</p><span class="mk off"></span></div>
            <div><p class="d">화</p><span class="mk off"></span></div>
            <div><p class="d">수</p><span class="mk off"></span></div>
            <div><p class="d">목</p><span class="mk off"></span></div>
            <div><p class="d">금</p><span class="mk off"></span></div>
            <div><p class="d">토</p><span class="mk off"></span></div>
            <div><p class="d">일</p><span class="mk off"></span></div>
        </div>
    </section>
</div>

<dialog id="dlgRoadmap" class="terms-dialog dash-dlg">
    <h2>12주 로드맵</h2>
    <ol id="dlgPlanList"></ol>
    <button type="button" onclick="dlgRoadmap.close()">닫기</button>
</dialog>

<%@ include file="../common/app-bottom.jsp" %>
