<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "온보딩 - 표정 등록"; int onbStep = 5;
   String onbCol = "onb-col--w740"; String onbNext = "kdOnbFaceNext()"; %>
<%@ include file="../common/onb-top.jsp" %>
<%-- 왼쪽 버튼은 첫 표정에서 '뒤로 가기', 그 뒤로는 '다시 찍기'로 바뀐다(auth-validate.js) --%>
<% onbPrevLabel = "뒤로 가기"; onbPrevAction = "kdOnbFaceRetry()"; onbNextLabel = "다음 표정"; %>

<h1 class="onb-title">표정을 등록할게요</h1>
<p class="onb-sub">화면 안에 얼굴을 맞추고 표정을 지어 주세요</p>

<%-- ponytail: 실제 카메라 스트림·촬영·판정은 기기/백엔드 몫이다.
     여기서는 [다음 표정]을 누를 때마다 기쁨 → 슬픔 → 화남 → 놀람 → 무표정 순으로
     한 칸씩 넘어가고 진행률이 차오르는 흐름만 만들어 둔다(auth-validate.js).
     아이콘은 Figma '12 리소스_감정 아이콘' 에서 받아온 것. --%>
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
