<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 아동홈 — Figma 웹_아동홈(24:18425). 아이가 학습을 시작하는 입구.
       보호자가 '오늘의 일상 입력'에서 고른 감정이 ?emo= 로 넘어오고, 여기서 학습1로 들어간다.
       학습 단계가 아니라서 스텝바·닫기·하단 바가 전부 없다.

       ⚠ "지우"는 아이 이름 자리다(세션 결과 화면과 같다) — 백엔드 연결 시 교체.
       ⚠ Figma 는 슬픔 한 벌뿐이라 화남·기쁨 카드 문구는 임시 — 기획 확정 필요.
       ⚠ 캐릭터 그림도 Figma 가 무표정 한 장을 쓴다. 감정별 그림은 자산이 없다. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    String[][] homeCards = {
        {"sad",   "토리가 슬퍼요",  "무슨 일인지 들어볼까요?"},
        {"angry", "토리가 화났어요", "무슨 일인지 들어볼까요?"},
        {"happy", "토리가 신났어요", "무슨 일인지 들어볼까요?"}
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
<%-- 하단 바가 없는 화면이다 --%>
<% storyFootOff = true; %>

<h1 class="home-hi">안녕, 지우야!</h1>
<p class="home-sub">토리가 기다리고 있어요</p>

<div class="home-card">
    <img class="art" src="/img/story-home-tori.png?v=142" alt="">
    <p class="ttl"><%= card[1] %></p>
    <p class="txt"><%= card[2] %></p>
    <a class="go" href="/story/scene?emo=<%= emo %>">토리 도우러 가기</a>
</div>

<%@ include file="../common/child-bottom.jsp" %>
