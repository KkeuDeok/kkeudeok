<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 발달 체크 카드 한 장(문항 3개).
     include 전에 선언할 것:
         String ckTitle   = "감정 이해";
         String ckCount   = "checkCount";   완료 카운터를 붙일 id (없으면 "")
         String[] ckNames = {"q1","q2","q3"};
         String[] ckTexts = {"...","...","..."};
     척도는 7단계 양극(왼쪽 그렇다 ↔ 오른쪽 그렇지 않다), 값 1~7. --%>
<div class="onb-check">
    <h2><%= ckTitle %></h2>
    <% if (!ckCount.isEmpty()) { %><span class="done-count" id="<%= ckCount %>"></span><% } %>

    <% for (int i = 0; i < ckNames.length; i++) { %>
    <div class="onb-q">
        <span class="txt"><%= ckTexts[i] %></span>
        <span class="yes">그렇다</span>
        <span class="dots"><%
            for (int v = 1; v <= 7; v++) {
        %><input type="radio" name="<%= ckNames[i] %>" value="<%= v %>" class="d<%= v %>"
                 aria-label="<%= ckTexts[i] %> — <%= v %>단계"><%
            }
        %></span>
        <span class="no">그렇지 않다</span>
    </div>
    <% } %>
</div>
