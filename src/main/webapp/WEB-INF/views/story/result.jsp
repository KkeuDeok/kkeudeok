<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%

    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo) && !"surprise".equals(emo)) emo = "sad";

    String pageTitle  = "잘했어요";
    String storyStep  = "praise";
    String storyCount = "";
%>
<%@ include file="../common/child-top.jsp" %>

<% storyFootOff = true; %>

<div class="done-confetti" aria-hidden="true">
    <%
        double cx = 196, cy = 131;
        int[] sizes = {6, 9, 12, 15};
        String[] colors = {"#ffc53d", "#58aef0", "#ff6b5e", "#a78bfa", "#2dd4bf"};
        for (int i = 0; i < 30; i++) {
            int sz = sizes[i % 4];
            String co = colors[i % 5];
    %>
    <i style="left:<%= Math.round(cx * 10) / 10.0 %>px;top:<%= Math.round((cy - 26) * 10) / 10.0 %>px;width:<%= sz %>px;height:<%= sz %>px;background:<%= co %>;--fall:<%= 140 + (i * 37) % 160 %>px;--spin:<%= 240 + (i * 53) % 420 %>deg;--dur:<%= 26 + (i * 7) % 18 %>00ms;--delay:<%= (i * 11) % 20 %>00ms"></i>
    <%
            cx += 639.3; if (cx > 1235) cx -= 1048.1;
            cy += 356.7; if (cy > 1000) cy -= 870;
        }
    %>
</div>

<div class="done-stage" data-home="/dashboard" role="button" tabindex="0" aria-label="오늘 학습 마치기">
    <div class="done-art"><img data-kd-char="proud" src="/img/char-tori-proud.png" alt=""></div>

    <h1 class="done-title">고마워, <span data-kd="childVocative">지우야</span>!</h1>
    <p class="done-tap">화면을 누르면 오늘 학습을 마쳐요</p>
</div>

<%@ include file="../common/child-bottom.jsp" %>
