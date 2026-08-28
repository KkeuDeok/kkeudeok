<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<% String pageTitle = "마이페이지"; String appNav = "mypage"; String mpTab = "account"; %>
<%@ include file="../common/app-top.jsp" %>

<div class="app-head mp-head">
    <h1>마이페이지</h1>
</div>

<div class="mp-tabsrow">
    <%@ include file="../common/mypage-tabs.jsp" %>
</div>

<section class="mp-sec">
    <h2>보호자 정보</h2>
    <div class="mp-grid">
        <div class="kd-field">
            <label class="kd-label" for="mpName">보호자 이름</label>
            <input class="kd-input" type="text" id="mpName" value="<c:out value='${user.name}'/>">
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpEmail">이메일</label>
            <input class="kd-input mp-input-ro" type="email" id="mpEmail"
                   value="<c:out value='${user.email}'/>" readonly>
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpPhone">휴대폰</label>
            <input class="kd-input" type="tel" id="mpPhone" value="<c:out value='${user.phone}'/>"
                   inputmode="numeric" maxlength="13" placeholder="010-0000-0000"
                   oninput="kdPhoneFormat(this)">
        </div>
        <div class="kd-field">
            <label class="kd-label" for="mpRel">아이와의 관계</label>
            <select class="onb-select" id="mpRel" style="width:520px">
                <option <c:if test="${user.relation eq '어머니'}">selected</c:if>>어머니</option>
                <option <c:if test="${user.relation eq '아버지'}">selected</c:if>>아버지</option>
                <option <c:if test="${user.relation eq '조부모'}">selected</c:if>>조부모</option>
                <option <c:if test="${user.relation eq '기타 보호자'}">selected</c:if>>기타 보호자</option>
            </select>
        </div>
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
    <span class="ic ic-danger"></span>
    <h2>계정을 삭제하시겠습니까?</h2>
    <p class="warn">아이의 학습 데이터, 감정 분석 리포트, 캐릭터 프로필 등<br>모든 정보가 영구적으로 삭제됩니다.</p>
    <p class="warn" id="delError" hidden></p>
    <div class="row">
        <button type="button" class="kd-btn kd-btn-outline" onclick="this.closest('dialog').close()">취소</button>
        <button type="button" class="kd-btn kd-btn-danger" onclick="kdDeleteAccount()">삭제</button>
    </div>
</dialog>

<script>
    function kdLogout() {
        fetch('/logoutProc', {method: 'POST'})
            .then(function (res) {
                return res.json();
            })
            .then(function () {
                location.href = '/login';
            })
            .catch(function () {
                alert('로그아웃에 실패했어요. 잠시 후 다시 시도해 주세요.');
            });
    }

    function kdPhoneFormat(el) {
        var atEnd = el.selectionStart === el.value.length;
        var d = el.value.replace(/\D/g, '').slice(0, 11);
        el.value = d.length < 4 ? d
            : d.length < 8 ? d.slice(0, 3) + '-' + d.slice(3)
                : d.slice(0, 3) + '-' + d.slice(3, 7) + '-' + d.slice(7);

        if (atEnd) el.setSelectionRange(el.value.length, el.value.length);
    }

    kdPhoneFormat(document.getElementById('mpPhone'));

    function kdSaveInfo() {
        var name = document.getElementById('mpName');
        var phone = document.getElementById('mpPhone');
        var rel = document.getElementById('mpRel');
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
                    name.value = name.value.trim();
                    phone.value = phone.value.trim();
                }
            })
            .catch(function () {
                kdSaved('통신에 실패했어요. 잠시 후 다시 시도해 주세요');
            });
    }
    function kdDeleteAccount() {
        var err = document.getElementById('delError');
        err.hidden = true;

        fetch('/deleteAccountProc', {method: 'POST'})
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                if (data.result === 1) {
                    location.href = '/login';
                } else {
                    err.textContent = data.msg;
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
