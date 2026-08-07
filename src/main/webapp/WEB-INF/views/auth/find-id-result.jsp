<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "아이디 찾기"; %>
<%@ include file="../common/auth-top.jsp" %>

<%@ include file="../common/auth-brand.jsp" %>
<div class="done-icon">
    <img src="/img/icon-check.svg" alt="">
</div>
<h1 class="done-title" style="margin-bottom: 0;">아이디를 찾았어요</h1>

<div class="done-id-card">pa****</div>

<a href="/find-pw" class="kd-btn kd-btn-outline auth-cta" style="margin-top: 24px;">비밀번호 찾기</a>
<a href="/login" class="kd-btn kd-btn-primary auth-cta" style="margin-top: 13px;">로그인하기</a>

<%@ include file="../common/auth-bottom.jsp" %>
