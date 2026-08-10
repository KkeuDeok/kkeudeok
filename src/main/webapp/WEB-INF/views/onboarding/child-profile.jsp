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
         2026-08-10 피드백 2 — "중복 장애면 어떻게 하냐". 지적+자폐 동반 진단이 흔해서
         유형만 select → 체크박스 복수 선택으로 바꿨다. 정도는 그대로 하나.
         ⚠ 안내 문구는 라벨 안에 넣는다 — 아래 줄로 빼면 필드가 밀려 온보딩 레일(내용 끝 785)이 깨진다. --%>
    <div class="kd-field onb-field">
        <span class="onb-label">장애 정보 <span class="req">*</span>
            <span class="onb-note">중복 진단이면 여러 개 고를 수 있어요</span></span>
        <div class="onb-row onb-row--gap">
            <div class="onb-radios onb-radios--dis">
                <span class="onb-radio-item">
                    <input class="onb-radio" type="checkbox" id="disAutism" name="disabilityType" value="자폐 장애">
                    <label for="disAutism">자폐</label>
                </span>
                <span class="onb-radio-item">
                    <input class="onb-radio" type="checkbox" id="disIntel" name="disabilityType" value="지적 장애">
                    <label for="disIntel">지적</label>
                </span>
                <span class="onb-radio-item">
                    <input class="onb-radio" type="checkbox" id="disDev" name="disabilityType" value="발달 장애">
                    <label for="disDev">발달</label>
                </span>
            </div>
            <select class="kd-input onb-select" id="disabilityLevel" name="disabilityLevel">
                <option value="">정도 선택</option>
                <option>경증</option>
                <option>중증</option>
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
