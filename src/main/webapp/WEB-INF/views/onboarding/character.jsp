<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 캐릭터"; int onbStep = 2; String onbCol = ""; String onbNext = "kdSubmitOnbCharacter()"; %>
<%@ include file="../common/onb-top.jsp" %>

<h1 class="onb-title">함께할 친구를 골라 주세요</h1>
<p class="onb-sub">아이가 좋아하는 친구를 고르면 이야기에 자주 나와요</p>

<form method="post" action="/onboarding/character">
    <%-- data-nickname: 카드를 고르면 아래 애칭칸의 placeholder 가 이 값으로 바뀐다 --%>
    <div class="kd-field onb-field onb-charfield">
        <div class="onb-chars">
            <label class="onb-char">
                <input type="radio" name="character" id="charHaru" value="haru" data-nickname="하루">
                <img src="/img/char-haru.png?v=2" alt="">
                <span class="nm">하루</span>
            </label>
            <label class="onb-char">
                <input type="radio" name="character" id="charBori" value="bori" data-nickname="보리">
                <img src="/img/char-bori.png?v=2" alt="">
                <span class="nm">보리</span>
            </label>
        </div>
    </div>

    <div class="onb-nickname">
        <input class="kd-input" type="text" id="characterName" name="characterName"
               placeholder="예) 토리야">
    </div>
</form>

<%@ include file="../common/onb-bottom.jsp" %>
