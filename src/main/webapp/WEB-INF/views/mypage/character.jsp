<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="kopo.kkeudeok.dto.ProfileDTO" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "character"; %>
<%@ include file="../common/app-top.jsp" %>

<%
    // 1. DB에서 아이 정보 읽어오기
    ProfileDTO child = (ProfileDTO) request.getAttribute("child");
    Long childId = null;
    String charKey = "tori"; // 기본값
    String nickname = "토리"; // 기본값

    if (child != null) {
        childId = child.getChildId();
        // DTO 필드명(characterType, characterNickname)과 매칭
        if (child.getCharacterType() != null && !child.getCharacterType().isEmpty()) {
            charKey = child.getCharacterType();
        }
        if (child.getCharacterNickname() != null && !child.getCharacterNickname().isEmpty()) {
            nickname = child.getCharacterNickname();
        }
    }
    String defaultCharName = "토리"; // 기본값
    if ("koko".equals(charKey)) defaultCharName = "코코";
    else if ("lala".equals(charKey)) defaultCharName = "라라";
    else if ("bada".equals(charKey)) defaultCharName = "바다";
    else if ("bomi".equals(charKey)) defaultCharName = "보미";
    else if ("rubi".equals(charKey)) defaultCharName = "루비";

    String defaultexplanation = "마음을 함께 읽어주는 다정한 친구예요"; // 기본값
    if ("koko".equals(charKey)) defaultexplanation = "궁금한 게 많은 씩씩한 친구예요";
    else if ("lala".equals(charKey)) defaultexplanation = "노래하며 기분을 밝게 해 주는 친구예요";
    else if ("bada".equals(charKey)) defaultexplanation = "천천히 기다려 주는 차분한 친구예요";
    else if ("bomi".equals(charKey)) defaultexplanation = "언제나 웃으며 응원해 주는 친구예요";
    else if ("rubi".equals(charKey)) defaultexplanation = "속상한 날 곁에 있어 주는 친구예요";
%>

<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<!-- childId 숨김 태그 (AJAX 전송용) -->
<input type="hidden" id="childId" value="<%= childId != null ? childId : "" %>">

<section class="mp-sec">
    <div class="mp-char">
        <div class="mp-char-view">
            <div class="mp-char-box">
                <img class="big" id="charBig" src="/img/char-<%= charKey %>-neutral.png" alt="<%= nickname %>">
            </div>
            <p class="nm" id="charName"><%= defaultCharName %></p>
            <p class="ds" id="charDesc"><%= defaultexplanation %></p>
            <div class="kd-field">
                <label class="kd-label" for="charNick">캐릭터 애칭</label>
                <input class="kd-input" type="text" id="charNick" value="<%= nickname %>" data-auto="<%= nickname %>">
            </div>
        </div>

        <div class="mp-char-pick">
            <h2>함께할 친구 고르기</h2>
            <div class="mp-chars">
                <label>
                    <input type="radio" name="character" value="tori" <%= "tori".equals(charKey) ? "checked" : "" %>
                           data-big="/img/char-tori-neutral.png" data-ds="마음을 함께 읽어주는 다정한 친구예요">
                    <img src="/img/char-tori-neutral.png" alt=""><span class="nm">토리</span>
                </label>
                <label>
                    <input type="radio" name="character" value="koko" <%= "koko".equals(charKey) ? "checked" : "" %>
                           data-big="/img/char-koko-neutral.png" data-ds="궁금한 게 많은 씩씩한 친구예요">
                    <img src="/img/char-koko-neutral.png" alt=""><span class="nm">코코</span>
                </label>
                <label>
                    <input type="radio" name="character" value="lala" <%= "lala".equals(charKey) ? "checked" : "" %>
                           data-big="/img/char-lala-neutral.png" data-ds="노래하며 기분을 밝게 해 주는 친구예요">
                    <img src="/img/char-lala-neutral.png" alt=""><span class="nm">라라</span>
                </label>
                <label>
                    <input type="radio" name="character" value="bomi" <%= "bomi".equals(charKey) ? "checked" : "" %>
                           data-big="/img/char-bomi-neutral.png" data-ds="언제나 웃으며 응원해 주는 친구예요">
                    <img src="/img/char-bomi-neutral.png" alt=""><span class="nm">보미</span>
                </label>
                <label>
                    <input type="radio" name="character" value="bada" <%= "bada".equals(charKey) ? "checked" : "" %>
                           data-big="/img/char-bada-neutral.png" data-ds="천천히 기다려 주는 차분한 친구예요">
                    <img src="/img/char-bada-neutral.png" alt=""><span class="nm">바다</span>
                </label>
                <label>
                    <input type="radio" name="character" value="rubi" <%= "rubi".equals(charKey) ? "checked" : "" %>
                           data-big="/img/char-rubi-neutral.png" data-ds="속상한 날 곁에 있어 주는 친구예요">
                    <img src="/img/char-rubi-neutral.png" alt=""><span class="nm">루비</span>
                </label>
            </div>
        </div>
    </div>
</section>

<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <!-- DB 저장 스크립트 실행 -->
    <button type="button" class="kd-btn kd-btn-primary" onclick="saveCharacterDB()">저장</button>
</div>

<script>
    // 카드를 고르면 좌측 미리보기(그림·이름·소개) 동적 변경
    document.querySelectorAll('.mp-chars input').forEach(function (radio) {
        radio.addEventListener('change', function () {
            var name = radio.parentElement.querySelector('.nm').textContent;
            var nick = document.getElementById('charNick');
            document.getElementById('charBig').src = radio.dataset.big;
            document.getElementById('charBig').alt = name;
            document.getElementById('charName').textContent = name;
            document.getElementById('charDesc').textContent = radio.dataset.ds;
        });
    });

    // DB 캐릭터 업데이트 AJAX 전송 함수
    function saveCharacterDB() {
        const picked = document.querySelector('.mp-chars input[name="character"]:checked');
        if (!picked) return;

        const childId = document.getElementById('childId').value;
        const charKey = picked.value;
        const nickname = document.getElementById('charNick').value.trim();

        if (!childId) {
            alert("아이 정보를 찾지 못했습니다. 새로고침 후 다시 시도해 주세요.");
            return;
        }

        fetch('/profile/updateCharacter', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                childId: parseInt(childId, 10),
                characterType: charKey,          // ProfileDTO의 characterType
                characterNickname: nickname      // ProfileDTO의 characterNickname
            })
        })
            .then(res => {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(data => {
                if (data.result > 0) {
                    if (typeof kdSaved === 'function') kdSaved('캐릭터를 성공적으로 저장했어요');
                    else alert('캐릭터를 성공적으로 저장했어요');

                    // 🎯 1.2초(1200ms) 지연 후 새로고침 (토스트 메시지가 충분히 보이도록 함)
                    setTimeout(function() {
                        window.location.reload();
                    }, 1200);

                } else {
                    alert(data.msg || '캐릭터 저장에 실패했습니다.');
                }
            })
            .catch(err => {
                console.error('Error:', err);
                alert('저장 중 오류가 발생했습니다. (' + err.message + ')');
            });
    }
</script>

<%@ include file="../common/app-bottom.jsp" %>