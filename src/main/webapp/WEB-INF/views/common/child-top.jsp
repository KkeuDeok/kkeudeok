<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>

<html lang="ko"<%= "home".equals(storyStep) ? "" : " data-kd-node=\"pending\"" %>>
<head>
    <title><%= pageTitle %> | 끄덕</title>
    <%@ include file="head.jsp" %>
</head>
<body class="child-body">

<div class="story-wait" aria-hidden="true">
    <img src="/img/char-bear.png?v=5" alt="">
    <p>이야기를 만들고 있어요</p>
</div>
<div class="child-shell">
    <div class="child-scale">

        <div class="child-deco" aria-hidden="true">
            <svg class="hills" viewBox="0 0 1440 260" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
                <ellipse cx="374" cy="48" rx="70" ry="22" fill="#e3f6ec"/>
                <ellipse cx="1096" cy="40" rx="90" ry="26" fill="#e3f6ec"/>
                <path transform="translate(0,86.9)" fill="#eaf3ee" d="M 0 32.73 C 240 -7.27 480 -10.61 720 22.73 C 960 56.06 1200 52.73 1440 12.73 L 1440 173.13 L 0 173.13 Z"/>
                <path transform="translate(0,162)" fill="#e3f6ec" d="M 0 16.36 C 266.67 -3.64 540 -5.3 820 11.36 C 1100 28.03 1306.67 26.36 1440 6.36 L 1440 97.96 L 0 97.96 Z"/>
                <path transform="translate(0,221.2)" fill="#34a36a" d="M 0 18.85 C 253.33 -4.49 506.67 -6.15 760 13.85 C 1013.33 33.85 1240 32.85 1440 10.85 L 1440 38.85 L 0 38.85 Z"/>
            </svg>
            <svg class="trees" viewBox="0 0 1440 260" xmlns="http://www.w3.org/2000/svg">
                <g fill="#34a36a">
                    <rect x="175.9" y="232" width="13" height="33.8" rx="6.5" fill="#268254"/>
                    <circle cx="182.4" cy="219" r="33.8"/><circle cx="161.6" cy="237.2" r="23.4"/><circle cx="203.2" cy="237.2" r="23.4"/>
                    <rect x="567.1" y="232" width="11.4" height="29.7" rx="5.7" fill="#268254"/>
                    <circle cx="572.85" cy="220.55" r="29.75"/><circle cx="554.5" cy="236.6" r="20.6"/><circle cx="591.1" cy="236.6" r="20.6"/>
                    <ellipse cx="846" cy="252" rx="54" ry="18"/>
                    <rect x="1035.1" y="232" width="14.6" height="38" rx="7.3" fill="#268254"/>
                    <circle cx="1042.35" cy="217.35" r="37.95"/><circle cx="1019.1" cy="237.9" r="26.3"/><circle cx="1065.8" cy="237.9" r="26.3"/>
                    <rect x="1298.8" y="232" width="12.4" height="32.3" rx="6.2" fill="#268254"/>
                    <circle cx="1305" cy="219.6" r="32.3"/><circle cx="1285.15" cy="236.95" r="22.35"/><circle cx="1324.85" cy="236.95" r="22.35"/>
                </g>
            </svg>
        </div>

        <%

            boolean storyFootOff = false;

            String CHAR = "<span data-kd=\"charName\">토리</span>";
        %>
        <header class="child-head">
            <% if (!"home".equals(storyStep)) { %>
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

            <a class="child-close" href="/story/home?emo=<%= emo %>" aria-label="학습 그만두기"></a>
            <% } %>
        </header>

        <main class="child-main">
