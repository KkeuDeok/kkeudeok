<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<nav class="mp-tabs">
<%
    String[][] mpTabs = {
        {"account",   "보호자 정보", "/mypage/account"},
        {"child",     "아동 프로필", "/mypage/child"},
        {"character", "캐릭터 관리", "/mypage/character"}
    };
    for (String[] t : mpTabs) {
%>
    <a href="<%= t[2] %>"<%= mpTab.equals(t[0]) ? " class=\"is-on\" aria-current=\"page\"" : "" %>><%= t[1] %></a>
<% } %>
</nav>

<div class="mp-toast" id="mpToast" role="status" aria-live="polite"></div>
<script>
    function kdSaved(msg) {
        var t = document.getElementById('mpToast');
        t.textContent = msg || '저장했어요';
        t.classList.add('is-on');
        clearTimeout(window.__kdToast);
        window.__kdToast = setTimeout(function () { t.classList.remove('is-on'); }, 1800);
    }
</script>
