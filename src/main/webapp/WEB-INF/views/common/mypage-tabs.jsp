<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 마이페이지 탭바 3개.
     사용법: include 전에 스크립틀릿으로
         String mpTab = "account";   account | child | character
     게이트(/mypage)는 탭바가 없다. --%>
<nav class="mp-tabs">
<%
    String[][] mpTabs = {
        {"account",   "회원정보",    "/mypage/account"},
        {"child",     "아동 프로필", "/mypage/child"},
        {"character", "캐릭터 관리", "/mypage/character"}
    };
    for (String[] t : mpTabs) {
%>
    <a href="<%= t[2] %>"<%= mpTab.equals(t[0]) ? " class=\"is-on\" aria-current=\"page\"" : "" %>><%= t[1] %></a>
<% } %>
</nav>
