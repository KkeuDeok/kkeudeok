<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String pageTitle = "성장 리포트"; String appNav = "report"; String reportTab = "weekly"; %>
<%@ include file="../common/app-top.jsp" %>

<%-- ponytail: 수치는 전부 예시값.
     8주 시퀀스는 끝값·최근 증감·누적이 다른 화면과 전부 맞아떨어지게 설계돼 있다:
     이해 52→72(+20, 최근 +2) / 표현 48→64(+16, +4) / 사회성 50→58(+8, -3) --%>
<div class="app-head rpt-head">
    <h1>지우의 성장 리포트</h1>
    <p>관찰 8주차 · 매주 월요일에 갱신돼요</p>
</div>

<div class="rpt-tabsrow">
    <%@ include file="../common/report-tabs.jsp" %>
</div>

<div class="rpt-score">
    <div>
        <p class="lb">관찰된 주</p>
        <p class="v">8<span class="u">주차</span></p>
    </div>
</div>

<section class="rpt-sec">
    <h2>감정별 주간 히트맵</h2>
    <%-- 색만으로는 정도를 못 읽는다는 지적(2026-08-09 팀장)이 있어 칸마다 횟수를 적는다.
         진하기 5단계 --o = .12/.26/.44/.62/.80 이 각각 1·2·4·6·8회.
         ⚠ opacity 로 진하기를 주면 숫자까지 같이 흐려진다 → report.css 에서 흰색과 섞는다. --%>
    <div class="rpt-heat">
        <span class="rowlb">기쁨</span>
        <div class="cell em-joy" style="--o:.44">4</div><div class="cell em-joy" style="--o:.44">4</div><div class="cell em-joy" style="--o:.62">6</div><div class="cell em-joy" style="--o:.62">6</div><div class="cell em-joy" style="--o:.62">6</div><div class="cell em-joy" style="--o:.80">8</div><div class="cell em-joy" style="--o:.80">8</div><div class="cell em-joy" style="--o:.80">8</div>
        <div class="rpt-heat-scale">
            <span>많음</span>
            <i style="opacity:.80"></i><i style="opacity:.62"></i><i style="opacity:.44"></i><i style="opacity:.26"></i><i style="opacity:.12"></i>
            <span>적음</span>
        </div>
        <span class="rowlb">슬픔</span>
        <div class="cell em-sad" style="--o:.26">2</div><div class="cell em-sad" style="--o:.26">2</div><div class="cell em-sad" style="--o:.44">4</div><div class="cell em-sad" style="--o:.44">4</div><div class="cell em-sad" style="--o:.62">6</div><div class="cell em-sad" style="--o:.62">6</div><div class="cell em-sad" style="--o:.62">6</div><div class="cell em-sad" style="--o:.80">8</div>
        <span class="rowlb">화남</span>
        <div class="cell em-mad" style="--o:.62">6</div><div class="cell em-mad" style="--o:.62">6</div><div class="cell em-mad" style="--o:.44">4</div><div class="cell em-mad" style="--o:.44">4</div><div class="cell em-mad" style="--o:.44">4</div><div class="cell em-mad" style="--o:.26">2</div><div class="cell em-mad" style="--o:.26">2</div><div class="cell em-mad" style="--o:.26">2</div>
        <span class="rowlb">놀람</span>
        <div class="cell em-sup" style="--o:.12">1</div><div class="cell em-sup" style="--o:.26">2</div><div class="cell em-sup" style="--o:.26">2</div><div class="cell em-sup" style="--o:.44">4</div><div class="cell em-sup" style="--o:.44">4</div><div class="cell em-sup" style="--o:.44">4</div><div class="cell em-sup" style="--o:.62">6</div><div class="cell em-sup" style="--o:.62">6</div>
        <span></span>
        <span class="weeklb">1주</span><span class="weeklb">2주</span><span class="weeklb">3주</span><span class="weeklb">4주</span><span class="weeklb">5주</span><span class="weeklb">6주</span><span class="weeklb">7주</span><span class="weeklb">8주</span>
    </div>
    <p class="rpt-heat-note">숫자는 그 주에 아이가 그 감정을 표현한 횟수예요</p>
</section>

<hr class="rpt-rule">

<section class="rpt-sec">
    <div class="rpt-cols">
        <div class="col-chart">
            <h2>지표별 8주 추이</h2>
            <%--
              세로축은 대시보드와 같은 45~75 구간 (격자선 75/65/55/45).
              y = 40.5 + (72 - v) × 6.857, 가로 8칸 x = 22 + i×74
            --%>
            <svg width="640" height="270" viewBox="0 0 640 270" role="img"
                 aria-label="지표별 8주 추이 — 감정 이해 72, 감정 표현 64, 사회성 58">
                <g stroke="#eef2ef" stroke-width="1">
                    <line x1="22" y1="19.97" x2="540" y2="19.97"/>
                    <line x1="22" y1="88.54" x2="540" y2="88.54"/>
                    <line x1="22" y1="157.11" x2="540" y2="157.11"/>
                    <line x1="22" y1="225.68" x2="540" y2="225.68"/>
                </g>
                <g font-size="12" fill="#8b978f" text-anchor="middle" font-family="Noto Sans KR, sans-serif">
                    <text x="22" y="252">1주</text><text x="96" y="252">2주</text><text x="170" y="252">3주</text>
                    <text x="244" y="252">4주</text><text x="318" y="252">5주</text><text x="392" y="252">6주</text>
                    <text x="466" y="252">7주</text><text x="540" y="252">8주</text>
                </g>
                <g fill="none" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline stroke="#e8a317" points="22,177.6 96,150.2 170,129.6 244,109.1 318,95.4 392,74.8 466,54.2 540,40.5"/>
                    <polyline stroke="#3d8fe0" points="22,205.1 96,191.4 170,170.8 244,157.1 318,143.4 392,136.5 466,122.8 540,95.4"/>
                    <polyline stroke="#0fa394" points="22,191.4 96,177.6 170,163.9 244,150.2 318,129.6 392,122.8 466,115.9 540,136.5"/>
                </g>
                <g>
                    <circle cx="540" cy="40.5" r="4.7" fill="#e8a317"/>
                    <circle cx="540" cy="95.4" r="4.7" fill="#3d8fe0"/>
                    <circle cx="540" cy="136.5" r="4.7" fill="#0fa394"/>
                </g>
                <g font-size="12" font-family="Noto Sans KR, sans-serif">
                    <text x="551" y="44" fill="#e8a317">감정 이해 72</text>
                    <text x="551" y="99" fill="#3d8fe0">감정 표현 64</text>
                    <text x="551" y="140" fill="#0fa394">사회성 58</text>
                </g>
            </svg>
        </div>
        <div class="col-list">
            <h2>가장 크게 달라진 것</h2>
            <div class="rpt-diff">
                <div class="rpt-diff-item">
                    <span class="n">+20</span>
                    <span><b>감정 이해</b><span class="ds">슬픔·놀람 표정 구분이 또렷해졌어요</span></span>
                </div>
                <div class="rpt-diff-item">
                    <span class="n">+16</span>
                    <span><b>감정 표현</b><span class="ds">몸짓 대신 말로 표현하는 순간이 늘었어요</span></span>
                </div>
                <div class="rpt-diff-item">
                    <span class="n">+8</span>
                    <span><b>사회성</b><span class="ds">차례 지키기가 한결 편안해졌어요</span></span>
                </div>
            </div>
        </div>
    </div>
</section>

<%@ include file="../common/app-bottom.jsp" %>
