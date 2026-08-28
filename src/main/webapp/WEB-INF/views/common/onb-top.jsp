<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% boolean onbPrev = true; String onbPrevLabel = "이전"; String onbPrevAction = "history.back()";
   String onbNextLabel = "다음"; %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title><%= pageTitle %> | 끄덕</title>
    <%@ include file="head.jsp" %>
</head>
<body class="onb-body">
<div class="onb-frame">
    <div class="onb-scale">
        <% if (onbStep >= 1) { %>
        <header class="onb-header">
            <%@ include file="auth-brand.jsp" %>
        </header>

        <nav class="onb-steps" aria-label="온보딩 진행 단계">
            <%
                String[] onbLabels = {"아이 정보", "캐릭터", "발달 체크", "표정 안내", "표정 등록"};
                for (int i = 0; i < onbLabels.length; i++) {
                    int no = i + 1;
                    String state = no < onbStep ? "is-done" : (no == onbStep ? "is-current" : "");
            %>
            <div class="onb-step <%= state %>"<%= no == onbStep ? " aria-current=\"step\"" : "" %>>
                <span class="dot"><%= no %></span>
                <span class="lb"><%= onbLabels[i] %></span>
                <% if (no < onbLabels.length) { %><span class="line"></span><% } %>
            </div>
            <% } %>
        </nav>
        <% } %>

        <div class="onb-col<%= onbStep == 0 ? " onb-col--bare" : "" %> <%= onbCol %>">
