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
        <span class="ava"><img src="/img/char-tori-icon.png" alt=""></span>
        <span class="tx">
            <b>지우</b>
            <span class="ds">만 7세 · 학습 시작 2025.11.03</span>
        </span>
    </div>
</section>

<section class="mp-sec">
    <h2>프로필 정보</h2>
    <div class="mp-grid">
        <div class="kd-field">
            <label class="kd-label" for="childName">아이 이름</label>
            <input class="kd-input" type="text" id="childName" value="지우">
        </div>

        <div class="kd-field">
            <label class="kd-label" for="birthYear">생년월일</label>
            <div class="onb-row">
                <select class="kd-input onb-select" id="birthYear" name="birthYear">
                    <% for (int y = Year.now().getValue(); y >= 1990; y--) { %>
                    <option value="<%= y %>"<%= y == 2018 ? " selected" : "" %>><%= y %>년</option>
                    <% } %>
                </select>
                <span class="onb-unit"></span>
                <select class="kd-input onb-select" id="birthMonth" name="birthMonth">
                    <% for (int m = 1; m <= 12; m++) { %>
                    <option value="<%= m %>"<%= m == 6 ? " selected" : "" %>><%= m %>월</option>
                    <% } %>
                </select>
                <span class="onb-unit"></span>
                <select class="kd-input onb-select" id="birthDay" name="birthDay">
                    <% for (int d = 1; d <= 31; d++) { %>
                    <option value="<%= d %>"<%= d == 12 ? " selected" : "" %>><%= d %>일</option>
                    <% } %>
                </select>
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

        <%-- 유형·정도 목록은 팀장 확정본 (2026-08-08 카톡 피드백) --%>
        <div class="kd-field">
            <label class="kd-label" for="disabilityType">장애 정보</label>
            <div class="onb-row onb-row--gap">
                <select class="kd-input onb-select" id="disabilityType" name="disabilityType">
                    <option selected>자폐 장애</option>
                    <option>지적 장애</option>
                    <option>발달 장애</option>
                    <option>기타</option>
                </select>
                <select class="kd-input onb-select" id="disabilityLevel" name="disabilityLevel">
                    <option>경증</option>
                    <option selected>중증</option>
                </select>
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
