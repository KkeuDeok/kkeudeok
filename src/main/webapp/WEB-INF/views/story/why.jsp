<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습3 이유 찾기 — Figma 웹_학습3_이유찾기(136:282 / 383:892 화남 / 383:1001 기쁨).
       말로 답하거나 그림을 눌러 고른다.

       화면을 새로 띄우지 않는다(사용자 요구) —
         [말로 알려줄래!] → 그 자리에서 버튼이 파형으로 바뀐다(story.js). 예전의 why-listening 화면은 없앴다.
         오답 카드      → 그 자리에서 오답 모달이 뜬다. 아이가 '새로고침만 됐다'고 느끼면 안 된다.
       ?wrong=other 로 직접 들어와도 모달이 열린 채 그려진다(JS 꺼짐 대비). */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    boolean whyWrong = "other".equals(request.getParameter("wrong"));
    String whyPick = whyWrong ? "other" : null;

    /* 오답은 3벌 모두 '졸려서' 하나뿐이라, 단서는 정답 쪽 상황을 되짚어 준다 */
    String[][] whyClues = {
        {"sad",   "졸린 게 아니야. 내가 넘어졌을 때를 떠올려 볼래?"},
        {"angry", "졸린 게 아니야. 내 블록이 어떻게 됐는지 떠올려 볼래?"},
        {"happy", "졸린 게 아니야. 내가 무엇을 받았는지 떠올려 볼래?"}
    };
    String whyClue = whyClues[0][1];
    for (String[] r : whyClues) { if (r[0].equals(emo)) whyClue = r[1]; }

    String pageTitle  = "이유 찾기";
    String storyStep  = "why";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>
<%@ include file="_why-body.jspf" %>

<%-- 같은 자리에서 버튼 ↔ 파형이 갈린다. story.js 가 hidden 을 토글한다 --%>
<button type="button" class="why-mic">말로 알려줄래!</button>
<div class="why-listen" hidden>
    <span class="wave" aria-hidden="true"><i></i><i></i><i></i><i></i><i></i><i></i><i></i></span>
    <span class="txt">듣고 있어요</span>
</div>

<%-- 오답 모달 — 마크업은 항상 두고 JS 가 연다. ?wrong= 로 들어오면 처음부터 열려 있다 --%>
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
