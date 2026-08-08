<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "social"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 수치는 전부 예시값. 고정값 표: 사회성 58 ▼3 · 8주 누적 +8 --%>
<div class="app-head rpt-head">
    <h1>지우의 성장 리포트</h1>
    <p>관찰 8주차 · 매주 월요일에 갱신돼요</p>
</div>

<div class="rpt-tabsrow">
    <%@ include file="../common/report-tabs.jsp" %>
</div>

<div class="rpt-score">
    <div>
        <p class="lb">사회성 지수</p>
        <p class="v">58<span class="u">%</span><b class="delta rpt-down">▼3 · 지난주 대비</b></p>
    </div>
</div>

<section class="rpt-sec">
    <%--
      오각 레이더 — 중심 (220,200), R=160, 꼭짓점 i 의 각 = -90° + i·72°
      점 = (220 + R·(v/100)·cos, 200 + R·(v/100)·sin)
      축 순서: 외향성(위) → 우호성/친화성 → 성실성 → 신경증/정서 안정성 → 개방성
    --%>
    <div class="rpt-radar">
        <%-- 1024 한 화면에 담기게 viewBox 는 그대로 두고 표시 크기만 0.73 배로 줄였다 --%>
        <svg width="509" height="320" viewBox="0 0 700 440" role="img"
             aria-label="사회성 5축 — 외향성 74, 우호성 62, 성실성 41, 정서 안정성 55, 개방성 68">
            <g transform="translate(130,20)">
                <%-- 격자 링 4개 (1 / 0.75 / 0.5 / 0.25 배) --%>
                <g fill="none" stroke="#e6ebe8" stroke-width="1" stroke-dasharray="4 4">
                    <polygon points="220,40 372.2,150.6 314.0,329.4 126.0,329.4 67.8,150.6"/>
                    <polygon points="220,80 334.1,163.0 290.5,297.1 149.5,297.1 105.9,163.0"/>
                    <polygon points="220,120 296.1,175.3 267.0,264.7 173.0,264.7 143.9,175.3"/>
                    <polygon points="220,160 258.1,187.7 243.5,232.4 196.5,232.4 181.9,187.7"/>
                    <%-- 축선 --%>
                    <line x1="220" y1="200" x2="220" y2="40"/>
                    <line x1="220" y1="200" x2="372.2" y2="150.6"/>
                    <line x1="220" y1="200" x2="314.0" y2="329.4"/>
                    <line x1="220" y1="200" x2="126.0" y2="329.4"/>
                    <line x1="220" y1="200" x2="67.8" y2="150.6"/>
                </g>
                <%-- 4주 전 [62,55,38,48,60] --%>
                <polygon points="220,100.8 303.7,172.8 255.7,249.2 174.9,262.1 128.7,170.3"
                         fill="none" stroke="#8b978f" stroke-width="2" stroke-dasharray="5 5"/>
                <%-- 이번 주 [74,62,41,55,68] --%>
                <polygon points="220,81.6 314.3,169.3 258.6,253.1 168.3,271.2 116.5,166.4"
                         fill="rgba(52,163,106,0.18)" stroke="#34a36a" stroke-width="2.5"
                         stroke-linejoin="round"/>
                <%-- 축 라벨 + 값 --%>
                <g font-family="Noto Sans KR, sans-serif" text-anchor="middle">
                    <text x="220" y="18" font-size="16" font-weight="700" fill="#2b3a33">외향성</text>
                    <text x="220" y="-6" font-size="14" fill="#6b7a72" transform="translate(0,42)">74</text>
                    <text x="452" y="146" font-size="16" font-weight="700" fill="#2b3a33">우호성/친화성</text>
                    <text x="452" y="170" font-size="14" fill="#6b7a72">62</text>
                    <text x="360" y="362" font-size="16" font-weight="700" fill="#2b3a33">성실성</text>
                    <text x="360" y="386" font-size="14" fill="#6b7a72">41</text>
                    <text x="80" y="362" font-size="16" font-weight="700" fill="#2b3a33">신경증/정서 안정성</text>
                    <text x="80" y="386" font-size="14" fill="#6b7a72">55</text>
                    <text x="-14" y="146" font-size="16" font-weight="700" fill="#2b3a33">개방성</text>
                    <text x="-14" y="170" font-size="14" fill="#6b7a72">68</text>
                </g>
            </g>
        </svg>
    </div>
    <div class="rpt-radar-legend">
        <span><i class="ln"></i>이번 주</span>
        <span><i class="ln ln-prev"></i>4주 전</span>
    </div>
</section>

<hr class="rpt-rule">

<%-- Figma 원본(24:16480)에서 1050 밖으로 잘려 있던 섹션 — 웹은 스크롤이 있어 복원 --%>
<section class="rpt-sec">
    <h2>이번 주 사회성 장면</h2>
    <div class="rpt-scenes">
        <div class="rpt-scene">
            <p class="t">놀이터에서 차례를 기다렸어요</p>
            <span class="chip">차례 지키기 성공</span>
            <span class="d">7.19</span>
        </div>
        <div class="rpt-scene">
            <p class="t">블록놀이에서 민수와 화해했어요</p>
            <span class="chip">마음을 말로 전달</span>
            <span class="d">7.22</span>
        </div>
    </div>
</section>

<div class="rpt-cta">
    <span class="ic"></span>
    <p>새 친구 앞에서는 아직 긴장해요 · '친구와 다툰 날' 이야기로 함께 연습해 보세요</p>
    <a class="kd-btn kd-btn-primary" href="/learn">학습 홈 가기</a>
</div>

<%@ include file="../common/app-bottom.jsp" %>
