<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 캐릭터"; int onbStep = 2; String onbCol = ""; String onbNext = "kdSubmitOnbCharacter()"; %>
<%@ include file="../common/onb-top.jsp" %>

<h1 class="onb-title">함께할 친구를 골라 주세요</h1>
<p class="onb-sub">아이가 좋아하는 친구를 고르면 이야기에 자주 나와요</p>

<form method="post" action="/onboarding/character">
    <%-- data-nickname: 카드를 고르면 아래 애칭칸의 placeholder 가 이 값으로 바뀐다 --%>
    <div class="kd-field onb-field onb-charfield">
        <div class="onb-chars">
            <%-- 하루·보리는 아트셋이 달라, 여기서 고른 친구가 사이드바·마이페이지에 그대로 나오면
                 캐릭터 관리 6명(토리·코코·라라·보미·바다·루비)과 그림체가 어긋난다.
                 같은 세트의 토리·바다로 맞춘다(2026-08-09 요청). --%>
            <label class="onb-char">
                <input type="radio" name="character" id="charTori" value="tori" data-nickname="토리">
                <img src="/img/char-tori-icon.png?v=119" alt="">
                <span class="nm">토리</span>
            </label>
            <label class="onb-char">
                <input type="radio" name="character" id="charBada" value="bada" data-nickname="바다">
                <img src="/img/char-bada.png?v=119" alt="">
                <span class="nm">바다</span>
            </label>
        </div>
    </div>

    <div class="onb-nickname">
        <input class="kd-input" type="text" id="characterName" name="characterName"
               placeholder="예) 토리야">
    </div>
</form>

<%@ include file="../common/onb-bottom.jsp" %>
