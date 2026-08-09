<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "roadmap"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- 보호자가 학습 커리큘럼을 고르고 고치는 화면.
     생김새는 온보딩 로드맵 비교 화면(Figma 306:75)과 같은 카드다 — 클래스도 .onb-plan 계열을 그대로 쓰고
     폭만 380 -> 340 으로 줄였다(340×3 + 40×2 = 1100 이라 마이페이지 레일 1120 안에 들어간다).
     주차별 상세 편집은 카드 아래 링크로 여는 모달에 있다 — 카드와 편집기를 한 화면에 같이 두면
     세로 예산(1024)을 넘긴다.

     6단계 미리보기와 12주 데이터의 관계: 정석·AI 는 온보딩과 같은 고정 문구를 쓰고,
     '직접 구성' 은 저장된 12주를 2주씩 묶어 kd-roadmap.js 가 채운다. --%>
<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec" id="roadmapPick">
    <div class="onb-plans mp-plans">
        <label class="onb-plan mp-plan">
            <input type="radio" name="planMode" value="standard">
            <span class="badge">표준</span>
            <h2>정석 커리큘럼</h2>
            <p class="lead">일반적인 순서로 진행하는 표준 과정</p>
            <div class="rule"></div>
            <div class="steps" data-plan="standard"></div>
        </label>

        <label class="onb-plan onb-plan--ai mp-plan">
            <input type="radio" name="planMode" value="ai">
            <span class="badge">AI 추천</span>
            <h2>AI 맞춤 커리큘럼</h2>
            <p class="lead">체크리스트 결과 기반 개인화 과정</p>
            <div class="rule"></div>
            <div class="steps" data-plan="ai"></div>
        </label>

        <%-- 만들기 전에는 없는 카드다. kd-roadmap.js 가 저장값이 custom 일 때만 켠다.
             정석·AI 옆에 늘 세워 두면 고른 안을 그대로 복사해 보여 주게 돼 옆 카드와 내용이 겹친다. --%>
        <label class="onb-plan mp-plan mp-plan--custom" hidden>
            <input type="radio" name="planMode" value="custom">
            <span class="badge">직접 구성</span>
            <%-- 카드를 누르면 편집이 열리므로 삭제는 stopPropagation 으로 따로 잡는다(kd-roadmap.js) --%>
            <button type="button" class="del" id="planDelete" aria-label="직접 만든 구성 삭제"></button>
            <h2>우리 아이 맞춤</h2>
            <p class="lead">주차별 주제와 순서를 직접 고른 과정</p>
            <div class="rule"></div>
            <div class="steps" data-plan="custom"></div>
        </label>
    </div>

    <div class="mp-links mp-plan-links">
        <button type="button" id="planEdit">주차별 전체 보기 · 바꾸기</button>
        <span class="sep"></span>
        <button type="button" id="planReset">기본값(AI 추천)으로 되돌리기</button>
    </div>
</section>

<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <button type="button" class="kd-btn kd-btn-primary" id="planSave">저장</button>
</div>

<%-- 주차별 편집 — .terms-dialog 를 반드시 같이 붙인다.
     빼면 --fit-scale 배율이 두 번 걸려 모달이 작아진다(마이페이지·대시보드 모달과 같은 규칙). --%>
<dialog id="dlgWeeks" class="terms-dialog roadmap-dlg">
    <h2>주차별 주제 · 순서</h2>
    <p class="ds">12주 전체입니다. 행을 끌어 원하는 자리에 놓거나 ▲▼로 옮길 수 있어요. 토글을 끄면 그 주차는 건너뜁니다.</p>

    <div class="mp-weeks" id="roadmapEdit">
        <%
            /* 주제 후보는 kd-roadmap.js 의 KD_TOPICS 와 같아야 한다.
               ⚠ 여기만 고치면 저장값이 목록에 없어 드롭다운이 비어 보인다 — 두 곳을 함께 고칠 것. */
            String[] topics = {
                "표정 알아보기", "기쁨 알아차리기", "슬픔 알아차리기", "화남 알아차리기",
                "놀람 알아차리기", "무서움 알아차리기", "표정으로 표현하기", "몸짓으로 표현하기",
                "슬픔 감정 표현하기", "기쁨 감정 표현하기", "복합 감정 이해하기",
                "친구 위로하기", "차례 지키기", "다툰 뒤 화해하기", "도움 요청하기",
                "마음 이야기 나누기", "눈 맞추고 대화하기", "먼저 말 걸어보기",
                "상황과 감정 잇기", "가정에서 연습하기", "학교에서 연습하기", "스스로 돌아보기"
            };
            for (int w = 1; w <= 12; w++) {
        %>
        <div class="mp-week">
            <span class="grip" aria-hidden="true"></span>
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

    <div class="ft">
        <button type="button" class="kd-btn kd-btn-outline" id="weeksCancel">취소</button>
        <button type="button" class="kd-btn kd-btn-primary" id="weeksApply">적용</button>
    </div>
</dialog>

<%@ include file="../common/app-bottom.jsp" %>
