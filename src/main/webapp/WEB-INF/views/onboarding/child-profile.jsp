<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.Year" %>
<% String pageTitle = "온보딩 - 아이 정보"; int onbStep = 1; String onbCol = ""; String onbNext = "kdSubmitOnbProfile()"; %>
<%@ include file="../common/onb-top.jsp" %>

<h1 class="onb-title">아이 정보를 알려주세요</h1>
<p class="onb-sub">학습 내용을 아이에게 맞추는 데 사용해요</p>

<%-- 입력 블록에 kd-field 와 onb-field 를 같이 다는 이유:
     kd-field 는 auth-validate.js 가 오류 문구를 붙일 때 closest() 로 찾는 앵커고,
     onb-field 는 온보딩 규격(입력칸 34·간격 42)으로 덮어쓰는 쪽이다. --%>
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

    <%-- 유형·정도 목록은 팀장 확정본 (2026-08-08 카톡 피드백).
         2026-08-11 — 유형은 다시 드롭다운 하나(중복 선택 없음)로 돌아갔고,
         정도는 유형에 딸린다: 자폐·지적은 중증만, 발달만 경증/중증.
         ⚠ 정도 옵션은 JSP 가 아니라 auth-validate.js 의 DIS_LEVELS 가 채운다 —
           여기에 <option> 을 박아 두면 두 곳이 어긋난다. --%>
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
