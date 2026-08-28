<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    String[][] faceTitles = {
        {"sad",   "슬픈 표정으로 {c} 마음을 느껴봐요"},
        {"angry", "화난 표정으로 {c} 마음을 느껴봐요"},
        {"happy", "기쁜 표정으로 {c} 마음을 느껴봐요"},
        {"surprise", "놀란 표정으로 {c} 마음을 느껴봐요"}
    };
    String faceTitle = faceTitles[0][1];
    for (String[] row : faceTitles) {
        if (row[0].equals(emo)) faceTitle = row[1];
    }

    String pageTitle  = "표정 따라하기";
    String storyStep  = "face";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>

<h1 class="cam-title"><%= faceTitle.replace("{c}", CHAR) %></h1>

<div class="cam-box">
    <div class="cam-hint">
        <img data-kd-char="<%= emo %>" src="/img/char-tori-<%= emo %>.png" alt="">
        <span>같이 해봐</span>
    </div>
</div>

<div class="cam-actions">
    <button type="button" class="kd-sub kd-sub-listen">다시 들려줘</button>
    <a class="kd-cta" href="/story/situation?emo=<%= emo %>">다 했어요!</a>
    <button type="button" class="kd-sub kd-sub-hint">잘 모르겠어</button>
</div>

<%@ include file="../common/child-bottom.jsp" %>
