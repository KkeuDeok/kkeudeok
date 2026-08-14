<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습5 동작 따라하기 — Figma 웹_학습5_동작따라하기(411:85 / 411:186 화남 / 411:287 기쁨).
       표정 다음 단계. 카메라 박스는 학습4 와 같은 규격이고 안에 시범 그림이 하나 더 있다.

       ⚠ 화남 벌은 Figma 가 "잠시 기다려줄까?" 였는데 **기다리는 포즈 그림이 없다**.
         마침 학습1 화남 시나리오가 임시(Figma 에 화남 벌 없음)라, '내가 실수로 무너뜨림' 으로
         바꾸고 여기서 **미안하다고 말하기**로 잇는다. ASD 사회성 학습에서 사과 표현은 핵심 스킬이라
         커리큘럼상으로도 이득이다 — 다만 문구는 팀장 확정 필요. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    /* 키 · 제목 · 시범 포즈 · 코너 카드 문구 */
    String[][] actSteps = {
        {"sad",   "{c}가 슬픈가봐. {c}를 위로해주자",                    "comfort",   "같이 토닥여봐"},
        {"angry", "{c}가 속상한가봐. 미안하다고 말해줄까?",              "sorry",     "이렇게 해봐"},
        {"happy", "{c}가 기분이 좋대! 우리도 같이 신나게 축하해 줄까?", "celebrate", "같이 축하해봐"},
        {"surprise", "{c}가 깜짝 놀랐나봐. 괜찮다고 토닥여 줄까?",       "comfort",   "괜찮다고 토닥여봐"}
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
    <%-- TODO: 카메라 미리보기(getUserMedia). 지금은 시범 그림과 맞출 자리만.
         Figma 는 시범 그림이 슬픔 벌에만 있었지만, 감정별 동작 그림이 생겨 3벌 모두 넣는다.
         ⚠ 큰 그림은 **도와줄 친구의 표정**(emo)이다 — 제목이 "토리가 슬픈가봐"인데 웃는 그림이
           있으면 안 맞는다(2026-08-10 사용자 지적). 따라 할 **동작**은 오른쪽 코너 카드가 보여 준다. --%>
    <img class="demo" data-kd-char="<%= emo %>" src="/img/char-tori-<%= emo %>.png" alt="">
    <div class="guide" aria-hidden="true"></div>
    <div class="cam-hint">
        <img data-kd-char="<%= act[2] %>" src="/img/char-tori-<%= act[2] %>.png" alt="">
        <span><%= act[3] %></span>
    </div>
</div>

<div class="cam-actions">
    <button type="button" class="kd-sub kd-sub-listen">다시 들려줘</button>
    <a class="kd-cta" href="/story/result?emo=<%= emo %>">다 했어요!</a>
    <button type="button" class="kd-sub kd-sub-hint">잘 모르겠어</button>
</div>

<%@ include file="../common/child-bottom.jsp" %>
