<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%

    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    String wrong = request.getParameter("wrong");
    if (wrong != null && (wrong.equals(emo) || !wrong.matches("happy|sad|angry|surprise"))) wrong = null;

    String feelPick = wrong;   

    String pageTitle  = "마음 읽기";
    String storyStep  = "feel";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>
<%@ include file="_feel-body.jspf" %>

<%

       String shown = (wrong != null) ? wrong : "happy";

       String[][] wrongTraits = {
           {"happy",    "기쁠 때는 입꼬리가 올라가."},
           {"sad",      "슬플 때는 눈썹이 아래로 처져."},
           {"angry",    "화날 때는 눈썹이 뾰족해져."},
           {"surprise", "놀랄 때는 눈이 동그래져."}
       };
       String[][] answerClues = {
           {"sad",      "지금 나는 눈물이 나고 있어… 어떤 마음일까?"},
           {"angry",    "지금 내 얼굴은 뜨거워졌어… 어떤 마음일까?"},
           {"happy",    "지금 나는 깡충깡충 뛰고 있어… 어떤 마음일까?"},
           {"surprise", "지금 내 눈은 동그래졌어… 어떤 마음일까?"}
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

            <a class="x" data-close href="/story/feel?emo=<%= emo %>" aria-label="닫기"></a>
        </div>
        <div class="bd">

            <img data-kd-char="sorry" src="/img/char-tori-sorry.png" alt="">
            <p class="say">"음... 내 마음은 조금 다른 것 같아"</p>
            <p class="clue"><span><%= trait %></span><span><%= clue %></span></p>
            <p class="more">가까워지고 있어! 조금만 더 살펴보자.</p>
        </div>
        <div class="ft">
            <a data-close href="/story/feel?emo=<%= emo %>">다시 볼래!</a>
        </div>
    </div>
</div>

<%@ include file="../common/child-bottom.jsp" %>
