<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습3b 이유 찾기 · 듣는 중 — Figma 웹_학습3b_이유찾기_듣는중(383:1232).
       학습3 과 같은 화면인데 [말로 알려줄래!] 자리가 듣는 중 표시로 바뀐다.
       파형은 정지 그림이다 — Figma 도 애니메이션이 아니다. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    String whyPick = null;

    String pageTitle  = "이유 찾기";
    String storyStep  = "why";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>
<%@ include file="_why-body.jspf" %>

<div class="why-listen">
    <span class="wave" aria-hidden="true"><i></i><i></i><i></i><i></i><i></i><i></i><i></i></span>
    <span class="txt">듣고 있어요</span>
</div>

<%@ include file="../common/child-bottom.jsp" %>
