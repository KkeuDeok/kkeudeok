<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 완료"; int onbStep = 6;
   String onbCol = "onb-col--w800 onb-col--center onb-col--done";
   String onbNext = "kdOnbSubmit(function(){"
                  + "localStorage.setItem('kdDone','0');"
                  + "sessionStorage.removeItem('kdStage');"
                  + "sessionStorage.removeItem('kdDaily');"
                  + "sessionStorage.removeItem('kdCounted');"
                  + "location.href='/dashboard';"
                  + "})"; %>
<%@ include file="../common/onb-top.jsp" %>
<% onbPrev = false; onbNextLabel = "대시보드로 가기"; %>

<div class="onb-confetti" aria-hidden="true">
    <%
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

<img class="onb-done-char" id="doneChar" src="/img/char-tori-neutral.png" alt="">
<h1 class="onb-title">준비가 끝났어요!</h1>
<p class="onb-sub" id="doneSub">지우에게 맞는 학습을 준비했어요</p>

<table class="onb-summary">
    <thead>
        <tr><th>No</th><th>항목</th><th>내용</th></tr>
    </thead>
    <tbody>
        <tr><td>1</td><td>아이 이름</td><td id="doneName">지우 (6세)</td></tr>
        <tr><td>2</td><td>함께할 친구</td><td id="doneFriend">토리</td></tr>
    </tbody>
</table>

<%@ include file="../common/onb-bottom.jsp" %>
