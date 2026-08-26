<%-- 상황 선택 안내 — 표정과 동작 사이에 끼는 화면. 학습 단계는 아니다.
     이번 이야기의 감정에 맞춰 카드 한 장을 보여 주고 동작 화면으로 넘긴다. --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%

    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    String[][] pickCards = {
        {"sad",   "#1890ff", "#e6f7ff", "#91d5ff", "우울한 상황", "{c}가 시무룩해요",
         "{c}가 우울해하며 시무룩하게 앉아 있어요.", "comfort"},
        {"angry", "#f5222d", "#fff1f0", "#ffa39e", "화난 상황", "{c}가 화가 났어요!",
         "{c}가 화가 나서 씩씩거리고 있어요.", "sorry"},
        {"happy", "#faad14", "#fffbe6", "#ffe58f", "기쁜 상황", "{c}가 신이 났어요!",
         "{c}가 함박웃음을 지으며 꼬리를 살랑이고 있어요!", "celebrate"},

        {"surprise", "#722ed1", "#f9f0ff", "#d3adf7", "놀란 상황", "{c}가 깜짝 놀랐어요!",
         "{c}가 갑자기 난 큰 소리에 눈이 동그래졌어요.", "comfort"}
    };
    String[] pick = pickCards[0];
    for (String[] row : pickCards) {
        if (row[0].equals(emo)) pick = row;
    }

    String[][] pickActs = {
        {"comfort",   "따뜻하게 토닥여 주러 가볼까요?",     "토닥토닥 해주기"},
        {"sorry",     "미안한 마음을 전하러 가볼까요?",     "미안하다고 말하기"},
        {"celebrate", "함께 축하해주러 가볼까요?",          "함께 축하해주기"},
        {"wave",      "먼저 다가가 인사해 볼까요?",         "손 흔들어 인사하기"}
    };
    String[] act = pickActs[0];
    for (String[] row : pickActs) {
        if (row[0].equals(pick[7])) act = row;
    }

    String pageTitle  = "상황 선택";
    String storyStep  = "act";
    String storyCount = "";   
%>
<%@ include file="../common/child-top.jsp" %>

<% storyFootOff = true; %>

<p class="pick-badge">오늘 이야기 1 / 3</p>
<h1 class="pick-title"><%= "{c}의 마음을 알아볼까?".replace("{c}", CHAR) %></h1>
<p class="pick-sub"><%= "지금 {c}는 어떤 기분일까요? 함께 들어보고 도와줄 상황을 선택해 보아요.".replace("{c}", CHAR) %></p>

<div class="pick-card" style="--pick-key:<%= pick[1] %>;--pick-tint:<%= pick[2] %>;--pick-line:<%= pick[3] %>">
    <div class="art"><img data-kd-char="<%= pick[0] %>" src="/img/char-tori-<%= pick[0] %>.png" alt=""></div>
    <p class="tag"><%= pick[4] %></p>
    <p class="ttl"><%= pick[5].replace("{c}", CHAR) %></p>
    <p class="txt"><span class="st"><%= pick[6].replace("{c}", CHAR) %></span> <span class="ac"><%= act[1] %></span></p>
    <a class="go" href="/story/act?emo=<%= emo %>"><%= act[2] %></a>
</div>

<%@ include file="../common/child-bottom.jsp" %>
