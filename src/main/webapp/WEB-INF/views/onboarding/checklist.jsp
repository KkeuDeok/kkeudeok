<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 발달 체크 (1/2)"; int onbStep = 3;
   String onbCol = "onb-col--w900 onb-col--center"; String onbNext = "kdSubmitOnbChecklist()"; %>
<%@ include file="../common/onb-top.jsp" %>

<h1 class="onb-title">아이의 현재 모습을 알려주세요</h1>
<p class="onb-sub">일상에서 관찰한 모습을 기준으로 편하게 답해주세요</p>

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
