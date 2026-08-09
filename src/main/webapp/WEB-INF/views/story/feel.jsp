<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습2 마음 읽기 — Figma 웹_학습2_마음읽기(24:18040).
       학습1 에서 넘어온 ?emo= 가 곧 정답이다(기쁨→happy · 화남→angry · 그 외→sad).
       오답 카드를 누르면 ?wrong= 이 붙어 돌아오고 그 위에 오답 모달(24:17777)을 띄운다.
       화면이 아니라 상태라 라우트를 새로 파지 않았다. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    String wrong = request.getParameter("wrong");
    if (wrong != null && (wrong.equals(emo) || !wrong.matches("happy|sad|angry"))) wrong = null;

    String feelPick = wrong;   /* 아이가 방금 고른 카드를 짚어 준다 */

    String pageTitle  = "마음 읽기";
    String storyStep  = "feel";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>
<%@ include file="_feel-body.jspf" %>

<%
       /* 모달 마크업은 늘 그린다 — 오답을 누르면 story.js 가 그 자리에서 연다.
          ?wrong= 로 직접 들어온 경우에만 처음부터 열려 있다(JS 꺼짐 대비). */
       String shown = (wrong != null) ? wrong : "happy";
       /* 모달 문구 — Figma 원문은 '화남을 골랐는데 정답이 슬픔'인 한 벌뿐이다.
          앞은 고른 감정의 특징, 뒤는 정답 감정의 단서로 갈라 6조합을 만든다.
          angry + sad 조합이 Figma 원문과 정확히 같다. */
       String[][] wrongTraits = {
           {"happy", "기쁠 때는 입꼬리가 올라가."},
           {"sad",   "슬플 때는 눈썹이 아래로 처져."},
           {"angry", "화날 때는 눈썹이 뾰족해져."}
       };
       String[][] answerClues = {
           {"sad",   "지금 나는 눈물이 나고 있어… 어떤 마음일까?"},
           {"angry", "지금 내 얼굴은 뜨거워졌어… 어떤 마음일까?"},
           {"happy", "지금 나는 깡충깡충 뛰고 있어… 어떤 마음일까?"}
       };
       String trait = "", clue = "";
       for (String[] r : wrongTraits) { if (r[0].equals(shown)) trait = r[1]; }
       for (String[] r : answerClues) { if (r[0].equals(emo))   clue  = r[1]; }
%>
<div class="story-dim<%= wrong != null ? " is-on" : "" %>" role="dialog" aria-modal="true"
     aria-labelledby="wrongTitle"<%= wrong != null ? "" : " hidden" %>>
    <div class="story-modal">
        <div class="hd">
            <h2 id="wrongTitle"><%= "{c}의 귓속말".replace("{c}", CHAR) %></h2>
            <%-- 닫아도 다시 고를 수 있게 모달 없는 같은 화면으로 돌아간다 --%>
            <a class="x" data-close href="/story/feel?emo=<%= emo %>" aria-label="닫기"></a>
        </div>
        <div class="bd">
            <%-- 오답 모달은 3벌 공통으로 미안해하는 표정을 쓴다 --%>
            <img data-kd-char="sorry" src="/img/char-tori-sorry.png" alt="">
            <p class="say">"음... 내 마음은 조금 다른 것 같아"</p>
            <p class="clue"><%= trait %> <%= clue %></p>
            <p class="more">가까워지고 있어! 조금만 더 살펴보자.</p>
        </div>
        <div class="ft">
            <a data-close href="/story/feel?emo=<%= emo %>">다시 볼래!</a>
        </div>
    </div>
</div>

<%@ include file="../common/child-bottom.jsp" %>
