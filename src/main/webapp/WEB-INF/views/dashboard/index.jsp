<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "메인 대시보드"; String appNav = "dashboard"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 숫자·그래프·목록은 전부 Figma 예시값이다.
     주간 집계는 백엔드에서 내려줄 값이라 화면 골격만 만들어 둔다. --%>
<div class="app-head">
    <h1><span data-kd="childCall">지우</span>의 이번 주</h1>
</div>

<div class="dash-kpis kd-has-data">
    <div class="dash-kpi">
        <p class="lb">감정 표현</p>
        <p class="v">64%</p>
        <div class="track"><span class="c-express" style="width:64%"></span></div>
        <p class="cap">지난주 대비 <b class="dl-up">+5%p</b></p>
    </div>
    <div class="dash-kpi">
        <p class="lb">감정 이해</p>
        <p class="v">72%</p>
        <div class="track"><span class="c-understand" style="width:72%"></span></div>
        <p class="cap">지난주 대비 <b class="dl-up">+2%p</b></p>
    </div>
    <div class="dash-kpi">
        <p class="lb">사회성</p>
        <p class="v">58%</p>
        <div class="track"><span class="c-social" style="width:58%"></span></div>
        <p class="cap">지난주 대비 <b class="dl-down">-3%p</b></p>
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

    <div class="dash-chart kd-has-data">
        <h2>주간 감정 변화 추이</h2>
        <%--
          세로축은 0~100 이 아니라 45~75 구간만 보여 준다 (Figma 격자선 = 75/65/55/45).
          값 v 의 y 좌표 = 40.5 + (72 - v) * 6.857
          가로는 월~일 7칸, x = 22 + i*90
        --%>
        <svg width="660" height="260" viewBox="0 0 660 260" role="img"
             aria-label="주간 감정 변화 추이 — 감정 이해 72, 감정 표현 64, 사회성 58">
            <g stroke="#eef2ef" stroke-width="1">
                <line x1="22" y1="19.97" x2="562" y2="19.97"/>
                <line x1="22" y1="88.54" x2="562" y2="88.54"/>
                <line x1="22" y1="157.11" x2="562" y2="157.11"/>
                <line x1="22" y1="225.68" x2="562" y2="225.68"/>
            </g>
            <g font-size="12" fill="#8b978f" text-anchor="middle" font-family="Noto Sans KR, sans-serif">
                <text x="22" y="248">월</text><text x="112" y="248">화</text><text x="202" y="248">수</text>
                <text x="292" y="248">목</text><text x="382" y="248">금</text><text x="472" y="248">토</text>
                <text x="562" y="248">일</text>
            </g>
            <g fill="none" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline stroke="#e8a317" points="22,136.5 112,122.8 202,102.2 292,68.2 382,54.5 472,47.6 562,40.5"/>
                <polyline stroke="#3d8fe0" points="22,191.4 112,177.7 202,157.1 292,136.5 382,116.0 472,102.2 562,95.4"/>
                <polyline stroke="#0fa394" points="22,164.0 112,157.1 202,150.3 292,150.3 382,143.4 472,143.4 562,136.5"/>
            </g>
            <g>
                <circle cx="562" cy="40.5" r="4.7" fill="#e8a317"/>
                <circle cx="562" cy="95.4" r="4.7" fill="#3d8fe0"/>
                <circle cx="562" cy="136.5" r="4.7" fill="#0fa394"/>
            </g>
            <g font-size="12" font-family="Noto Sans KR, sans-serif">
                <text x="573" y="44" fill="#e8a317">이해 72</text>
                <text x="573" y="99" fill="#3d8fe0">표현 64</text>
                <text x="573" y="140" fill="#0fa394">사회성 58</text>
            </g>
        </svg>
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
        <p>이야기 1편 + 표정 미션 1회</p>
        <a class="kd-btn kd-btn-primary" href="/learn">학습 시작하기</a>
    </section>

    <section class="dash-streak">
        <p class="big kd-has-data"><b>5일</b><span>연속 이용 중</span></p>
        <p class="big kd-no-data"><b>0일</b><span>오늘 시작해 볼까요?</span></p>
        <p class="big kd-s1"><b>1일</b><span>연속 이용 중</span></p>
        <div class="dash-week kd-has-data">
            <div><p class="d">월</p><span class="mk on"></span></div>
            <div><p class="d">화</p><span class="mk on"></span></div>
            <div><p class="d">수</p><span class="mk off"></span></div>
            <div><p class="d">목</p><span class="mk on"></span></div>
            <div><p class="d">금</p><span class="mk on"></span></div>
            <div><p class="d">토</p><span class="mk off"></span></div>
            <div><p class="d">일</p><span class="mk on"></span></div>
        </div>
        <div class="dash-week kd-s1">
            <div><p class="d">월</p><span class="mk on"></span></div>
            <div><p class="d">화</p><span class="mk off"></span></div>
            <div><p class="d">수</p><span class="mk off"></span></div>
            <div><p class="d">목</p><span class="mk off"></span></div>
            <div><p class="d">금</p><span class="mk off"></span></div>
            <div><p class="d">토</p><span class="mk off"></span></div>
            <div><p class="d">일</p><span class="mk off"></span></div>
        </div>
        <div class="dash-week kd-no-data">
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

<%-- 하단 173px 이 비어 있던 자리. Figma A안(284:75)에 있다가 B안으로 넘어오며
     빠진 `오늘의 관찰 확인` 가로 배너를 되살린 것이다. --%>
<div class="dash-observe kd-hide0">
    <span class="ic"></span>
    <p>오늘 <span data-kd="childCall">지우</span>의 모습을 기록하면 다음 학습이 더 정확해져요</p>
    <a class="kd-btn kd-btn-primary" href="/learn">관찰 기록하기</a>
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
