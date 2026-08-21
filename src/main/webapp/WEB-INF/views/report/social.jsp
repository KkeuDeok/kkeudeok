<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "social"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 수치는 전부 예시값. 고정값 표: 사회성 58 ▼3 · 8주 누적 +8 --%>
<div class="app-head rpt-head">
    <h1><span data-kd="childCall">지우</span>의 성장 리포트</h1>
</div>

<div class="rpt-tabsrow">
    <%@ include file="../common/report-tabs.jsp" %>
</div>
<%@ include file="../common/report-empty.jsp" %>


<div class="rpt-score">
    <div>
        <p class="lb">사회성 지수</p>
        <p class="v" data-rpt="social">-<span class="u">%</span><b class="delta"></b></p>
    </div>
</div>
<%-- 사회성만 척도가 다르다 — 성공률이 아니라 <b>환산 지수</b>다.
     아래 레이더 값과 딱 맞아떨어지지 않는 건 체크리스트를 뒤집어 지수화한 뒤
     실제 미션 수행과 합치기 때문이다. 근거 줄(rpt-basis)에 그 셈을 그대로 적는다.

     ⚠ 진짜 SRS-2 T점수가 아니다. SRS-2 는 65문항 라이선스 도구이고, T점수는 규준표가
       있어야 나오는데 그 표는 우리에게 없다. 여기서는 채점 <b>방향</b>(점수가 높을수록
       어려움 → 뒤집어 지수화)만 따른다. 화면에 'SRS-2 점수' 라고 적지 말 것 —
       교수님이 "T점수가 몇이냐" 고 물으면 답할 수 없다(2026-08-20). ReportService 참고 --%>
<p class="rpt-basis" data-rpt-basis="social"></p>

<section class="rpt-sec">
    <h2>발달 영역별 지수</h2>
    <%--
      ⚠ 제목을 반드시 둔다. 이 그림은 '사회성의 네 축' 이 아니라 <b>발달 네 영역</b>이다.
        제목 없이 사회성 탭에 놓였더니 감정 이해·표현·조절이 사회성의 하위 항목처럼 읽혀
        "왜 사회성 그림에 감정이 있나" 로 보였다(2026-08-20 지적).
        네 영역은 각각 다른 도구에서 왔다 — 이해 AKT · 표현 EESC · 조절 ERC · 상호작용 SRS-2.

      레이더 — 사회성 지수에 <b>실제로 쓰는</b> 체크리스트 영역만 축으로 쓴다.
      중심 (220,200), R=160. 축 순서: 위에서 시작해 시계방향
        감정 이해(위) · 감정 표현(오른쪽 아래) · 사회적 상호작용(왼쪽 아래)

      ⚠ 축 개수는 서버(ReportService.RADAR_DOMAINS)가 정하고 kd-report.js 가 그 수대로
        그린다. 오각형(Big5) → 사각형 → 삼각형으로 두 번 바뀐 자리다 — 여기에 개수를
        전제한 값을 박아 두면 다음에 또 어긋난다.
      ⚠ 예전에는 Big5(외향성·우호성·성실성·신경증·개방성)를 그렸다. 재지도 않는 값이
        박혀 있어 점수와 그림이 딴 이야기를 했다(2026-08-16 지적). 재는 것만 그린다.
      ⚠ 감정 조절을 뺀 이유는 ReportService.RADAR_DOMAINS 주석에 있다(2026-08-20).
      ⚠ 격자·다각형·라벨은 전부 kd-report.js 가 그린다 — 값이 아이마다 다르므로
        여기 좌표를 박아 두면 또 같은 일이 반복된다.
    --%>
    <div class="rpt-radar">
        <%-- ⚠ viewBox 는 축 개수가 바뀔 때마다 다시 잡아야 한다. 오각형 → 사각형 때도
             옛 값을 그대로 둬 라벨이 잘렸다(2026-08-18 지적).
             지금은 삼각형(감정 이해·감정 표현·사회적 상호작용, 2026-08-20).
               꼭짓점  위 (220, 40) · 오른쪽 (358.6, 280) · 왼쪽 (81.4, 280)
               라벨은 거기서 34 바깥, 값은 다시 22 아래 → 가로 −4~428, 세로 6~323
             넉넉히 −20~460 / −20~340 으로 잡는다.
             ⚠ 요소 크기(848×560)와 viewBox 비율이 달라도 된다 — 기본 preserveAspectRatio 가
               가운데 맞춰 축소하므로 잘리지 않는다. 억지로 비율을 맞추려다 라벨을 자르지 말 것. --%>
        <svg width="848" height="560" viewBox="-20 -20 480 360" role="img"
             id="rptRadar" aria-label="체크리스트 영역별 지수">
            <g id="rptRadarBody"></g>
        </svg>
        <div class="rpt-radar-legend">
            <span><i class="ln"></i>온보딩 체크리스트 기준</span>
        </div>
    </div>
</section>

<%@ include file="../common/app-bottom.jsp" %>
