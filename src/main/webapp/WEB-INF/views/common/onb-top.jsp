<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 온보딩(보호자) 공통 껍데기(위쪽) — 브랜드 바 + 6단계 스텝바까지.
     사용법: include 전에 스크립틀릿으로
         String pageTitle = "아이 정보";
         int onbStep = 1;          0 = 스텝바 없음(PIN) · 1~6 = 진행 · 7 = 전부 완료
         String onbCol = "";       본문 열 폭 변형 (onb-col--w560 / --w740 / --w800 / --center)
     반드시 onb-bottom.jsp 와 짝으로 쓸 것.
     include 다음 줄에서 onbPrev(=false 면 [이전] 없음)·onbNextLabel 을 바꿀 수 있다. --%>
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
                String[] onbLabels = {"아이 정보", "캐릭터", "발달 체크", "표정 안내", "표정 등록", "로드맵"};
                for (int i = 0; i < onbLabels.length; i++) {
                    int no = i + 1;
                    /* onbStep = 7 이면 여섯 단계가 모두 완료 상태가 된다 (완료 화면) */
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
