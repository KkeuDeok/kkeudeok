<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.Year" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "child"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- Figma 24:15283. 폼 마크업은 온보딩 child-profile.jsp 를 그대로 가져왔다.
     ⚠ 사이드바는 "지우 · 6세", 여기는 Figma 원본대로 "만 7세" — 원본부터 어긋나 있어
        그대로 두고 팀장 확인 대상으로 남긴다. --%>
<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec">
    <div class="mp-profile">
        <span class="ava"><img data-kd-char="neutral" src="/img/char-tori-neutral.png" alt=""></span>
        <span class="tx">
            <b data-kd="childName">지우</b>
            <%-- ponytail: 학습 시작일은 아직 저장하는 곳이 없어 예시값 그대로다 --%>
            <span class="ds">만 <span data-kd="childAge">7</span>세 · 학습 시작 2025.11.03</span>
        </span>
    </div>
</section>

<section class="mp-sec">
    <%-- 보호자 정보 탭과 대칭 — 어느 쪽이 누구 정보인지 제목에서 갈린다 --%>
    <h2>아이 정보</h2>
    <div class="mp-grid mp-grid--child">
        <div class="kd-field">
            <label class="kd-label" for="childName">아이 이름</label>
            <input class="kd-input" type="text" id="childName" value="지우">
        </div>

        <%-- 온보딩 아동 프로필과 같은 방식 — 드롭다운에는 숫자만 두고 년·월·일은 밖에 라벨로 붙인다.
             예전에는 .onb-unit 이 빈 칸이고 단위가 옵션 글자에 섞여 있어 온보딩과 모양이 달랐다. --%>
        <div class="kd-field">
            <label class="kd-label" for="birthYear">생년월일</label>
            <div class="onb-row">
                <select class="kd-input onb-select" id="birthYear" name="birthYear">
                    <% for (int y = Year.now().getValue(); y >= 1990; y--) { %>
                    <option value="<%= y %>"<%= y == 2018 ? " selected" : "" %>><%= y %></option>
                    <% } %>
                </select>
                <span class="onb-unit">년</span>
                <select class="kd-input onb-select" id="birthMonth" name="birthMonth">
                    <% for (int m = 1; m <= 12; m++) { %>
                    <option value="<%= m %>"<%= m == 6 ? " selected" : "" %>><%= m %></option>
                    <% } %>
                </select>
                <span class="onb-unit">월</span>
                <select class="kd-input onb-select" id="birthDay" name="birthDay">
                    <% for (int d = 1; d <= 31; d++) { %>
                    <option value="<%= d %>"<%= d == 12 ? " selected" : "" %>><%= d %></option>
                    <% } %>
                </select>
                <span class="onb-unit">일</span>
            </div>
        </div>

        <div class="kd-field">
            <span class="kd-label">성별</span>
            <div class="mp-radios">
                <span class="onb-radio-item">
                    <input class="onb-radio" type="radio" id="genderBoy" name="gender" value="M" checked>
                    <label for="genderBoy">남자</label>
                </span>
                <span class="onb-radio-item">
                    <input class="onb-radio" type="radio" id="genderGirl" name="gender" value="F">
                    <label for="genderGirl">여자</label>
                </span>
            </div>
        </div>

        <%-- 유형·정도 목록은 팀장 확정본 (2026-08-08 카톡 피드백).
             ⚠ 유형 <option> 은 온보딩 child-profile.jsp 와 **같은 목록이 두 곳에 하드코딩**돼 있다.
               한쪽만 고치면 어긋난다.
             정도는 유형에 딸린다(2026-08-11) — 옵션은 auth-validate.js 의 DIS_LEVELS 가 채우므로
             여기는 비워 둔다. 마이페이지는 '정도 선택' 안내 칸 없이 바로 값이 박힌다. --%>
        <div class="kd-field">
            <span class="kd-label">장애 정보</span>
            <div class="onb-row onb-row--gap">
                <select class="kd-input onb-select" id="disabilityType" name="disabilityType">
                    <option value="자폐 장애" selected>자폐</option>
                    <option value="지적 장애">지적</option>
                    <option value="발달 장애">발달</option>
                </select>
                <select class="kd-input onb-select" id="disabilityLevel" name="disabilityLevel"></select>
            </div>
        </div>
    </div>
</section>

<%-- Figma 에는 이 버튼행이 없다 — 캐릭터 관리 탭과 대칭을 맞추려고 웹에서 추가 --%>
<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <button type="button" class="kd-btn kd-btn-primary" onclick="kdSaved('아이 프로필을 저장했어요')">저장</button>
</div>

<%@ include file="../common/app-bottom.jsp" %>
