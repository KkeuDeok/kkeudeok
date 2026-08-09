<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습5 동작 따라하기 — Figma 웹_학습5_동작따라하기(411:85 / 411:186 화남 / 411:287 기쁨).
       표정 다음 단계. 카메라 박스는 학습4 와 같은 규격이고 안에 시범 그림이 하나 더 있다.
       제목 3벌은 Figma 원문 그대로다(여기는 감정별로 제대로 갈려 있다). */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    String[][] actTitles = {
        {"sad",   "토리가 슬픈가봐. 토리를 위로해주자"},
        {"angry", "토리가 기분이 안 좋은가봐. 우리 잠시 기다려줄까?"},
        {"happy", "토리가 기분이 좋대! 우리도 같이 신나게 축하해 줄까?"}
    };
    String actTitle = actTitles[0][1];
    for (String[] row : actTitles) {
        if (row[0].equals(emo)) actTitle = row[1];
    }

    String pageTitle  = "동작 따라하기";
    String storyStep  = "act";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>

<h1 class="cam-title"><%= actTitle %></h1>

<div class="cam-box">
    <%-- TODO: 카메라 미리보기(getUserMedia). 지금은 시범 그림과 맞출 자리만.
         시범 그림은 Figma 기본(슬픔) 벌에만 있다 — 화남·기쁨 벌엔 아예 없어서 그대로 뺐다.
         감정별 그림이 생기면 story-act-demo-{감정}.png 로 넣고 이 조건을 없애면 된다. --%>
    <% if ("sad".equals(emo)) { %>
    <img class="demo" src="/img/story-act-demo.png?v=142" alt="">
    <% } %>
    <div class="guide" aria-hidden="true"></div>
    <div class="cam-hint">
        <img src="/img/story-tori-wave.png?v=142" alt="">
        <span>같이 흔들어봐</span>
    </div>
</div>

<div class="cam-actions">
    <button type="button" class="kd-sub kd-sub-listen">다시 들려줘</button>
    <a class="kd-cta" href="/story/result?emo=<%= emo %>">다 했어요!</a>
    <button type="button" class="kd-sub kd-sub-hint">잘 모르겠어</button>
</div>

<%@ include file="../common/child-bottom.jsp" %>
