<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- onbStep = 7 → 스텝바 여섯 칸이 모두 완료 표시된다 --%>
<%-- ⚠ JSP 주석은 스크립틀릿 안에 넣을 수 없다 — 넣으면 컴파일이 깨진다.
        주석 닫는 기호를 주석 본문에 글자로 적어도 안 된다. 거기서 주석이 끝나 버려
        뒷문장이 화면에 그대로 찍힌다 (실제로 이 주석이 그렇게 새고 있었다) --%>
<% String pageTitle = "온보딩 - 완료"; int onbStep = 7;
   String onbCol = "onb-col--w800 onb-col--center onb-col--done";
   /* 온보딩을 실제로 통과했으면 데이터가 0인 게 맞다 — 대시보드를 신규 상태로 띄운다.
      해제는 주소에 ?stage=2 (head.jsp 스위치). 위 경고대로 여기는 자바 주석이어야 한다. */
   String onbNext = "sessionStorage.setItem('kdStage','0');location.href='/dashboard'"; %>
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

<img class="onb-done-char" id="doneChar" src="/img/char-tori-icon.png?v=119" alt="">
<h1 class="onb-title">준비가 끝났어요!</h1>
<p class="onb-sub" id="doneSub">지우에게 맞는 학습을 준비했어요</p>

<%-- 여기 적힌 값은 Figma 예시다. 앞 단계 입력이 보관돼 있으면
     auth-validate.js 의 renderOnbDone() 이 덮어쓴다.
     백엔드가 붙으면 그 함수 대신 세션/DB 값으로 채우면 된다 --%>
<table class="onb-summary">
    <thead>
        <tr><th>No</th><th>항목</th><th>내용</th></tr>
    </thead>
    <tbody>
        <tr><td>1</td><td>아이 이름</td><td id="doneName">지우 (6세)</td></tr>
        <tr><td>2</td><td>함께할 친구</td><td id="doneFriend">곰 토리</td></tr>
    </tbody>
</table>

<%@ include file="../common/onb-bottom.jsp" %>
