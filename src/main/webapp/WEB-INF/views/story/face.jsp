<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습4 표정 따라하기 — Figma 웹_학습4_표정따라하기(383:1367 / 383:1544 화남 / 383:1650 기쁨).
       카메라를 켜고 토리와 같은 표정을 지어 보는 단계.

       ⚠ Figma 3벌이 전부 "화난 표정으로…" 로 같다(복제 잔재라 노드 이름에도 [TODO문구] 표시가 있다).
         노드 이름이 `슬픈 표정으로…` 인 걸로 보아 감정별로 갈리는 게 원래 의도라 판단해
         슬픔·기쁨 문구를 채웠다 — 팀장 확정 필요.
       ⚠ 실제 카메라는 아직 안 붙였다. 온보딩 표정 등록과 같은 권한 게이트가 필요하다. */
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
    <%-- TODO: 카메라 미리보기(getUserMedia). 지금은 얼굴 맞출 자리만 보여 준다 --%>
    <div class="guide" aria-hidden="true"></div>
    <div class="cam-hint">
        <%-- 따라 할 표정을 그대로 보여 준다 — Figma 는 무표정 고정이었지만 감정별로 바꿨다 --%>
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
