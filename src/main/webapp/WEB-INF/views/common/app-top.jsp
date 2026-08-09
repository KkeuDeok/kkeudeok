<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 보호자 앱 공통 껍데기(위쪽) — 사이드바 + 본문 열림.
     사용법: include 전에 스크립틀릿으로
         String pageTitle = "메인 대시보드";
         String appNav = "dashboard";   dashboard | learn | report | mypage
     반드시 app-bottom.jsp 와 짝으로 쓸 것.
     성장 리포트·마이페이지도 이 껍데기를 그대로 쓴다. --%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title><%= pageTitle %> | 끄덕</title>
    <%@ include file="head.jsp" %>
</head>
<body class="app-body">
<div class="app-shell">
    <div class="app-scale">
        <aside class="app-side">
            <%-- 브랜드를 누르면 메인 대시보드로 (2026-08-09 요청) --%>
            <a class="app-brand" href="/dashboard">
                <span class="ava"><img src="/img/char-tori-bear.png?v=1" alt=""></span>
                <span class="nm">끄덕</span>
            </a>

            <nav class="app-nav">
                <%
                    String[][] appMenu = {
                        {"dashboard", "메인 대시보드", "/dashboard"},
                        {"learn",     "학습 홈",       "/learn"},
                        {"report",    "성장 리포트",   "/report"},
                        {"mypage",    "마이페이지",    "/mypage"}
                    };
                    for (String[] mi : appMenu) {
                %>
                <a href="<%= mi[2] %>"<%= appNav.equals(mi[0]) ? " class=\"is-on\" aria-current=\"page\"" : "" %>><%= mi[1] %></a>
                <% } %>
            </nav>

            <%-- 아이 이름·나이는 온보딩 입력값(sessionStorage.kdOnb)을
                 auth-validate.js 의 renderAppOnb() 가 data-kd 훅을 보고 채운다.
                 입력값이 없으면(직접 URL 진입·시크릿 모드) 아래 예시값이 그대로 남는다.
                 ⚠ 아바타는 아이가 고른 친구가 아니라 서비스 마스코트 곰돌이 고정(2026-08-09 요청) —
                 data-kd="charAva" 를 붙이지 말 것. --%>
            <div class="app-profile">
                <span class="ava"><img src="/img/char-tori-bear.png?v=1" alt=""></span>
                <span class="nm" data-kd="childNameAge">지우 · 6세</span>
            </div>
        </aside>

        <main class="app-main">
