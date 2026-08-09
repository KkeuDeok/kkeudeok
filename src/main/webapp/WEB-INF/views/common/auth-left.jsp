<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 배경(auth-bg)과 곰(tori)은 분리된 레이어 — 배경만 갈아끼우면 곰은 유지된다 --%>
<aside class="auth-illust" aria-hidden="true">
    <%-- ⚠ PNG(2.1MB) → JPEG(323KB). 로그인 계열 9화면이 매번 이걸 통째로 받고 있었다.
         불투명한 그림이라 JPEG 로 잃는 게 없다(평균 색차 1.87). 곰(tori.png)은 투명이 필요해 PNG 유지. --%>
    <img class="bg" src="/img/auth-bg.jpg?v=148" alt="">
    <img class="bear" src="/img/tori.png" alt="">
</aside>
