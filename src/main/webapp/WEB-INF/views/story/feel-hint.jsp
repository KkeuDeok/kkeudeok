<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습2b 마음 읽기 힌트 — Figma 웹_학습2b_마음읽기_힌트(368:75).
       학습2 에서 [잘 모르겠어]를 누르면 온다. 카드 배치는 학습2 와 같고
       정답 카드에 테두리가 생기고 아래에 힌트 말풍선이 붙는다.

       ⚠ 힌트 문구는 Figma 가 슬픔 한 벌뿐이라 화남·기쁨은 임시 — 기획 확정 필요. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    String feelPick = emo;   /* 힌트 화면은 정답을 짚어 준다 */

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
