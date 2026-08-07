<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- onbStep = 7 → 스텝바 여섯 칸이 모두 완료 표시된다 --%>
<%-- 대시보드는 아직 만들 단계가 아니라 흐름을 여기서 끝낸다.
     대시보드 화면이 확정되면 onbNext 를 그쪽으로 돌리면 된다.
     ⚠ JSP 주석(<%-- --%>)은 스크립틀릿(<% %>) 안에 넣을 수 없다 — 넣으면 컴파일이 깨진다 --%>
<% String pageTitle = "온보딩 - 완료"; int onbStep = 7;
   String onbCol = "onb-col--w800 onb-col--center onb-col--done"; String onbNext = ""; %>
<%@ include file="../common/onb-top.jsp" %>
<% onbPrev = false; onbNextLabel = "대시보드로 가기"; %>

<%-- 축하 점 — Figma 완료 화면의 흩뿌려진 색점을 좌표 그대로 옮긴 것 --%>
<div class="onb-confetti" aria-hidden="true">
    <%
        /* {x, y, 지름, 색인덱스} — 색은 아래 palette */
        int[][] dots = {
            {248, 236, 9, 0}, {455, 214, 7, 1}, {545, 190, 8, 2}, {690, 168, 6, 3},
            {318, 300, 7, 2}, {760, 296, 9, 4}, {214, 372, 8, 1}, {830, 260, 7, 0},
            {296, 452, 6, 3}, {880, 420, 8, 2}, {186, 470, 9, 4}, {940, 352, 6, 1},
            {352, 560, 7, 0}, {820, 540, 6, 3}, {248, 620, 8, 2}, {900, 610, 7, 4},
            {430, 660, 6, 1}, {700, 690, 8, 0}, {560, 720, 7, 3}, {330, 730, 6, 4}
        };
        String[] palette = {"#9fc9f3", "#f4b8c8", "#ffd98e", "#b9e4c9", "#c9bdf0"};
        for (int[] d : dots) {
    %>
    <span style="left:<%= d[0] %>px; top:<%= d[1] %>px; width:<%= d[2] %>px; height:<%= d[2] %>px; background:<%= palette[d[3]] %>"></span>
    <% } %>
</div>

<img class="onb-done-char" src="/img/char-haru.png?v=2" alt="">
<h1 class="onb-title">준비가 끝났어요!</h1>
<p class="onb-sub">지우에게 맞는 학습을 준비했어요</p>

<%-- ponytail: 값은 앞 단계 입력을 그대로 보여줘야 한다. 지금은 예시 —
     백엔드 연동 시 세션/DB 값으로 채울 것 --%>
<table class="onb-summary">
    <thead>
        <tr><th>No</th><th>항목</th><th>내용</th></tr>
    </thead>
    <tbody>
        <tr><td>1</td><td>아이 이름</td><td>지우 (6세)</td></tr>
        <tr><td>2</td><td>함께할 친구</td><td>곰 토리</td></tr>
    </tbody>
</table>

<%@ include file="../common/onb-bottom.jsp" %>
