<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "회원가입 - 완료"; %>
<%@ include file="../common/auth-top.jsp" %>

<%@ include file="../common/signup-header.jsp" %>

<div class="done-icon">
    <img src="/img/icon-check.svg" alt="">
</div>
<h1 class="done-title">가입이 완료되었어요!</h1>
<p class="done-sub">이제 아이 정보를 등록할 차례예요</p>

<div class="done-card">
    <p class="card-caption">다음 단계</p>
    <div class="done-step">
        <span class="num">1</span>
        <div>
            <p class="t">아이 정보 등록</p>
            <p class="d">이름과 나이를 알려주세요</p>
        </div>
    </div>
    <div class="done-step">
        <span class="num">2</span>
        <div>
            <p class="t">캐릭터 고르기</p>
            <p class="d">함께할 친구를 골라요</p>
        </div>
    </div>
    <div class="done-step">
        <span class="num">3</span>
        <div>
            <p class="t">첫 이야기 시작</p>
            <p class="d">감정을 배우는 모험을 떠나요</p>
        </div>
    </div>
</div>

<%-- 가입 다음 단계는 보호자 PIN 설정이다(로그인 성공 시에도 같은 곳으로 간다) --%>
<a href="/onboarding/pin" class="kd-btn kd-btn-primary auth-cta">아이 정보 등록하기</a>

<%@ include file="../common/auth-bottom.jsp" %>
