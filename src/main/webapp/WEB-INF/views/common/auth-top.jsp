<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- auth 계열 화면 공통 껍데기(위쪽) — 문서 시작부터 폼 열이 열리는 데까지.
     사용법: include 전에 스크립틀릿으로 String pageTitle = "로그인"; 선언
     반드시 auth-bottom.jsp 와 짝으로 쓸 것 --%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title><%= pageTitle %> | 끄덕</title>
    <%@ include file="head.jsp" %>
</head>
<body class="auth-body">
<div class="app-frame">
    <%@ include file="auth-left.jsp" %>
    <main class="auth-panel">
        <div class="auth-form-col">

