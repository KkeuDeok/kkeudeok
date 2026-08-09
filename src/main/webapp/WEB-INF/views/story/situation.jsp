<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 상황 선택 — Figma 웹_상황선택(82:148 / 210:175 화남 / 210:257 기쁨).

       ⚠ 이 3벌만 파랑·빨강·노랑 톤이라 학습 흐름 나머지(초록)와 색이 다르다.
         스텝바는 `행동`인데 내용은 "도와줄 상황을 선택해 보아요"라 세션 시작 화면처럼 읽힌다.
         팀장 확인 대기 항목이라 Figma 원본을 그대로 옮겼다.
       ⚠ 화남 벌은 Figma 원 배경이 #d9d9d9(미수정 잔재)다. 슬픔·기쁨은 원 = 버튼색이라
         같은 규칙으로 #f5222d 를 넣었다.
       ⚠ 실제로는 상황이 여러 개 뜨고 그중 하나를 고르는 화면일 텐데 Figma 는 벌마다
         카드가 한 장뿐이다 — 기획 확정 필요. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    /* 키 · 주색 · 연한배경 · 테두리 · 태그 · 제목 · 설명 · 버튼 */
    String[][] pickCards = {
        {"sad",   "#1890ff", "#e6f7ff", "#91d5ff", "우울한 상황", "{c}가 시무룩해요",
         "{c}가 우울해하며 시무룩하게 앉아 있어요. 슬픈 {c}에게 따뜻한 토닥토닥 위로를 건네볼까요?", "토닥토닥 해주기"},
        {"angry", "#f5222d", "#fff1f0", "#ffa39e", "화난 상황", "{c}가 화가 났어요!",
         "{c}가 화가 나서 씩씩거리고 있어요. {c}에게 무슨 일이 생긴 걸까요? 마음을 달래주러 가볼까요?", "{c} 위로해주기"},
        {"happy", "#faad14", "#fffbe6", "#ffe58f", "기쁜 상황", "{c}가 신이 났어요!",
         "{c}가 함박웃음을 지으며 꼬리를 살랑이고 있어요! 행복한 소식을 나누고 함께 축하해주러 가볼까요?", "함께 축하해주기"}
    };
    String[] pick = pickCards[0];
    for (String[] row : pickCards) {
        if (row[0].equals(emo)) pick = row;
    }

    String pageTitle  = "상황 선택";
    String storyStep  = "act";
    String storyCount = "";   /* 진도는 아래 배지가 직접 보여 준다 */
%>
<%@ include file="../common/child-top.jsp" %>
<%-- 하단 바가 없는 화면이다 --%>
<% storyFootOff = true; %>

<p class="pick-badge">오늘 이야기 1 / 3</p>
<h1 class="pick-title"><%= "{c}의 마음을 알아볼까?".replace("{c}", CHAR) %></h1>
<p class="pick-sub"><%= "지금 {c}는 어떤 기분일까요? 함께 들어보고 도와줄 상황을 선택해 보아요.".replace("{c}", CHAR) %></p>

<div class="pick-card" style="--pick-key:<%= pick[1] %>;--pick-tint:<%= pick[2] %>;--pick-line:<%= pick[3] %>">
    <div class="art"><img data-kd-char="<%= pick[0] %>" src="/img/char-tori-<%= pick[0] %>.png" alt=""></div>
    <p class="tag"><%= pick[4] %></p>
    <p class="ttl"><%= pick[5].replace("{c}", CHAR) %></p>
    <p class="txt"><%= pick[6].replace("{c}", CHAR) %></p>
    <a class="go" href="/story/act?emo=<%= emo %>"><%= pick[7].replace("{c}", CHAR) %></a>
</div>

<%@ include file="../common/child-bottom.jsp" %>
