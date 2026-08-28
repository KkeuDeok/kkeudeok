<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
