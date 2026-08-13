<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- onbStep = 0 → 브랜드·스텝바 없는 껍데기. PIN 설정은 온보딩 스텝 흐름 밖의 계정 설정 단계다 --%>
<% String pageTitle = "보호자 PIN 설정"; int onbStep = 0; String onbCol = ""; String onbNext = "kdSubmitOnbPin()"; %>
<%@ include file="../common/onb-top.jsp" %>
<%-- 뒤로 갈 곳이 로그인뿐이라 [이전]을 두지 않는다 --%>
<% onbPrev = false; %>

<img class="onb-hero" src="/img/icon-hand.png?v=1" alt="">
<h1 class="onb-title">보호자 PIN을 만들어주세요</h1>

<form method="post" action="/onboarding/pin">
    <%-- 점은 글꼴이 아니라 .onb-pin-dots 가 그린다 (auth-validate.js 가 입력 수만큼 켠다) --%>
    <div class="kd-field">
        <label class="kd-label" for="pin">보호자 PIN 4자리 <span class="req">*</span></label>
        <div class="onb-pin-box">
            <input class="kd-input onb-pin" type="password" id="pin" name="pin"
                   inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
            <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
        </div>
        <p class="kd-hint">숫자 4자리를 입력해 주세요</p>
    </div>
    <div class="kd-field">
        <label class="kd-label" for="pinCheck">PIN 확인 <span class="req">*</span></label>
        <div class="onb-pin-box">
            <input class="kd-input onb-pin" type="password" id="pinCheck" name="pinCheck"
                   inputmode="numeric" maxlength="4" autocomplete="new-password" data-nocopy>
            <span class="onb-pin-dots" aria-hidden="true"><i></i><i></i><i></i><i></i></span>
        </div>
        <p class="kd-hint">한 번 더 똑같이 입력해 주세요</p>
    </div>
</form>

<%@ include file="../common/onb-bottom.jsp" %>
