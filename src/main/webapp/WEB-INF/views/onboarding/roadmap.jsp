<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - AI 로드맵"; int onbStep = 6;
   String onbCol = "onb-col--w900 onb-col--center"; String onbNext = "location.href='/onboarding/done'"; %>
<%@ include file="../common/onb-top.jsp" %>
<% onbNextLabel = "시작"; %>

<h1 class="onb-title">AI가 추천하는 학습 로드맵이에요</h1>
<p class="onb-sub">체크리스트 결과를 바탕으로 우리 아이에게 맞는 커리큘럼을 추천했어요</p>

<%-- 두 카드는 라디오라서 어느 쪽이든 고를 수 있다(기본값 = AI 추천).
     ponytail: 커리큘럼 내용은 Figma 확정본 그대로고, 체크리스트 점수로
     단계를 재배치하는 실제 추천 로직은 백엔드 몫 --%>
<form class="onb-plans" method="post" action="/onboarding/roadmap">
    <label class="onb-plan">
        <input type="radio" name="plan" value="standard">
        <span class="badge">표준</span>
        <h2>정석 커리큘럼</h2>
        <p class="lead">일반적인 순서로 진행하는 표준 과정</p>
        <div class="rule"></div>
        <div class="onb-step-item"><span class="no">1</span><div class="hd"><p class="t">감정 인식 기초</p><span class="wk">1-2주</span></div><p class="d">기본 6가지 감정 알기</p></div>
        <div class="onb-step-item"><span class="no">2</span><div class="hd"><p class="t">감정 표현 연습</p><span class="wk">3-4주</span></div><p class="d">표정/몸짓 따라하기</p></div>
        <div class="onb-step-item"><span class="no">3</span><div class="hd"><p class="t">상황별 감정 이해</p><span class="wk">5-6주</span></div><p class="d">상황과 감정 연결하기</p></div>
        <div class="onb-step-item"><span class="no">4</span><div class="hd"><p class="t">공감 반응 연습</p><span class="wk">7-8주</span></div><p class="d">위로/격려 표현하기</p></div>
        <div class="onb-step-item"><span class="no">5</span><div class="hd"><p class="t">사회적 행동 적용</p><span class="wk">9-10주</span></div><p class="d">실생활 사회 기술</p></div>
        <div class="onb-step-item"><span class="no">6</span><div class="hd"><p class="t">종합 복습</p><span class="wk">11-12주</span></div><p class="d">전체 내용 통합</p></div>
    </label>

    <label class="onb-plan onb-plan--ai">
        <input type="radio" name="plan" value="ai" checked>
        <span class="badge">AI 추천</span>
        <h2>AI 맞춤 커리큘럼</h2>
        <p class="lead">체크리스트 결과 기반 개인화 과정</p>
        <div class="rule"></div>
        <div class="onb-step-item"><span class="no">1</span><div class="hd"><p class="t">감정 표현 집중</p><span class="wk">1-3주</span></div><p class="d">표현이 약한 부분 우선 강화</p></div>
        <div class="onb-step-item"><span class="no">2</span><div class="hd"><p class="t">감정 이해 심화</p><span class="wk">4-5주</span></div><p class="d">복합 감정 이해하기</p></div>
        <div class="onb-step-item"><span class="no">3</span><div class="hd"><p class="t">사회적 상호작용</p><span class="wk">6-7주</span></div><p class="d">친구와 소통하기</p></div>
        <div class="onb-step-item"><span class="no">4</span><div class="hd"><p class="t">공감 실전 연습</p><span class="wk">8-9주</span></div><p class="d">실제 상황 역할극</p></div>
        <div class="onb-step-item"><span class="no">5</span><div class="hd"><p class="t">일상 적용</p><span class="wk">10-11주</span></div><p class="d">가정/학교 연계 활동</p></div>
        <div class="onb-step-item"><span class="no">6</span><div class="hd"><p class="t">자기 평가</p><span class="wk">12주</span></div><p class="d">스스로 돌아보기</p></div>
    </label>
</form>

<%@ include file="../common/onb-bottom.jsp" %>
