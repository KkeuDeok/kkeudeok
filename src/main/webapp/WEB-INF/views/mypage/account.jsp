<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "account"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- Figma 24:15148. 보호자 정보는 UserController.mypageAccount 가 담아 준 ${user} 를 쓴다.
     DB 에서 온 값이라 c:out 으로 이스케이프한다(그냥 찍으면 XSS — find-id-result.jsp 와 같은 규칙).
     ⚠ 알림 설정 토글 3개와 아동 프로필 탭은 아직 예시 값이다. --%>
<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec">
    <%-- '이름' 만 쓰면 아동 프로필의 '아이 이름' 과 구분이 안 돼 두 탭이 겹쳐 보인다는 지적(2026-08-09).
         누구 정보인지 라벨로 못박는다. --%>
    <h2>보호자 정보</h2>
    <div class="mp-grid">
        <div class="kd-field">
            <label class="kd-label" for="mpName">보호자 이름</label>
            <%-- data-kd="guardianName" 를 뗐다. 붙여 두면 auth-validate.js 의 renderAppOnb() 가
                 sessionStorage(kdOnb)에 남은 '가입 당시 이름'으로 이 칸을 덮어써서,
                 다른 계정으로 로그인해도 앞사람 이름이 보인다. 이제 값의 주인은 서버다. --%>
            <input class="kd-input" type="text" id="mpName" value="<c:out value='${user.name}'/>">
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpEmail">이메일</label>
            <%-- 이메일은 계정 식별자라 수정 불가. DB 엔 암호문이고 서비스가 복호화해 넘겨준다 --%>
            <input class="kd-input mp-input-ro" type="email" id="mpEmail"
                   value="<c:out value='${user.email}'/>" readonly>
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpPhone">휴대폰</label>
            <%-- 아직 채우는 화면이 없어 대개 NULL 이다 → 빈 칸 + 안내 문구 --%>
            <input class="kd-input" type="tel" id="mpPhone" value="<c:out value='${user.phone}'/>"
                   placeholder="010-0000-0000">
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpRel">아이와의 관계</label>
            <%-- onb-select.js 가 select.onb-select 를 커스텀 드롭다운으로 바꿔 준다.
                 저장된 값과 같은 항목만 selected — 값이 없으면 아무것도 안 골라진다 --%>
            <select class="onb-select" id="mpRel" style="width:520px">
                <option <c:if test="${user.relation eq '어머니'}">selected</c:if>>어머니</option>
                <option <c:if test="${user.relation eq '아버지'}">selected</c:if>>아버지</option>
                <option <c:if test="${user.relation eq '조부모'}">selected</c:if>>조부모</option>
                <option <c:if test="${user.relation eq '기타 보호자'}">selected</c:if>>기타 보호자</option>
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
    <button type="button" class="kd-btn kd-btn-primary" onclick="kdSaveInfo()">저장</button>
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
        <button type="button" class="kd-btn kd-btn-primary" onclick="kdLogout()">로그아웃</button>
    </div>
</dialog>

<dialog id="dlgDelete" class="terms-dialog mp-modal">
    <%-- 로그아웃 모달과 같은 아이콘 블록. 없으면 그만큼 낮아져 두 모달 크기가 달라진다.
         .ic-danger 가 원 배경과 그림(휴지통)만 danger 색으로 바꾼다. --%>
    <span class="ic ic-danger"></span>
    <h2>계정을 삭제하시겠습니까?</h2>
    <%-- 원문은 4줄이라 모달이 뚱뚱해 보였다(2026-08-14 지적) → 내용은 지키고 2줄로.
         원문 그대로면 833px 이라 2줄(줄당 416px 한도)에 물리적으로 안 들어간다.
         덜어낸 건 '계정을 삭제하면'(바로 위 제목이 이미 말한다)과 '포함한'→'등' 뿐이다.
         실측: 1줄 346px · 2줄 337px (canvas 아닌 실제 렌더 폭, 2026-08-14).
         줄바꿈은 <br> 로 못 박는다 — 브라우저에 맡기면 뜻이 이어지는 자리에서 끊긴다.
         로그아웃 모달도 같은 방식이다. --%>
    <p class="warn">아이의 학습 데이터, 감정 분석 리포트, 캐릭터 프로필 등<br>모든 정보가 영구히 소멸되며 절대 복구할 수 없습니다.</p>
    <%-- 실패 사유가 붙을 자리. alert 대신 모달 안에서 보여 준다 --%>
    <p class="warn" id="delError" hidden></p>
    <div class="row">
        <button type="button" class="kd-btn kd-btn-outline" onclick="this.closest('dialog').close()">취소</button>
        <button type="button" class="kd-btn kd-btn-danger" onclick="kdDeleteAccount()">삭제</button>
    </div>
</dialog>

<script>
    /* 서버 세션(SS_USER_ID)을 지우고 로그인 화면으로 보낸다.
       링크로 /login 만 가면 세션이 살아있어 주소만 다시 치면 되돌아온다. */
    function kdLogout() {
        fetch('/logoutProc', {method: 'POST'})
            .then(function (res) {
                return res.json();
            })
            .then(function () {
                /* 모달에서 이미 물어봤고 로그인 화면이 뜨는 것 자체가 결과 보고다.
                   alert 는 [확인]을 한 번 더 눌러야 넘어가 흐름이 끊긴다. */
                location.href = '/login';
            })
            .catch(function () {
                alert('로그아웃에 실패했어요. 잠시 후 다시 시도해 주세요.');
            });
    }

    /* 회원정보 저장. 결과 문구는 서버가 준 것을 그대로 공통 토스트(kdSaved)에 띄운다. */
    function kdSaveInfo() {
        var name = document.getElementById('mpName');
        var phone = document.getElementById('mpPhone');
        /* onb-select.js 가 커스텀 드롭다운을 그리지만 원본 select 의 selectedIndex 도 같이 바꾼다 */
        var rel = document.getElementById('mpRel');

        /* 빈 이름은 서버까지 갈 것 없이 여기서 잡는다. 서버도 어차피 다시 본다. */
        if (!name.value.trim()) {
            kdSaved('보호자 이름을 입력해 주세요');
            name.focus();
            return;
        }

        fetch('/updateUserInfoProc', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: 'userName=' + encodeURIComponent(name.value.trim()) +
                '&phone=' + encodeURIComponent(phone.value.trim()) +
                '&relation=' + encodeURIComponent(rel.value)
        })
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                kdSaved(data.msg);
                if (data.result === 1) {
                    /* 서버에 저장된 값(앞뒤 공백 제거본)과 화면을 맞춘다 */
                    name.value = name.value.trim();
                    phone.value = phone.value.trim();
                }
            })
            .catch(function () {
                kdSaved('통신에 실패했어요. 잠시 후 다시 시도해 주세요');
            });
    }

    /* 계정 완전 삭제. 로그아웃과 달리 실패할 수 있어서(세션 끊김·이미 삭제됨) result 를 본다.
       비밀번호 재확인은 팀 결정으로 뺐다(2026-08-14) — 모달의 [삭제] 한 번이면 바로 지워진다. */
    function kdDeleteAccount() {
        var err = document.getElementById('delError');
        err.hidden = true;

        fetch('/deleteAccountProc', {method: 'POST'})
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                if (data.result === 1) {
                    location.href = '/login';      // 계정도 세션도 사라졌다
                } else {
                    err.textContent = data.msg;    // 서버가 준 사유를 모달 안에 그대로
                    err.hidden = false;
                }
            })
            .catch(function () {
                err.textContent = '통신에 실패했어요. 잠시 후 다시 시도해 주세요.';
                err.hidden = false;
            });
    }
</script>

<%@ include file="../common/app-bottom.jsp" %>
