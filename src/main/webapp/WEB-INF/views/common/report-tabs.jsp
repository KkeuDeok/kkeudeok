<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 성장 리포트 탭바 5개.
     사용법: include 전에 스크립틀릿으로
         String reportTab = "understand";   understand | express | social | weekly | ai
     감정 이해가 첫 탭이라 /report 가 곧 감정 이해 화면이다. --%>
<nav class="rpt-tabs">
<%
    String[][] rptTabs = {
        {"understand", "감정 이해", "/report"},
        {"express",    "감정 표현", "/report/express"},
        {"social",     "사회성",    "/report/social"},
        {"weekly",     "주간 변화", "/report/weekly"},
        {"ai",         "AI 피드백", "/report/ai"}
    };
    for (String[] t : rptTabs) {
%>
    <a href="<%= t[2] %>"<%= reportTab.equals(t[0]) ? " class=\"is-on\" aria-current=\"page\"" : "" %>><%= t[1] %></a>
<% } %>
</nav>
