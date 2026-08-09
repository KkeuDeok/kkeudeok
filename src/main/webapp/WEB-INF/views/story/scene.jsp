<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습1 상황 이야기 — 감정 3벌.
       보호자가 '오늘의 일상 입력'에서 고른 감정이 ?emo= 로 넘어온다(기쁨→happy · 화남→angry · 그 외→sad).
       화면을 3개 만들지 않고 레일 하나에 문구만 갈아끼운다 — Figma 도 그림은 3벌이 같은 걸 쓴다.

       ⚠ 문구 출처
         sad   = Figma 웹_학습1_상황이야기(368:200) 원문
         happy = Figma 웹_학습1_상황이야기_기쁨(237:402) 원문. 단 '토리랑 같이 기뻐할래?' 한 줄은
                 원본에 질문 줄이 아예 없어서 레일을 맞추려고 넣은 임시 문구다.
         angry = Figma 에 화남 벌이 없다. 전부 임시 — 기획 확정 필요. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    String[][] storyScenes = {
        {"sad",   "토리가 처음 간 곳에서 길을 잃을 뻔했어요", "낯선 곳이라 무서웠어요. 그래서 울고 있어요.",   "토리를 도와줄래?"},
        {"angry", "토리가 쌓아 올린 블록이 무너졌어요",       "열심히 만들었는데 무너져서 화가 났어요.",       "토리를 도와줄래?"},
        {"happy", "토리가 친구에게 깜짝 선물을 받았어요",     "너무 좋아서 깡충깡충 뛰었어요. 기분이 좋대요!", "토리랑 같이 기뻐할래?"}
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

<%-- 감정별 캐릭터 그림은 아직 없다 — Figma 도 세 벌 다 '캐릭터 - 무표정' 한 장을 돌려쓴다.
     자산이 생기면 char-tori-{감정}.png 식으로 갈아끼우면 된다. --%>
<div class="story-art"><img src="/img/char-tori-full.png?v=114" alt=""></div>

<h1 class="story-title"><%= sc[1] %></h1>
<p class="story-sub"><%= sc[2] %></p>
<p class="story-ask"><%= sc[3] %></p>

<%-- TODO: 다음 화면(학습2 마음읽기)을 만들면 /story/feel?emo= 로 연결 --%>
<a class="story-cta" href="#">토리에게 가기</a>

<%@ include file="../common/child-bottom.jsp" %>
