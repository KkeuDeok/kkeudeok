<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 표정 안내"; int onbStep = 4;
   String onbCol = "onb-col--w560 onb-col--center"; String onbNext = "kdOnbAskCamera()"; %>
<%@ include file="../common/onb-top.jsp" %>
<% onbPrev = false; onbNextLabel = "카메라 켜기"; %>
<h1 class="onb-title">표정을 등록할게요</h1>
<p class="onb-sub">아이가 표정을 지으면 <span data-kd="charName">토리</span>가 알아볼 수 있어요</p>

<div class="onb-cam-icon">
    <img src="/img/icon-camera.png?v=1" alt="">
</div>

<div class="onb-notes" id="camNotes">
    <p class="onb-note"><span class="ck"></span>사진은 아이 표정을 알아보는 데만 써요</p>
    <p class="onb-note"><span class="ck"></span>원본 사진은 등록이 끝나면 바로 지워져요</p>
    <p class="onb-note"><span class="ck"></span>언제든 마이페이지에서 삭제 및 편집 할 수 있어요</p>
</div>

<div class="onb-denied" id="camDenied" hidden>
    <span class="ic" aria-hidden="true">!</span>
    <span class="tx">
        <span class="t" id="camDeniedTitle">카메라 권한이 필요해요</span>
        <span class="d" id="camDeniedDesc">주소창 왼쪽 자물쇠 아이콘에서 카메라를 허용으로 바꾼 뒤 다시 시도해 주세요.</span>
    </span>
</div>

<%@ include file="../common/onb-bottom.jsp" %>
