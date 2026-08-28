<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 시작"; %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title><%= pageTitle %> | 끄덕</title>
    <%@ include file="../common/head.jsp" %>
</head>
<body class="auth-body">
<div class="app-frame">
    <%@ include file="../common/auth-left.jsp" %>
    <main class="auth-panel onb-start-panel">
        <div class="onb-start-col">
            <h1 class="onb-title">아이와 함께할 준비를 해요</h1>
            <p class="onb-sub">아이에게 맞는 학습을 만들기 위해 세 가지만 확인할게요</p>

            <div class="onb-card">
                <span class="ic">
                    <img src="/img/onb-card-child.png?v=1" alt="">
                </span>
                <span class="tx">
                    <p class="t">아이 정보 · 캐릭터 등록</p>
                    <p class="d">이름과 함께할 친구를 골라요</p>
                </span>
                <span class="min">약 1분</span>
            </div>
            <div class="onb-card">
                <span class="ic">
                    <img src="/img/onb-card-check.png?v=1" alt="">
                </span>
                <span class="tx">
                    <p class="t">발달 체크리스트</p>
                    <p class="d">9개 문항으로 아이를 이해해요</p>
                </span>
                <span class="min">약 2분</span>
            </div>
            <div class="onb-card">
                <span class="ic">
                    <img src="/img/onb-card-face.png?v=1" alt="">
                </span>
                <span class="tx">
                    <p class="t">표정 등록</p>
                    <p class="d">카메라로 5가지 표정을 담아요</p>
                </span>
                <span class="min">약 2분</span>
            </div>

            <a class="kd-btn kd-btn-primary onb-start-cta" href="/onboarding/profile">시작하기</a>
        </div>
    </main>
</div>
</body>
</html>
