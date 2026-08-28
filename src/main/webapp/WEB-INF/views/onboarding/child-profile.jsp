<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.Year" %>
<% String pageTitle = "온보딩 - 아이 정보"; int onbStep = 1; String onbCol = ""; String onbNext = "kdSubmitOnbProfile()"; %>
<%@ include file="../common/onb-top.jsp" %>

<h1 class="onb-title">아이 정보를 알려주세요</h1>
<p class="onb-sub">학습 내용을 아이에게 맞추는 데 사용해요</p>

<form method="post" action="/onboarding/profile">
    <div class="kd-field onb-field onb-field--hint">
        <label class="onb-label" for="childName">아이 이름 <span class="req">*</span></label>
        <input class="kd-input onb-input" type="text" id="childName" name="childName"
               placeholder="이름을 입력하세요">
        <p class="kd-hint">아이가 부르는 이름으로 적어 주세요</p>
    </div>

    <div class="kd-field onb-field">
        <label class="onb-label" for="birthYear">생년월일 <span class="req">*</span></label>
        <div class="onb-row">
            <select class="kd-input onb-select" id="birthYear" name="birthYear">
                <option value="">선택</option>
                <% for (int y = Year.now().getValue(); y >= 1990; y--) { %>
                <option value="<%= y %>"><%= y %></option>
                <% } %>
            </select>
            <span class="onb-unit">년</span>
            <select class="kd-input onb-select" id="birthMonth" name="birthMonth">
                <option value="">선택</option>
                <% for (int m = 1; m <= 12; m++) { %>
                <option value="<%= m %>"><%= m %></option>
                <% } %>
            </select>
            <span class="onb-unit">월</span>
            <select class="kd-input onb-select" id="birthDay" name="birthDay">
                <option value="">선택</option>
                <% for (int d = 1; d <= 31; d++) { %>
                <option value="<%= d %>"><%= d %></option>
                <% } %>
            </select>
            <span class="onb-unit">일</span>
        </div>
    </div>

    <div class="kd-field onb-field">
        <span class="onb-label">장애 정보 <span class="req">*</span></span>
        <div class="onb-row onb-row--gap">
            <select class="kd-input onb-select" id="disabilityType" name="disabilityType">
                <option value="">유형 선택</option>
                <option value="자폐 장애">자폐</option>
                <option value="지적 장애">지적</option>
                <option value="발달 장애">발달</option>
            </select>
            <select class="kd-input onb-select" id="disabilityLevel" name="disabilityLevel">
                <option value="">정도 선택</option>
            </select>
        </div>
    </div>

    <div class="onb-field">
        <span class="onb-label">성별</span>
        <div class="onb-radios">
            <span class="onb-radio-item">
                <input class="onb-radio" type="radio" id="genderBoy" name="gender" value="M">
                <label for="genderBoy">남아</label>
            </span>
            <span class="onb-radio-item">
                <input class="onb-radio" type="radio" id="genderGirl" name="gender" value="F">
                <label for="genderGirl">여아</label>
            </span>
        </div>
    </div>
</form>

<%@ include file="../common/onb-bottom.jsp" %>
