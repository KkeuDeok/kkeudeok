<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 성장 리포트 5탭 공통 — 관찰 데이터가 0일 때 본문 대신 뜨는 안내.
     본문(.rpt-score/.rpt-sec/...)은 report.css 의 단계 규칙(0·1)이 통째로 숨긴다.
     탭바는 그대로 살려 둔다 — 어떤 리포트가 생기는지는 미리 보여 주는 편이 낫다. --%>
<div class="rpt-empty kd-hide2">
    <div class="kd-empty">
        <span class="ic kd-empty-ic-seed"></span>
        <p class="t">성장 리포트는 첫 주 학습이 끝나면 만들어져요</p>
        <p class="d kd-no-data">이번 주 0 / 3회 · 세 번을 채우면 감정 이해 · 표현 · 사회성 지수가 처음 계산돼요</p>
        <p class="d kd-s1">이번 주 <b>1 / 3회</b> · 두 번만 더 하면 첫 리포트가 만들어져요</p>
        <a class="kd-btn kd-btn-primary" href="/learn">학습 시작하기</a>
    </div>
</div>
