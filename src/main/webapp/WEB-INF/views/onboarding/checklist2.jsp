<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 발달 체크 2/2 — 스텝바는 그대로 3단계(발달 체크)다. 카드 3장이 한 화면(1024)에
     안 들어가서 나눈 것이므로 단계를 늘리지 않는다. --%>
<% String pageTitle = "온보딩 - 발달 체크 (2/2)"; int onbStep = 3;
   String onbCol = "onb-col--w900 onb-col--center"; String onbNext = "kdSubmitOnbChecklist2()"; %>
<%@ include file="../common/onb-top.jsp" %>

<h1 class="onb-title">감정을 다스리고 어울리는 모습은 어떤가요?</h1>
<p class="onb-sub">마지막 여섯 문항이에요</p>

<%-- [감정 조절] Emotion Regulation Checklist(ERC, Shields & Cicchetti 1997)
       q7 상황에 맞는 표현 ← 'Emotion Regulation' 하위척도(적절한 정서 표출)
       q8 회복             ← 'Lability/Negativity' 하위척도(기분 회복, 역방향)
       q9 진정 시도        ← 'Emotion Regulation' 하위척도(자기 진정)

     [사회적 상호작용] SRS-2(Social Responsiveness Scale) 의 사회적 인식·인지·동기 개념
       ⚠ SRS-2 문항은 저작권으로 보호돼 문장을 옮기지 않았다. 개념만 참고해 새로 썼다.
       q10 사회적 인식   ← Social Awareness
       q11 상호작용 시작 ← Social Motivation
       q12 주고받기      ← Social Communication --%>
<%-- action 은 checklist.jsp 에서 복사할 때 그대로 남아 있던 값이었다.
     지금은 POST 라우트가 없어 잠복 상태지만, 백엔드가 붙는 순간 2페이지가 1페이지로 제출된다. --%>
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
