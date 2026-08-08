<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "account"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- Figma 24:15148. ponytail: 값은 전부 예시 — 백엔드가 붙으면 그대로 갈아끼운다. --%>
<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec">
    <h2>기본 정보</h2>
    <div class="mp-grid">
        <div class="kd-field">
            <label class="kd-label" for="mpName">이름</label>
            <input class="kd-input" type="text" id="mpName" value="김지우">
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpEmail">이메일</label>
            <%-- 이메일은 계정 식별자라 수정 불가 --%>
            <input class="kd-input mp-input-ro" type="email" id="mpEmail" value="jiu@example.com" readonly>
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpPhone">휴대폰</label>
            <input class="kd-input" type="tel" id="mpPhone" value="010-1234-5678">
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpRel">관계</label>
            <%-- onb-select.js 가 select.onb-select 를 커스텀 드롭다운으로 바꿔 준다 --%>
            <select class="onb-select" id="mpRel" style="width:520px">
                <option selected>어머니</option>
                <option>아버지</option>
                <option>조부모</option>
                <option>기타 보호자</option>
            </select>
        </div>
    </div>
</section>

<hr class="mp-rule">

<section class="mp-sec">
    <h2>알림 설정</h2>
    <div class="mp-switch">
        <span class="tx"><b>주간 리포트 알림</b><span class="ds">매주 월요일 지난주 성장 리포트를 보내드려요</span></span>
        <label class="mp-toggle"><input type="checkbox" checked><span></span></label>
    </div>
    <div class="mp-switch">
        <span class="tx"><b>학습 리마인더</b><span class="ds">아이가 3일 이상 학습하지 않으면 알려드려요</span></span>
        <label class="mp-toggle"><input type="checkbox" checked><span></span></label>
    </div>
    <div class="mp-switch">
        <span class="tx"><b>마케팅 정보 수신</b><span class="ds">새 소식과 이벤트 정보를 받아볼 수 있어요</span></span>
        <label class="mp-toggle"><input type="checkbox"><span></span></label>
    </div>
</section>

<section class="mp-sec">
    <h2>계정</h2>
    <div class="mp-links">
        <button type="button" onclick="dlgLogout.showModal()">로그아웃</button>
        <span class="sep"></span>
        <a class="arrow" href="/find-pw">비밀번호 변경</a>
    </div>
    <div class="mp-links">
        <button type="button" class="danger" onclick="dlgDelete.showModal()">계정 삭제</button>
        <span class="sep"></span>
        <a class="arrow" href="/mypage/pin-reset">보호자 PIN 변경</a>
    </div>
</section>

<div class="mp-actions">
    <a class="kd-btn kd-btn-outline" href="/dashboard">취소</a>
    <button type="button" class="kd-btn kd-btn-primary" onclick="kdSaved('회원정보를 저장했어요')">저장</button>
</div>

<%-- 확인 모달 2개 (Figma 439:2129 · 439:2259).
     네이티브 <dialog> 라 JS 파일이 필요 없다 — showModal() 로 열고 close() 로 닫는다.
     .terms-dialog 를 같이 붙여야 backdrop 과 zoom 상쇄를 물려받는다. --%>
<dialog id="dlgLogout" class="terms-dialog mp-modal">
    <span class="ic"></span>
    <h2>로그아웃 하시겠습니까?</h2>
    <p>로그아웃하시면 아이의 일일 학습 분석 및<br>실시간 AI 성장 리포트 수집이 잠시 일시정지됩니다.</p>
    <div class="row">
        <button type="button" class="kd-btn kd-btn-outline" onclick="this.closest('dialog').close()">취소</button>
        <a class="kd-btn kd-btn-primary" href="/login">로그아웃</a>
    </div>
</dialog>

<dialog id="dlgDelete" class="terms-dialog mp-modal">
    <h2>계정을 삭제하시겠습니까?</h2>
    <p class="warn">계정을 삭제하면 아이의 학습 데이터, 감정 분석 리포트, 캐릭터 프로필을 포함한
        모든 정보가 영구히 소멸되며 절대 복구할 수 없습니다.</p>
    <div class="row">
        <button type="button" class="kd-btn kd-btn-outline" onclick="this.closest('dialog').close()">취소</button>
        <a class="kd-btn kd-btn-danger" href="/login">삭제</a>
    </div>
</dialog>

<%@ include file="../common/app-bottom.jsp" %>
