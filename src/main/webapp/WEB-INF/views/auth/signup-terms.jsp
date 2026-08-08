<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "회원가입 - 약관동의"; %>
<%@ include file="../common/auth-top.jsp" %>

<%@ include file="../common/signup-header.jsp" %>

<h1 class="auth-title">회원가입</h1>

<%-- auth-center: 콘텐츠가 짧아 남는 공간을 제목 아래·버튼 위로 반씩 나눈다 (auth.css 참고) --%>
<form method="post" action="/signup/terms" class="auth-center">
    <%-- 전체 동의 토글은 auth-validate.js 가 처리 --%>
    <div class="terms-all">
        <input class="kd-check" type="checkbox" id="agreeAll">
        <label for="agreeAll">전체 동의</label>
    </div>

    <div class="terms-item">
        <input class="kd-check" type="checkbox" id="agreeTerms" name="agreeTerms">
        <label for="agreeTerms">이용약관 <span class="opt">(필수)</span></label>
        <a class="terms-view" href="#" onclick="dlgTerms.showModal(); return false">보기 &gt;</a>
    </div>
    <div class="terms-item">
        <input class="kd-check" type="checkbox" id="agreePrivacy" name="agreePrivacy">
        <label for="agreePrivacy">개인정보 처리방침 <span class="opt">(필수)</span></label>
        <a class="terms-view" href="#" onclick="dlgPrivacy.showModal(); return false">보기 &gt;</a>
    </div>
    <div class="terms-item">
        <input class="kd-check" type="checkbox" id="agreeSensitive" name="agreeSensitive">
        <label for="agreeSensitive">민감정보 처리 동의 <span class="opt">(필수)</span></label>
        <a class="terms-view" href="#" onclick="dlgSensitive.showModal(); return false">보기 &gt;</a>
    </div>
    <p class="terms-note">표정·음성 데이터는 기기 안에서만 처리되고 원본은 저장되지 않아요</p>
    <div class="terms-item">
        <input class="kd-check" type="checkbox" id="agreeMarketing" name="agreeMarketing">
        <label for="agreeMarketing">마케팅 정보 수신 <span class="opt">(선택)</span></label>
        <a class="terms-view" href="#" onclick="dlgMarketing.showModal(); return false">보기 &gt;</a>
    </div>

    <p class="terms-error" id="termsError"></p>

    <%-- 데모 검증 — 백엔드 연동 시 type="submit"으로 복원 --%>
    <button type="button" class="kd-btn kd-btn-primary auth-cta"
            onclick="kdSubmitTerms()">동의하고 계속하기</button>
</form>

<%-- 약관 본문 — 브라우저 기본 <dialog>라 JS·라이브러리 없이 뜬다.
     ponytail: 아래 문구는 골자만 적은 임시본. 법무 확정본 받으면 <p> 내용만 교체할 것 --%>
<dialog class="terms-dialog" id="dlgTerms">
    <h2>이용약관</h2>
    <p>제1조(목적) 이 약관은 끄덕(이하 "서비스")의 이용 조건과 절차, 이용자와 서비스의 권리·의무를 정합니다.</p>
    <p>제2조(이용자) 서비스는 보호자 계정으로 가입하며, 아동 정보는 보호자가 등록·관리합니다.</p>
    <p>제3조(서비스 내용) 감정 학습 콘텐츠, 학습 기록 조회, 성장 리포트 제공을 포함합니다.</p>
    <p>제4조(금지행위) 타인 계정 도용, 서비스 역이용, 콘텐츠 무단 복제를 금지합니다.</p>
    <p>제5조(해지) 이용자는 언제든지 마이페이지에서 탈퇴할 수 있으며, 탈퇴 시 아동 정보도 함께 삭제됩니다.</p>
    <form method="dialog"><button>확인</button></form>
</dialog>

<dialog class="terms-dialog" id="dlgPrivacy">
    <h2>개인정보 처리방침</h2>
    <p><b>수집 항목</b> — 보호자: 이름, 아이디, 비밀번호, 이메일 / 아동: 이름(애칭), 생년월일, 성별</p>
    <p><b>수집 목적</b> — 계정 식별, 아동별 학습 기록 제공, 성장 리포트 생성</p>
    <p><b>보유 기간</b> — 회원 탈퇴 시까지. 탈퇴 후에는 지체 없이 파기합니다.</p>
    <p><b>제3자 제공</b> — 제공하지 않습니다. 법령에 근거한 요청이 있는 경우에만 예외로 합니다.</p>
    <p>동의를 거부할 수 있으나, 필수 항목에 동의하지 않으면 서비스 이용이 제한됩니다.</p>
    <form method="dialog"><button>확인</button></form>
</dialog>

<dialog class="terms-dialog" id="dlgSensitive">
    <h2>민감정보 처리 동의</h2>
    <p><b>대상</b> — 아동의 표정(카메라)·음성(마이크) 데이터, 발달 체크리스트 응답</p>
    <p><b>처리 방식</b> — 표정·음성은 <b>기기 안에서만</b> 분석되며 원본은 서버로 전송·저장되지 않습니다. 분석 결과(감정 분류 값)만 저장됩니다.</p>
    <p><b>이용 목적</b> — 감정 인식 학습 피드백, 개인화된 학습 로드맵 추천</p>
    <p><b>보유 기간</b> — 회원 탈퇴 또는 아동 프로필 삭제 시까지</p>
    <p>만 14세 미만 아동의 정보이므로 보호자 동의가 필요하며, 동의는 언제든 철회할 수 있습니다.</p>
    <form method="dialog"><button>확인</button></form>
</dialog>

<dialog class="terms-dialog" id="dlgMarketing">
    <h2>마케팅 정보 수신 (선택)</h2>
    <p>신규 학습 콘텐츠, 업데이트 소식, 이벤트 안내를 이메일로 보내드립니다.</p>
    <p>동의하지 않아도 서비스 이용에는 아무런 제한이 없습니다.</p>
    <p>수신 설정은 마이페이지에서 언제든 변경할 수 있습니다.</p>
    <form method="dialog"><button>확인</button></form>
</dialog>

<%@ include file="../common/auth-bottom.jsp" %>
