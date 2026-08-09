<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "character"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- Figma 24:15532. 캐릭터 6명은 전신 시트에서 잘라 만든다(kkeudeok-tools/verify/make_icons_v7.py).
     선택 상태(테두리·배지)는 :has(input:checked) 로 처리 — CSS 만.
     좌측 미리보기 교체는 2026-08-09 팀장 피드백으로 추가(맨 아래 스크립트).
     ⚠ 소개 문구 6줄은 임시 — 팀장·기획 확정 필요.
     ⚠ `?v=` 를 빼면 브라우저가 옛 그림을 계속 쓴다 — 자산을 바꿀 때 여기 숫자도 같이 올릴 것. --%>
<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec">
    <div class="mp-char">
        <div class="mp-char-view">
            <img class="big" id="charBig" src="/img/char-tori-icon.png?v=118" alt="토리">
            <p class="nm" id="charName">토리</p>
            <p class="ds" id="charDesc">마음을 함께 읽어주는 다정한 친구예요</p>
            <div class="kd-field">
                <label class="kd-label" for="charNick">캐릭터 애칭</label>
                <input class="kd-input" type="text" id="charNick" value="토리야" data-auto="토리야">
            </div>
        </div>

        <div class="mp-char-pick">
            <h2>함께할 친구 고르기</h2>
            <div class="mp-chars">
                <label>
                    <input type="radio" name="character" value="tori" checked
                           data-big="/img/char-tori-icon.png?v=118" data-ds="마음을 함께 읽어주는 다정한 친구예요">
                    <img src="/img/char-tori-icon.png?v=118" alt=""><span class="nm">토리</span>
                </label>
                <%-- 윗줄 3명 여자 · 아랫줄 3명 남자로 보이도록 보미↔코코 자리를 바꿨다(2026-08-09) --%>
                <label>
                    <input type="radio" name="character" value="koko"
                           data-big="/img/char-koko.png?v=118" data-ds="궁금한 게 많은 씩씩한 친구예요">
                    <img src="/img/char-koko.png?v=118" alt=""><span class="nm">코코</span>
                </label>
                <label>
                    <input type="radio" name="character" value="lala"
                           data-big="/img/char-lala.png?v=118" data-ds="노래하며 기분을 밝게 해 주는 친구예요">
                    <img src="/img/char-lala.png?v=118" alt=""><span class="nm">라라</span>
                </label>
                <label>
                    <input type="radio" name="character" value="bomi"
                           data-big="/img/char-bomi.png?v=118" data-ds="언제나 웃으며 응원해 주는 친구예요">
                    <img src="/img/char-bomi.png?v=118" alt=""><span class="nm">보미</span>
                </label>
                <label>
                    <input type="radio" name="character" value="bada"
                           data-big="/img/char-bada.png?v=118" data-ds="천천히 기다려 주는 차분한 친구예요">
                    <img src="/img/char-bada.png?v=118" alt=""><span class="nm">바다</span>
                </label>
                <label>
                    <input type="radio" name="character" value="rubi"
                           data-big="/img/char-rubi.png?v=118" data-ds="속상한 날 곁에 있어 주는 친구예요">
                    <img src="/img/char-rubi.png?v=118" alt=""><span class="nm">루비</span>
                </label>
            </div>
        </div>
    </div>
</section>

<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <button type="button" class="kd-btn kd-btn-primary" onclick="kdSaved('캐릭터를 저장했어요')">저장</button>
</div>

<script>
// 카드를 고르면 좌측 미리보기(그림·이름·소개)가 바뀐다.
// 애칭은 사용자가 직접 고친 값이면 건드리지 않는다 — 자동으로 넣어 둔 값일 때만 바꾼다.
document.querySelectorAll('.mp-chars input').forEach(function (radio) {
    radio.addEventListener('change', function () {
        var name = radio.parentElement.querySelector('.nm').textContent;
        var nick = document.getElementById('charNick');
        document.getElementById('charBig').src = radio.dataset.big;
        document.getElementById('charBig').alt = name;
        document.getElementById('charName').textContent = name;
        document.getElementById('charDesc').textContent = radio.dataset.ds;
        if (nick.value === nick.dataset.auto) nick.value = name + '야';
        nick.dataset.auto = name + '야';
    });
});
</script>

<%@ include file="../common/app-bottom.jsp" %>
