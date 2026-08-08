<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "학습 홈"; String appNav = "learn"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 칩·표·문구는 전부 Figma 예시값이다. 백엔드가 붙으면 그대로 갈아끼운다.
     Figma 원본(307:871)은 본문이 1115 라 하단 배너가 프레임 밖으로 잘려 있다.
     웹에서는 .app-main 이 세로로 스크롤되므로 잘리지 않고 끝까지 보인다. --%>
<div class="app-head app-head-lg">
    <h1>학습 홈</h1>
    <p>오늘 학습을 시작하고 일상을 기록해요</p>
</div>

<div class="learn-hero">
    <img class="art" src="/img/learn-hero.png" alt="">
    <div class="txt">
        <h2>AI 스토리 학습</h2>
        <p>AI가 만든 상황 이야기로 감정을 배워요</p>
        <a class="kd-btn kd-btn-primary" href="#">이야기 시작하기</a>
    </div>
</div>

<div class="learn-row">
    <section class="learn-daily">
        <h2 class="learn-sec">오늘의 일상 입력</h2>
        <form action="#" method="post">
            <div class="learn-chips">
                <label><input type="radio" name="situation" value="유치원" checked><span>유치원</span></label>
                <label><input type="radio" name="situation" value="친구랑 다퉜어"><span>친구랑 다퉜어</span></label>
                <label><input type="radio" name="situation" value="가족 나들이"><span>가족 나들이</span></label>
                <label><input type="radio" name="situation" value="새로운 곳"><span>새로운 곳</span></label>
            </div>
            <textarea name="memo" placeholder="블록놀이하다가 민수랑 다퉜어요"></textarea>
            <div class="learn-save"><button type="submit" class="kd-btn kd-btn-outline">기록 저장</button></div>
        </form>
    </section>

    <section class="learn-recent">
        <h2 class="learn-sec">최근 학습 기록</h2>
        <table>
            <thead>
            <tr><th class="c-no">No</th><th class="c-date">날짜</th><th class="c-story">스토리</th><th class="c-state">상태</th></tr>
            </thead>
            <tbody>
            <tr><td class="c-no">1</td><td class="c-date">7.22</td><td class="c-story">친구가 내 블록을 무너뜨렸어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">2</td><td class="c-date">7.21</td><td class="c-story">처음 간 곳에서 길을 잃을 뻔했어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">3</td><td class="c-date">7.19</td><td class="c-story">놀이터에서 차례를 기다렸어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">4</td><td class="c-date">7.18</td><td class="c-story">동생이 내 장난감을 가져갔어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            <tr><td class="c-no">5</td><td class="c-date">7.16</td><td class="c-story">블록으로 높은 탑을 쌓았어요</td><td class="c-state"><span class="chip chip-done">완료</span></td></tr>
            </tbody>
        </table>
    </section>
</div>

<div class="learn-flow">
    <h2 class="learn-sec learn-sec-plain">스토리 학습은 이렇게 진행돼요</h2>
    <ol>
        <li><span class="no">1</span><b>상황 이야기 제시</b><span class="ds">AI가 상황을 이야기 형태로 제시</span></li>
        <li><span class="no">2</span><b>표정·동작·음성 반응 인식</b><span class="ds">아동의 반응을 분석하여 이해도 파악</span></li>
        <li><span class="no">3</span><b>분기 진행 / 코칭 피드백</b><span class="ds">반응에 따라 스토리가 분기되고 코칭 제공</span></li>
        <li><span class="no">4</span><b>세션 결과 요약</b><span class="ds">이번 학습의 결과를 정리</span></li>
        <li><span class="no">5</span><b>성장 리포트 반영</b><span class="ds">학습 결과가 성장 리포트에 반영</span></li>
    </ol>
</div>

<div class="learn-cta">
    <span class="ic"></span>
    <p>이번 주 학습을 3번 함께했어요 · 다음 이야기는 '친구와 다툰 날'이에요</p>
    <a class="kd-btn kd-btn-primary" href="#">학습 시작</a>
</div>

<%@ include file="../common/app-bottom.jsp" %>
