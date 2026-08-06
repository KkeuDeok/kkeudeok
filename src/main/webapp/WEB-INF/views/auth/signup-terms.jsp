<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title>회원가입 - 약관동의 | 끄덕</title>
    <%@ include file="../common/head.jsp" %>
</head>
<body class="auth-body">
<div class="app-frame">
    <%@ include file="../common/auth-left.jsp" %>
    <main class="auth-panel auth-panel--top">
        <div class="auth-form-col">
            <% int signupStep = 1; %>
            <%@ include file="../common/signup-header.jsp" %>

            <h1 class="auth-title auth-title--tight">회원가입</h1>

            <form method="post" action="/signup/terms">
                <div class="terms-all">
                    <input class="kd-check" type="checkbox" id="agreeAll">
                    <label for="agreeAll">전체 동의</label>
                </div>

                <div class="terms-item">
                    <input class="kd-check" type="checkbox" id="agreeTerms" name="agreeTerms">
                    <label for="agreeTerms">이용약관 <span class="opt">(필수)</span></label>
                    <a class="terms-view" href="#">보기 &gt;</a>
                </div>
                <div class="terms-item">
                    <input class="kd-check" type="checkbox" id="agreePrivacy" name="agreePrivacy">
                    <label for="agreePrivacy">개인정보 처리방침 <span class="opt">(필수)</span></label>
                    <a class="terms-view" href="#">보기 &gt;</a>
                </div>
                <div class="terms-item">
                    <input class="kd-check" type="checkbox" id="agreeSensitive" name="agreeSensitive">
                    <label for="agreeSensitive">민감정보 처리 동의 <span class="opt">(필수)</span></label>
                    <a class="terms-view" href="#">보기 &gt;</a>
                </div>
                <p class="terms-note">표정·음성 데이터는 기기 안에서만 처리되고 원본은 저장되지 않아요</p>
                <div class="terms-item">
                    <input class="kd-check" type="checkbox" id="agreeMarketing" name="agreeMarketing">
                    <label for="agreeMarketing">마케팅 정보 수신 <span class="opt">(선택)</span></label>
                    <a class="terms-view" href="#">보기 &gt;</a>
                </div>

                <%-- 데모 내비게이션 — 백엔드 연동 시 type="submit"으로 복원 --%>
                <button type="button" class="kd-btn kd-btn-primary auth-cta"
                        onclick="location.href='/signup/form'">동의하고 계속하기</button>
            </form>
        </div>
        <div class="auth-footer">개인정보 처리방침 · 이용약관 · 문의하기</div>
    </main>
</div>
<script>
    // 전체 동의 체크 시 하위 항목 일괄 토글 (해제 포함)
    document.getElementById('agreeAll').addEventListener('change', function () {
        document.querySelectorAll('.terms-item .kd-check').forEach(function (c) {
            c.checked = this.checked;
        }, this);
    });
</script>
</body>
</html>
