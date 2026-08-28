<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    String[][] actSteps = {
        {"sad",   "{c}가 슬픈가봐. {c}를 토닥여 주자",                  "comfort",   "토닥토닥 해 볼까?"},
        {"angry", "{c}가 속상한가봐. 미안하다고 말해줄까?",              "sorry",     "미안하다고 해 볼까?"},
        {"happy", "{c}가 기분이 좋대! 우리도 같이 신나게 축하해 줄까?", "celebrate", "같이 축하해 볼까?"},
        {"surprise", "{c}가 깜짝 놀랐나봐. 괜찮다고 토닥여 줄까?",       "comfort",   "토닥토닥 해 볼까?"}
    };
    String[] act = actSteps[0];
    for (String[] row : actSteps) {
        if (row[0].equals(emo)) act = row;
    }

    String pageTitle  = "동작 따라하기";
    String storyStep  = "act";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>

<h1 class="cam-title"><%= act[1].replace("{c}", CHAR) %></h1>

<div class="cam-box">
    <img class="demo" data-kd-char="<%= emo %>" src="/img/char-tori-<%= emo %>.png" alt="">

    <div class="cam-hint cam-hint-text">
        <p class="gt"><%= act[3] %></p>
        <ol class="gs">
            <li>손을 들어요</li>
            <li>친구 쪽으로</li>
            <li>토닥토닥 흔들어요</li>
        </ol>
    </div>
</div>

<div class="cam-actions">
    <button type="button" class="kd-sub kd-sub-listen">다시 들려줘</button>
    <a class="kd-cta" href="/story/result?emo=<%= emo %>">다 했어요!</a>
    <button type="button" class="kd-sub kd-sub-hint">잘 모르겠어</button>
</div>

<%@ include file="../common/child-bottom.jsp" %>
