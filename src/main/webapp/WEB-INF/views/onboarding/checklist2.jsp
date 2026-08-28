<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 발달 체크 (2/2)"; int onbStep = 3;
   String onbCol = "onb-col--w900 onb-col--center"; String onbNext = "kdSubmitOnbChecklist2()"; %>
<%@ include file="../common/onb-top.jsp" %>

<h1 class="onb-title">감정을 다스리고 어울리는 모습은 어떤가요?</h1>
<p class="onb-sub">마지막 여섯 문항이에요</p>

<form method="post" action="/onboarding/checklist-2">
    <%
        String ckTitle = "감정 조절";
        String ckCount = "checkCount";
        String[] ckNames = {"q7", "q8", "q9"};
        String[] ckTexts = {
            "상황에 맞게 감정을 표현하나요? (예: 조용한 곳에서 목소리 낮추기)",
            "속상한 일이 있어도 오래지 않아 기분을 되찾나요?",
            "감정이 격해졌을 때 스스로 가라앉히려고 하나요?"
        };
    %>
    <%@ include file="../common/onb-check-card.jsp" %>

    <%
        ckTitle = "사회적 상호작용";
        ckCount = "";
        ckNames = new String[]{"q10", "q11", "q12"};
        ckTexts = new String[]{
            "친구가 다가와 말을 걸면 알아차리고 반응하나요?",
            "먼저 다가가 같이 놀자고 하나요?",
            "이야기할 때 눈을 맞추고 번갈아 주고받나요?"
        };
    %>
    <%@ include file="../common/onb-check-card.jsp" %>
</form>

<%@ include file="../common/onb-bottom.jsp" %>
