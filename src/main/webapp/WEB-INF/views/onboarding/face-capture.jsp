<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 표정 등록"; int onbStep = 5;
   String onbCol = "onb-col--w740"; String onbNext = "kdOnbFaceNext()"; %>
<%@ include file="../common/onb-top.jsp" %>
<% onbPrevLabel = "뒤로 가기"; onbPrevAction = "kdOnbFaceRetry()"; onbNextLabel = "다음 표정"; %>

<h1 class="onb-title">표정을 등록할게요</h1>
<p class="onb-sub">화면 안에 얼굴을 맞추고 표정을 지어 주세요</p>

<div class="onb-capture">
    <div class="onb-cam">
        <div class="view">
            <p class="hint">얼굴을 화면 안에 맞춰 주세요</p>
        </div>
        <div class="bar">
            <span class="track"><span class="fill" style="width:0%"></span></span>
            <span class="cnt">0 / 5 완료</span>
        </div>
    </div>

    <div class="onb-emotions">
        <div class="onb-emotion is-current"><img class="ico" src="/img/face-happy.png?v=1" alt=""><span class="nm">기쁨</span><span class="st">현재</span></div>
        <div class="onb-emotion is-wait"><img class="ico" src="/img/face-sad.png?v=1" alt=""><span class="nm">슬픔</span><span class="st">대기</span></div>
        <div class="onb-emotion is-wait"><img class="ico" src="/img/face-angry.png?v=1" alt=""><span class="nm">화남</span><span class="st">대기</span></div>
        <div class="onb-emotion is-wait"><img class="ico" src="/img/face-surprise.png?v=1" alt=""><span class="nm">놀람</span><span class="st">대기</span></div>
        <div class="onb-emotion is-wait"><img class="ico" src="/img/face-neutral.png?v=1" alt=""><span class="nm">무표정</span><span class="st">대기</span></div>
    </div>
</div>

<%@ include file="../common/onb-bottom.jsp" %>
