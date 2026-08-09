<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "roadmap"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- 보호자가 AI 추천 12주 커리큘럼을 직접 고치는 화면.
     주제·순서·포함여부를 바꾸면 대시보드 로드맵 카드와 '전체 보기' 모달이 그대로 따라온다.
     값·프리셋·저장은 전부 kd-roadmap.js 가 들고 있다 — 이 파일은 껍데기만.

     세로 예산: 본문 시작 251 + 세그먼트 73 + 목록 6행×48 + 되돌리기 30 + 버튼행 92 → 1024 안.
     12주를 한 줄씩 세로로 놓으면 넘쳐서 2열 6행으로 나눴다(좌 1~6주 / 우 7~12주). --%>
<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec" id="roadmapEdit">
    <h2>학습 로드맵</h2>

    <div class="mp-plans">
        <label><input type="radio" name="planMode" value="ai"><span>AI 맞춤</span></label>
        <label><input type="radio" name="planMode" value="standard"><span>정석</span></label>
        <label><input type="radio" name="planMode" value="custom"><span>직접 구성</span></label>
        <p class="ds">주제를 바꾸거나 순서를 옮기면 자동으로 <b>직접 구성</b>이 돼요</p>
    </div>

    <div class="mp-weeks">
        <%
            /* 주제 후보는 kd-roadmap.js 의 KD_TOPICS 와 같아야 한다.
               ⚠ 여기만 고치면 저장값이 목록에 없어 드롭다운이 비어 보인다 — 두 곳을 함께 고칠 것. */
            String[] topics = {
                "표정 알아보기", "기쁨 알아차리기", "슬픔 알아차리기", "화남 알아차리기",
                "놀람 알아차리기", "무서움 알아차리기", "표정으로 표현하기", "몸짓으로 표현하기",
                "감정 표현하기 — 슬픔 연습", "감정 표현하기 — 기쁨 연습", "복합 감정 이해하기",
                "친구 위로하기", "차례 지키기", "다툰 뒤 화해하기", "도움 요청하기",
                "마음 이야기 나누기", "눈 맞추고 대화하기", "먼저 말 걸어보기",
                "상황과 감정 잇기", "가정에서 연습하기", "학교에서 연습하기", "스스로 돌아보기"
            };
            for (int w = 1; w <= 12; w++) {
        %>
        <div class="mp-week">
            <span class="no"><%= w %></span>
            <select class="kd-input onb-select" aria-label="<%= w %>주차 학습 주제">
                <% for (String t : topics) { %>
                <option value="<%= t %>"><%= t %></option>
                <% } %>
            </select>
            <button type="button" class="mv up" aria-label="<%= w %>주차 위로"></button>
            <button type="button" class="mv dn" aria-label="<%= w %>주차 아래로"></button>
            <label class="mp-toggle" title="이 주차 포함">
                <input type="checkbox" checked aria-label="<%= w %>주차 포함"><span></span>
            </label>
        </div>
        <% } %>
    </div>

    <div class="mp-links">
        <button type="button" id="planReset">기본값(AI 추천)으로 되돌리기</button>
    </div>
</section>

<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <button type="button" class="kd-btn kd-btn-primary" id="planSave">저장</button>
</div>

<%@ include file="../common/app-bottom.jsp" %>
