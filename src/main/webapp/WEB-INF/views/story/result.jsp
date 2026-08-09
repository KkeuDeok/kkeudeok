<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    /* 학습6 세션 결과 — Figma 웹_학습6_세션결과(24:17081 / 412:188 화남 / 412:630 기쁨).
       6단계를 모두 마치면 나오는 칭찬 화면. 학습 화면 중 유일하게 하단 바가 없다.

       ⚠ "지우"는 아이 이름 자리다. Figma 원문 그대로 뒀고, 로그인/온보딩에서 받은
         이름으로 갈아끼워야 한다 — 백엔드 연결 시 TODO.
       ⚠ Figma 캔버스에 "3초 있다가 홈 화면으로 넘어가도록" 메모가 있다. 아동홈 화면이
         아직 없어서 자동 이동은 넣지 않았다. */
    String emo = request.getParameter("emo");
    if (!"happy".equals(emo) && !"angry".equals(emo)) emo = "sad";

    String pageTitle  = "잘했어요";
    String storyStep  = "praise";
    String storyCount = "";
%>
<%@ include file="../common/child-top.jsp" %>
<%-- 하단 바가 없는 화면이다. 변수는 child-top.jsp 가 선언해 둔다 --%>
<% storyFootOff = true; %>

<%-- 색 점 30개 — Figma 는 좌표를 하나씩 찍어 뒀지만 규칙이 있다.
     x +639.3(1235 넘으면 −1048.1) · y +356.7(1000 넘으면 −870), 크기 6·9·12·15,
     색 5가지가 돌아간다. 첫 점 (196,131)까지 Figma 값과 같다. --%>
<div class="done-confetti" aria-hidden="true">
    <%
        double cx = 196, cy = 131;
        int[] sizes = {6, 9, 12, 15};
        String[] colors = {"#ffc53d", "#58aef0", "#ff6b5e", "#a78bfa", "#2dd4bf"};
        for (int i = 0; i < 30; i++) {
            int sz = sizes[i % 4];
            String co = colors[i % 5];
    %>
    <i style="left:<%= Math.round(cx * 10) / 10.0 %>px;top:<%= Math.round((cy - 26) * 10) / 10.0 %>px;width:<%= sz %>px;height:<%= sz %>px;background:<%= co %>"></i>
    <%
            cx += 639.3; if (cx > 1235) cx -= 1048.1;
            cy += 356.7; if (cy > 1000) cy -= 870;
        }
    %>
</div>

<div class="done-art"><img src="/img/story-tori-proud.png?v=142" alt=""></div>

<h1 class="done-title">고마워, 지우야!</h1>
<p class="done-sub">마음이 따뜻해졌어</p>

<%@ include file="../common/child-bottom.jsp" %>
