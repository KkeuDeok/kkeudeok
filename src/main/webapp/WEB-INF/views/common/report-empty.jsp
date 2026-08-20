<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 성장 리포트 3탭 공통 — 학습 기록이 없을 때 본문 대신 뜨는 안내.
     보일지 말지는 kd-report.js 가 서버의 hasData 를 보고 html[data-kd-report] 로 정한다.
     ⚠ 시연용 단계 스위치(kd-hide2·kd-no-data)를 쓰지 않는다 — DB 에 기록이 있어도
       그 브라우저에서 학습한 적이 없으면 리포트가 통째로 잠겼다(2026-08-18).
     탭바는 그대로 살려 둔다 — 어떤 리포트가 생기는지는 미리 보여 주는 편이 낫다. --%>
<div class="rpt-empty">
    <div class="kd-empty">
        <span class="ic kd-empty-ic-seed"></span>
        <p class="t">성장 리포트는 첫 주 학습이 끝나면 만들어져요</p>
        <p class="d">이야기를 한 편 마치면 감정 이해 · 표현 · 사회성 지수가 계산돼요</p>
        <a class="kd-btn kd-btn-primary" href="/learn">학습 시작하기</a>
    </div>
</div>
