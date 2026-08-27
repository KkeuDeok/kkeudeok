<%-- 아동 홈 — 학습으로 들어가는 입구.
     캐릭터 카드를 눌러 한 편을 시작한다. 여기 박힌 문구는 기본값이고,
     실제 이야기는 story-session.js 가 서버에서 받아 갈아끼운다. --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%

    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    String[][] homeCards = {
        {"sad",   "{c}가 슬퍼요",  "무슨 일인지 들어볼까요?"},
        {"angry", "{c}가 화났어요", "무슨 일인지 들어볼까요?"},
        {"happy", "{c}가 신났어요", "무슨 일인지 들어볼까요?"}
    };
    String[] card = homeCards[0];
    for (String[] row : homeCards) {
        if (row[0].equals(emo)) card = row;
    }

    String pageTitle  = "아동 홈";
    String storyStep  = "home";
    String storyCount = "";
%>
<%@ include file="../common/child-top.jsp" %>

<% storyFootOff = true; %>

<h1 class="home-hi">안녕, <span data-kd="childVocative">지우야</span>!</h1>
<p class="home-sub"><%= "{c}가 기다리고 있어요".replace("{c}", CHAR) %></p>

<div class="home-card">
    <img class="art" data-kd-char="<%= emo %>" src="/img/char-tori-<%= emo %>.png" alt="">
    <p class="ttl"><%= card[1].replace("{c}", CHAR) %></p>
    <p class="txt"><%= card[2] %></p>
    <a class="go" href="/story/scene?emo=<%= emo %>">도우러 가기</a>
</div>

<%@ include file="../common/child-bottom.jsp" %>
