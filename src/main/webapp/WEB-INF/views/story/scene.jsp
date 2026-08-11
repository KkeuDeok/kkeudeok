<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습1 상황 이야기 — 감정 3벌.
       보호자가 '오늘의 일상 입력'에서 고른 감정이 ?emo= 로 넘어온다(기쁨→happy · 화남→angry · 그 외→sad).
       화면을 3개 만들지 않고 레일 하나에 문구만 갈아끼운다 — Figma 도 그림은 3벌이 같은 걸 쓴다.

       ⚠ 문구 출처
         sad   = Figma 웹_학습1_상황이야기(368:200) 원문
         happy = Figma 웹_학습1_상황이야기_기쁨(237:402) 원문. 단 '같이 기뻐할래?' 한 줄은
                 원본에 질문 줄이 아예 없어서 레일을 맞추려고 넣은 임시 문구다.
         angry = Figma 에 화남 벌이 없다. 전부 임시 — 기획 확정 필요.
                 학습5 에서 '미안하다고 말하기'로 이어지도록 **내 실수로 무너뜨린 상황**으로 잡았다. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    String[][] storyScenes = {
        {"sad",   "{c}가 처음 간 곳에서 길을 잃을 뻔했어요", "낯선 곳이라 무서웠어요. 그래서 울고 있어요.",   "{c}를 도와줄래?"},
        {"angry", "{c}가 쌓은 블록을 내가 실수로 무너뜨렸어요", "열심히 만든 게 무너져서 화가 났어요.",       "{c}를 도와줄래?"},
        {"happy", "{c}가 친구에게 깜짝 선물을 받았어요",     "너무 좋아서 깡충깡충 뛰었어요. 기분이 좋대요!", "{c}랑 같이 기뻐할래?"}
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
