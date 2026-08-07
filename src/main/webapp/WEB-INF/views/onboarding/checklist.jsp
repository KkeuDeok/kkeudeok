<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 발달 체크 (1/2)"; int onbStep = 3;
   String onbCol = "onb-col--w900 onb-col--center"; String onbNext = "kdSubmitOnbChecklist()"; %>
<%@ include file="../common/onb-top.jsp" %>

<h1 class="onb-title">아이의 현재 모습을 알려주세요</h1>
<p class="onb-sub">일상에서 관찰한 모습을 기준으로 편하게 답해주세요</p>

<%--
  문항 출처 — 공개된 척도의 '구성개념'을 참고해 새로 쓴 문항이다.
  SRS-2 처럼 문항이 저작권으로 보호되는 도구는 문장을 그대로 옮기지 않았다.

  [감정 이해] Denham 의 Affect Knowledge Test(AKT) 4구성요소
    q1 표정 인식      ← receptive/expressive labeling
    q2 상황-감정 추론 ← situational knowledge
    q3 조망 수용·공감 ← affective perspective taking

  [감정 표현] Emotion Expression Scale for Children(EESC, Penza-Clyve & Zeman 2002)
    q4 자기감정 자각  ← 'Poor Awareness' (역방향)
    q5 표현 의지      ← 'Expressive Reluctance' (역방향)
    q6 도움 요청      ← 표현 의지 + 사회적 지원 추구

  [감정 조절] Emotion Regulation Checklist(ERC, Shields & Cicchetti 1997) — 다음 장
    q7 상황에 맞는 표현 · q8 회복 · q9 진정 시도

  ⚠ 진단 도구가 아니다. 학습 커리큘럼 개인화를 위한 보호자 관찰 문항이며,
    원 척도들은 4점(ERC) 또는 5점(EESC)인데 여기서는 Figma 디자인에 맞춰
    7점 양극 척도를 쓴다. 임상 해석에 그대로 쓰지 말 것.
--%>
<form method="post" action="/onboarding/checklist">
    <%
        String ckTitle = "감정 이해";
        String ckCount = "checkCount";
        String[] ckNames = {"q1", "q2", "q3"};
        String[] ckTexts = {
            "다른 사람의 표정을 보고 어떤 기분인지 알아차리나요?",
            "이야기나 상황을 듣고 그 사람의 기분을 짐작하나요?",
            "다른 사람이 울거나 속상해할 때 알아차리고 반응하나요?"
        };
    %>
    <%@ include file="../common/onb-check-card.jsp" %>

    <%
        ckTitle = "감정 표현";
        ckCount = "";
        ckNames = new String[]{"q4", "q5", "q6"};
        ckTexts = new String[]{
            "자기가 지금 어떤 기분인지 스스로 알아차리나요?",
            "기쁘거나 속상할 때 말이나 표정으로 드러내나요?",
            "힘들 때 어른에게 도와 달라고 말하나요?"
        };
    %>
    <%@ include file="../common/onb-check-card.jsp" %>
</form>

<%@ include file="../common/onb-bottom.jsp" %>
