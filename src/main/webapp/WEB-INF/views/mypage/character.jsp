<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "character"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- Figma 24:15532. 캐릭터 이미지 7장은 Figma 에서 그대로 받아 왔다.
     선택 상태는 :has(input:checked) 로 처리 — JS 0줄.
     ponytail: 좌측 미리보기는 선택에 따라 바뀌어야 하지만 백엔드/JS 없이 정적이라
     기본값(토리)만 보여 준다. --%>
<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec">
    <div class="mp-char">
        <div class="mp-char-view">
            <img class="big" src="/img/char-tori-full.png" alt="토리">
            <p class="nm">토리</p>
            <p class="ds">마음을 함께 읽어주는 다정한 친구예요</p>
            <div class="kd-field">
                <label class="kd-label" for="charNick">캐릭터 애칭</label>
                <input class="kd-input" type="text" id="charNick" value="토리야">
            </div>
        </div>

        <div class="mp-char-pick">
            <h2>함께할 친구 고르기</h2>
            <div class="mp-chars">
                <label>
                    <input type="radio" name="character" value="tori" checked>
                    <img src="/img/char-tori-icon.png" alt=""><span class="nm">토리</span>
                </label>
                <label>
                    <input type="radio" name="character" value="bomi">
                    <img src="/img/char-bomi.png" alt=""><span class="nm">보미</span>
                </label>
                <label>
                    <input type="radio" name="character" value="lala">
                    <img src="/img/char-lala.png" alt=""><span class="nm">라라</span>
                </label>
                <label>
                    <input type="radio" name="character" value="koko">
                    <img src="/img/char-koko.png" alt=""><span class="nm">코코</span>
                </label>
                <label>
                    <input type="radio" name="character" value="bada">
                    <img src="/img/char-bada.png" alt=""><span class="nm">바다</span>
                </label>
                <label>
                    <input type="radio" name="character" value="rubi">
                    <img src="/img/char-rubi.png" alt=""><span class="nm">루비</span>
                </label>
            </div>
        </div>
    </div>
</section>

<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <button type="button" class="kd-btn kd-btn-primary" onclick="kdSaved('캐릭터를 저장했어요')">저장</button>
</div>

<%@ include file="../common/app-bottom.jsp" %>
