<%-- 학습1 상황 이야기 — 무슨 일이 있었는지 들려주는 첫 화면.
     여기 들어온 순간이 한 편의 시작이라 story-session.js 가 세션을 연다. --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%

    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    String[][] storyScenes = {
        {"sad",   "{c}가 처음 간 곳에서 길을 잃을 뻔했어요", "낯선 곳이라 무서웠어요. 그래서 울고 있어요.",   "{c}를 도와줄래?"},
        {"angry", "{c}가 쌓은 블록을 내가 실수로 무너뜨렸어요", "열심히 만든 게 무너져서 화가 났어요.",       "{c}를 도와줄래?"},
        {"happy", "{c}가 친구에게 깜짝 선물을 받았어요",     "너무 좋아서 깡충깡충 뛰었어요. 기분이 좋대요!", "{c}랑 같이 기뻐할래?"},
        {"surprise", "{c}가 갑자기 큰 소리에 깜짝 놀랐어요", "쿵 소리가 나서 눈이 동그래졌어요. 가슴이 콩콩 뛰어요.", "{c}를 안심시켜 줄래?"}
    };
    String[] sc = storyScenes[0];
    for (String[] row : storyScenes) {
        if (row[0].equals(emo)) sc = row;
    }

    String pageTitle  = "상황 이야기";
    String storyStep  = "scene";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>

<div class="story-art"><img data-kd-char="<%= emo %>" src="/img/char-tori-<%= emo %>.png" alt=""></div>

<h1 class="story-title"><%= sc[1].replace("{c}", CHAR) %></h1>
<p class="story-sub"><%= sc[2] %></p>
<p class="story-ask"><%= sc[3].replace("{c}", CHAR) %></p>

<a class="story-cta" href="/story/feel?emo=<%= emo %>"><%= "{c}에게 가기".replace("{c}", CHAR) %></a>

<%@ include file="../common/child-bottom.jsp" %>
