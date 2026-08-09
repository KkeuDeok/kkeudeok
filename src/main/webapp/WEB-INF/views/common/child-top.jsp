<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 아동 학습 흐름(스토리) 공통 껍데기(위쪽) — 스텝바 + 본문 열림.
     사용법: include 전에 스크립틀릿으로
         String pageTitle  = "이유 찾기";
         String storyStep  = "why";                 scene | feel | why | face | act | praise
         String storyCount = "오늘 이야기 1 / 3";    "" 이면 카운터를 숨긴다(상황 선택 화면처럼 자체 표기가 있는 경우)
     반드시 child-bottom.jsp 와 짝으로 쓸 것.
     보호자 앱 껍데기(app-top.jsp)와는 다른 파일이다 — 아동 화면엔 사이드바가 없다. --%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <title><%= pageTitle %> | 끄덕</title>
    <%@ include file="head.jsp" %>
</head>
<body class="child-body">
<div class="child-shell">
    <div class="child-scale">
        <%-- 스텝바 — 현재 단계 이전은 완료(체크), 이후는 예정 --%>
        <header class="child-head">
            <nav class="child-steps" aria-label="학습 단계">
                <%
                    String[][] storySteps = {
                        {"scene",  "이야기"},
                        {"feel",   "마음"},
                        {"why",    "왜?"},
                        {"face",   "표정"},
                        {"act",    "행동"},
                        {"praise", "칭찬"}
                    };
                    int storyCur = 0;
                    for (int i = 0; i < storySteps.length; i++) {
                        if (storySteps[i][0].equals(storyStep)) storyCur = i;
                    }
                    for (int i = 0; i < storySteps.length; i++) {
                        if (i > 0) {
                %>
                <i class="ln"></i>
                <%      }
                        String stCls = i < storyCur ? "st is-done" : (i == storyCur ? "st is-on" : "st");
                %>
                <span class="<%= stCls %>"<%= i == storyCur ? " aria-current=\"step\"" : "" %>><% if (i < storyCur) { %><i class="ic"></i><% } %><%= storySteps[i][1] %></span>
                <% } %>
            </nav>
        </header>

        <main class="child-main">
