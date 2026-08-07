<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "비밀번호 변경 완료"; %>
<%@ include file="../common/auth-top.jsp" %>

<%@ include file="../common/auth-brand.jsp" %>
<div class="done-icon">
    <img src="/img/icon-check.svg" alt="">
</div>
<h1 class="done-title">비밀번호가 변경되었어요</h1>
<p class="done-sub">새 비밀번호로 로그인해 주세요</p>

<%-- 버튼 높이는 비찾 첫 화면 확인 버튼(645)과 동일 레일 — 팀장 확정 --%>
<a href="/login" class="kd-btn kd-btn-primary auth-cta" style="margin-top: 124px;">로그인 하러가기</a>

<%@ include file="../common/auth-bottom.jsp" %>
