<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "메인 대시보드"; String appNav = "dashboard"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 숫자·그래프·목록은 전부 Figma 예시값이다.
     주간 집계는 백엔드에서 내려줄 값이라 화면 골격만 만들어 둔다. --%>
<div class="app-head">
    <h1><span data-kd="childCall">지우</span>의 이번 주</h1>
</div>

<%-- 세 지표는 성장 리포트와 **같은 계산**을 쓴다(/api/report) — 화면마다 따로 세면
     대시보드 64% 와 리포트 64% 가 어긋나는 날이 온다. kd-report.js 가 채운다. --%>
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
    <%-- 데이터 0일 때 KPI·추이 그래프 자리를 대신하는 카드. .dash-row 가 flex 라
         옆 로드맵 카드와 높이가 저절로 맞는다(고정 높이 지정 금지). --%>
    <div class="dash-chart kd-no-data">
        <div class="kd-empty">
            <span class="ic kd-empty-ic-seed"></span>
            <p class="t">아직 성장 데이터가 없어요</p>
            <p class="d">첫 이야기를 마치면 감정 이해 · 표현 · 사회성이 주차별로 여기에 쌓여요</p>
            <a class="kd-btn kd-btn-primary" href="/learn">학습 시작하기</a>
        </div>
        <%-- 카드가 휑하다는 지적(2026-08-10) — dash-today 와 같은 방식으로 캐릭터를 얹는다 --%>
        <img class="ch" data-kd-char="wave" src="/img/char-tori-wave.png" alt="">
    </div>

    <%-- 1단계 — 일상 기록만 남긴 상태(이야기는 아직). 다음 행동을 딱 하나만 가리킨다.
         2026-08-10: 이야기를 한 편이라도 마치면 곧바로 2단계로 가므로 여기는 '학습 전' 이다 --%>
    <div class="dash-chart kd-s1">
        <div class="kd-empty">
            <span class="ic kd-empty-ic-seed"></span>
            <p class="t">오늘 기록을 남겼어요</p>
            <p class="d">이야기를 <b>한 편</b> 마치면 감정 이해 · 표현 · 사회성 지수가 계산돼요</p>
            <%-- 버튼을 두지 않는다 — 바로 아래 '오늘의 추천 학습' 에 같은 [학습 시작하기] 가 있어 겹친다.
                 CTA 는 화면당 하나로 (0단계에서 하단 카드를 통째로 숨긴 것도 같은 이유). --%>
        </div>
    </div>


    <section class="dash-card dash-plan">
        <div class="hd">
            <h2>주차별 로드맵</h2>
            <button type="button" class="lk" onclick="dlgRoadmap.showModal()">전체 보기</button>
        </div>
        <%-- 목록·하단 문구는 kd-roadmap.js 가 채운다. 커리큘럼은 그 파일 한 곳에만 있다.
             2026-08-10: 로드맵을 고르고 고치던 화면 두 개(온보딩 선택 · 마이페이지 탭)를
             삭제해서 이제 커리큘럼은 고정 1벌이다 — 보호자가 바꿀 경로가 없다. --%>
        <ol id="dashPlanList"></ol>
        <p class="foot" id="dashPlanFoot"></p>
    </section>
</div>

<%-- 2026-08-09 에는 이 행을 0단계에서 통째로 숨겼다(갈 곳을 [학습 시작하기] 하나로 좁히려고).
     그랬더니 화면이 너무 휑했다(2026-08-10 지적) → **겹치는 CTA 가 있는 '오늘의 추천 학습' 만**
     숨기고 '연속 이용' 은 0단계에도 보인다. 그쪽은 0단계 문구를 원래 갖고 있었다. --%>
<div class="dash-bottom">
    <section class="dash-today kd-hide0">
        <h2>오늘의 추천 학습</h2>
        <%-- 2026-08-13: '이야기 1편 + 표정 미션 1회' 는 화면 구성을 그대로 읽어 준 말이라
             보호자에게 무엇이 좋은지가 안 보였다. 아이가 무엇을 해 보는지로 바꿨다 --%>
        <p>오늘은 친구 마음을 알아보고, 표정으로 마음을 전해 봐요</p>
        <a class="kd-btn kd-btn-primary" href="/learn">학습 시작하기</a>
    </section>

    <%-- 연속 이용일수와 요일 점은 **실제 학습 기록**으로 채운다(kd-summary.js).
         예전에는 5일/1일/0일 세 벌을 데모 스위치(kd-has-data 등)로 갈아 끼웠는데,
         누가 써도 같은 숫자가 나와 남의 화면처럼 보였다(2026-08-15 지적). --%>
    <section class="dash-streak">
        <p class="big" id="dashStreak"><b>-</b><span>불러오는 중…</span></p>
        <%-- 요일은 고정, 점만 kd-summary.js 가 이번 주 기록으로 켠다 --%>
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

<%-- 로드맵 전체 보기 — 네이티브 <dialog>. 라우트를 새로 파면 서버 재시작이 필요해서 모달로 만들었다.
     .terms-dialog 를 같이 붙여야 backdrop 과 zoom 상쇄를 물려받는다(mypage 모달과 같은 규칙).
     목록은 kd-roadmap.js 가 채운다 — 커리큘럼은 그 파일 한 곳에만 있다. --%>
<dialog id="dlgRoadmap" class="terms-dialog dash-dlg">
    <h2>12주 로드맵</h2>
    <ol id="dlgPlanList"></ol>
    <button type="button" onclick="dlgRoadmap.close()">닫기</button>
</dialog>

<%@ include file="../common/app-bottom.jsp" %>
