<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="kopo.kkeudeok.dto.ChildDTO" %>
<%@ page import="kopo.kkeudeok.dto.CharacterType" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "character"; %>
<%@ include file="../common/app-top.jsp" %>

<%
    ChildDTO child = (ChildDTO) request.getAttribute("child");
    Long childId = null;
    String nickname = null;

    CharacterType picked = CharacterType.DEFAULT;

    if (child != null) {
        childId = child.getChildId();
        picked = CharacterType.orDefault(child.getCharacterType());

        if (child.getCharacterNickname() != null && !child.getCharacterNickname().isEmpty()) {
            nickname = child.getCharacterNickname();
        }
    }

    String charKey = picked.key();
    if (nickname == null) nickname = picked.label();
%>

<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<input type="hidden" id="childId" value="<%= childId != null ? childId : "" %>">

<section class="mp-sec">
    <div class="mp-char">
        <div class="mp-char-view">
            <div class="mp-char-box">
                <img class="big" id="charBig" src="/img/char-<%= charKey %>-neutral.png" alt="<%= nickname %>">
            </div>
            <p class="nm" id="charName"><%= picked.label() %></p>
            <p class="ds" id="charDesc"><%= picked.description() %></p>
            <div class="kd-field">
                <label class="kd-label" for="charNick">캐릭터 애칭</label>
                <input class="kd-input" type="text" id="charNick" value="<%= nickname %>" data-auto="<%= nickname %>">
            </div>
        </div>

        <div class="mp-char-pick">
            <h2>함께할 친구 고르기</h2>
            <div class="mp-chars">
                <% for (CharacterType c : CharacterType.values()) {
                       String charImg = "/img/char-" + c.key() + "-neutral.png"; %>
                <label>
                    <input type="radio" name="character" value="<%= c.key() %>" <%= c == picked ? "checked" : "" %>
                           data-big="<%= charImg %>" data-ds="<%= c.description() %>">
                    <img src="<%= charImg %>" alt=""><span class="nm"><%= c.label() %></span>
                </label>
                <% } %>
            </div>
        </div>
    </div>
</section>

<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <button type="button" class="kd-btn kd-btn-primary" onclick="saveCharacterDB()">저장</button>
</div>

<script>
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

        fetch('/child/updateCharacter', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                childId: parseInt(childId, 10),
                characterType: charKey,
                characterNickname: nickname
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
