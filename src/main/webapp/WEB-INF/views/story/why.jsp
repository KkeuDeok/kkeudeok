<%-- 학습3 이유 찾기 — 왜 그런 마음이 됐는지 고르거나 말로 답한다.
     본문은 _why-body.jspf 에 있고, 음성 인식은 kd-mediapipe.js 가 맡는다. --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%

    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    boolean whyWrong = "other".equals(request.getParameter("wrong"));
    String whyPick = whyWrong ? "other" : null;

    String whyClue = "아까 무슨 일이 있었는지 다시 떠올려 볼래?";

    String pageTitle  = "이유 찾기";
    String storyStep  = "why";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>
<%@ include file="_why-body.jspf" %>

<button type="button" class="why-mic">말로 알려줄래!</button>
<div class="why-listen" hidden>
    <span class="wave" aria-hidden="true"><i></i><i></i><i></i><i></i><i></i><i></i><i></i></span>
    <span class="txt">듣고 있어요</span>
</div>

<div class="story-dim<%= whyWrong ? " is-on" : "" %>" role="dialog" aria-modal="true"
     aria-labelledby="whyWrongTitle"<%= whyWrong ? "" : " hidden" %>>
    <div class="story-modal">
        <div class="hd">
            <h2 id="whyWrongTitle"><%= "{c}의 귓속말".replace("{c}", CHAR) %></h2>
            <a class="x" data-close href="/story/why?emo=<%= emo %>" aria-label="닫기"></a>
        </div>
        <div class="bd">
            <img data-kd-char="sorry" src="/img/char-tori-sorry.png" alt="">
            <p class="say">"음... 그건 아닌 것 같아"</p>
            <p class="clue"><%= whyClue %></p>
            <p class="more">가까워지고 있어! 조금만 더 살펴보자.</p>
        </div>
        <div class="ft">
            <a data-close href="/story/why?emo=<%= emo %>">다시 볼래!</a>
        </div>
    </div>
</div>

<%@ include file="../common/child-bottom.jsp" %>
