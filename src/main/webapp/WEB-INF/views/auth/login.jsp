<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "로그인"; %>
<%@ include file="../common/auth-top.jsp" %>

<%-- 첫 진입 스플래시 — 곰이 고개를 두 번 끄덕이고 워드마크가 올라온 뒤 스스로 걷힌다.
     라우트를 새로 파지 않아 서버 재시작이 필요 없다(대시보드 모달과 같은 이유).
     서버가 그려 보내므로 첫 프레임부터 덮인 상태다 — 깜빡임(FOUC)이 없다.
     ⚠ 로그인 화면에만 둔다. auth-top.jsp 에 넣으면 아이디찾기·회원가입 9화면에 다 뜬다. --%>
<div class="kd-splash" aria-hidden="true">
    <div class="kd-splash-logo">
        <img src="/img/char-bear.png?v=5" alt="">
        <span>끄덕</span>
    </div>
</div>
<script>
    /* 비밀번호 찾기 갔다 돌아올 때 또 뜨면 성가시다 — 세션에 한 번만.
       JS 가 꺼져 있으면 그냥 매번 재생된다(화면은 멀쩡하다). */
    if (sessionStorage.getItem('kdSplash')) {
        document.querySelector('.kd-splash').remove();
    } else {
        sessionStorage.setItem('kdSplash', '1');
    }
</script>

<%@ include file="../common/auth-brand.jsp" %>
<h1 class="auth-title">로그인</h1>

<form method="post" action="/login">
    <div class="kd-field">
        <label class="kd-label" for="loginId">아이디</label>
        <input class="kd-input" type="text" id="loginId" name="loginId"
               placeholder="아이디를 입력하세요" autocomplete="username">
    </div>
    <div class="kd-field">
        <label class="kd-label" for="password">비밀번호</label>
        <input class="kd-input" type="password" id="password" name="password"
               placeholder="비밀번호를 입력하세요" autocomplete="current-password">
    </div>

    <div class="auth-remember">
        <input class="kd-check" type="checkbox" id="rememberMe" name="rememberMe">
        <label for="rememberMe">로그인 상태 유지</label>
    </div>

    <%-- 데모 검증 — 백엔드 연동 시 type="submit"으로 복원 --%>
    <button type="button" class="kd-btn kd-btn-primary auth-cta"
            onclick="kdSubmitLogin()">로그인</button>
</form>

<div class="auth-links">
    <a href="/find-id">아이디 찾기</a><span>·</span><a href="/find-pw">비밀번호 찾기</a><span>·</span><a href="/signup/terms">회원가입</a>
</div>

<%@ include file="../common/auth-bottom.jsp" %>
