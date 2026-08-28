<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%

    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    String feelPick = emo;

    String[][] feelHints = {
        {"sad",   "{c}가 살짝 알려줄게… 눈썹이 아래로 처져 있어."},
        {"angry", "{c}가 살짝 알려줄게… 눈썹이 뾰족하게 올라갔어."},
        {"happy", "{c}가 살짝 알려줄게… 입꼬리가 쭉 올라갔어."}
    };
    String hint = feelHints[0][1];
    for (String[] row : feelHints) {
        if (row[0].equals(emo)) hint = row[1];
    }

    String pageTitle  = "마음 읽기";
    String storyStep  = "feel";
    String storyCount = "오늘 이야기 1 / 3";
%>
<%@ include file="../common/child-top.jsp" %>
<%@ include file="_feel-body.jspf" %>

<p class="feel-hint"><%= hint.replace("{c}", CHAR) %></p>

<%@ include file="../common/child-bottom.jsp" %>
