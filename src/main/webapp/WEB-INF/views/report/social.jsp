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
  <div class="rpt-radar-row">
    <div class="rpt-radar">
        <%-- viewBox 를 그림이 실제로 차지하는 범위(x 40~660, y 10~410)로 좁혀
             같은 표시 크기에서 오각형이 커지게 했다. 좌표 계산은 그대로 쓴다. --%>
        <svg width="620" height="400" viewBox="40 10 620 400" role="img"
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
        <div class="rpt-radar-legend">
            <span><i class="ln"></i>이번 주</span>
            <span><i class="ln ln-prev"></i>4주 전</span>
        </div>
    </div>

    <%-- 오각형만으로는 "성실성 41" 같은 정확한 값을 읽기 어렵다.
         증감은 레이더의 4주 전 데이터 [62,55,38,48,60] 과의 차이를 그대로 쓴 것이다. --%>
    <div class="rpt-axis">
        <h3>축별 점수</h3>
        <div class="rpt-hbar"><span class="lb">외향성</span><div class="track"><span style="width:74%;background:#34a36a"></span></div><span class="pct">74</span><b class="dt rpt-up">▲12</b></div>
        <div class="rpt-hbar"><span class="lb">우호성/친화성</span><div class="track"><span style="width:62%;background:#34a36a"></span></div><span class="pct">62</span><b class="dt rpt-up">▲7</b></div>
        <div class="rpt-hbar"><span class="lb">개방성</span><div class="track"><span style="width:68%;background:#34a36a"></span></div><span class="pct">68</span><b class="dt rpt-up">▲8</b></div>
        <div class="rpt-hbar"><span class="lb">신경증/정서 안정성</span><div class="track"><span style="width:55%;background:#34a36a"></span></div><span class="pct">55</span><b class="dt rpt-up">▲7</b></div>
        <div class="rpt-hbar"><span class="lb">성실성</span><div class="track"><span style="width:41%;background:#34a36a"></span></div><span class="pct">41</span><b class="dt rpt-up">▲3</b></div>
        <p class="cap">4주 전과 견준 변화예요</p>

        <%-- Figma 원본(24:16480)에서 1050 밖으로 잘려 있던 섹션.
             아래에 따로 두면 오각형을 키운 만큼 화면이 넘쳐서 우측 열로 올렸다. --%>
        <h3 class="mt">이번 주 사회성 장면</h3>
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
    </div>
  </div>
</section>

<div class="rpt-cta">
    <span class="ic"></span>
    <p>새 친구 앞에서는 아직 긴장해요 · '친구와 다툰 날' 이야기로 함께 연습해 보세요</p>
    <a class="kd-btn kd-btn-primary" href="/learn">학습 홈 가기</a>
</div>

<%@ include file="../common/app-bottom.jsp" %>
