<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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

            <div class="app-profile">
                <span class="ava"><img src="/img/char-tori-bear.png?v=1" alt=""></span>
                <span class="nm" data-kd="childNameAge">지우 · 6세</span>
            </div>
        </aside>
        <main class="app-main">
