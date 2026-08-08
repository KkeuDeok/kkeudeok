<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "메인 대시보드"; String appNav = "dashboard"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 숫자·그래프·목록은 전부 Figma 예시값이다.
     주간 집계는 백엔드에서 내려줄 값이라 화면 골격만 만들어 둔다. --%>
<div class="app-head">
    <h1>지우의 이번 주</h1>
    <p>로드맵: 맞춤 (1순위 · 감정 표현)</p>
</div>

<div class="dash-kpis">
    <div class="dash-kpi">
        <p class="lb">감정 표현</p>
        <p class="v">64%</p>
        <div class="track"><span class="c-express" style="width:64%"></span></div>
        <p class="cap">지난주 대비</p>
    </div>
    <div class="dash-kpi">
        <p class="lb">감정 이해</p>
        <p class="v">72%</p>
        <div class="track"><span class="c-understand" style="width:72%"></span></div>
        <p class="cap">지난주 대비</p>
    </div>
    <div class="dash-kpi">
        <p class="lb">사회성</p>
        <p class="v">58%</p>
        <div class="track"><span class="c-social" style="width:58%"></span></div>
        <p class="cap">지난주 대비</p>
    </div>
</div>

<div class="dash-row">
    <div class="dash-chart">
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
            <a href="/report">전체 보기</a>
        </div>
        <ol>
            <li><span class="no no-done">5</span><span class="t">표정으로 표현하기</span><span class="chip chip-done">완료</span></li>
            <li><span class="no no-done">6</span><span class="t">몸짓으로 표현하기</span><span class="chip chip-done">완료</span></li>
            <li><span class="no no-now">7</span><span class="t">감정 표현하기 — 슬픔 연습</span><span class="chip chip-now">진행중</span></li>
            <li><span class="no no-soon">8</span><span class="t">친구 위로하기</span><span class="chip chip-soon">예정</span></li>
        </ol>
        <p class="foot">12주 과정 중 7주차 · 이번 주 2 / 3 완료</p>
    </section>
</div>

<div class="dash-bottom">
    <section class="dash-today">
        <h2>오늘의 추천 학습</h2>
        <p>이야기 1편 + 표정 미션 1회</p>
        <a class="kd-btn kd-btn-primary" href="/learn">학습 시작하기</a>
    </section>

    <section class="dash-streak">
        <p class="big"><b>5일</b><span>연속 이용 중</span></p>
        <div class="dash-week">
            <div><p class="d">월</p><span class="mk on"></span></div>
            <div><p class="d">화</p><span class="mk on"></span></div>
            <div><p class="d">수</p><span class="mk off"></span></div>
            <div><p class="d">목</p><span class="mk on"></span></div>
            <div><p class="d">금</p><span class="mk on"></span></div>
            <div><p class="d">토</p><span class="mk off"></span></div>
            <div><p class="d">일</p><span class="mk on"></span></div>
        </div>
    </section>
</div>

<%@ include file="../common/app-bottom.jsp" %>
