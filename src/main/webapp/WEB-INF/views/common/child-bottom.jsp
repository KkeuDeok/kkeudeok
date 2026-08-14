<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 아동 학습 흐름 공통 껍데기(아래쪽) — child-top.jsp 가 연 태그를 닫는다.
     하단 바는 학습 화면 대부분이 공통으로 쓴다. 왼쪽만 화면군에 따라 갈린다:
       기본(이야기·마음·왜?)  [다시 들려줘][잘 모르겠어]
       표정·행동             [이 표정은 하기 싫어] 글자 링크 하나
       세션 결과·상황 선택     하단 바 자체가 없다 → storyFootOff = true
     각 화면이 `emo` 를 선언해 두는 것이 규약이다(링크에 실어 보낸다). --%>
<% if (!storyFootOff) {
       boolean camStep = "face".equals(storyStep) || "act".equals(storyStep);
%>
            <div class="child-foot">
                <div class="side">
                    <% if (camStep) {
                           /* 표정은 아직 흐름 중간이라 다음 단계(상황 선택)로 건너뛴다.
                              행동은 마지막 단계다 — 예전엔 칭찬으로 직행해 **안 했는데 칭찬을 받았다**.
                              (2026-08-10 사용자 확정) 확인을 물어보고 학습을 끝낸다. */
                           boolean isFace = "face".equals(storyStep);
                    %>
                    <% if (isFace) { %>
                    <a class="kd-skip" href="/story/situation?emo=<%= emo %>">이 표정은 하기 싫어</a>
                    <% } else { %>
                    <button type="button" class="kd-skip" data-ask>이 동작은 하기 싫어</button>
                    <% } %>
                    <% } else { %>
                    <%-- TODO: 이야기 음성 재생은 백엔드/TTS 붙을 때 동작을 넣는다 --%>
                    <button type="button" class="kd-sub kd-sub-listen">다시 들려줘</button>
                    <%-- 마음 읽기에만 힌트 화면(학습2b)이 있다. 나머지 단계는 아직 갈 곳이 없어
                         버튼 그대로 둔다. --%>
                    <% if ("feel".equals(storyStep)) { %>
                    <a class="kd-sub kd-sub-hint" href="/story/feel-hint?emo=<%= emo %>">잘 모르겠어</a>
                    <% } else { %>
                    <button type="button" class="kd-sub kd-sub-hint">잘 모르겠어</button>
                    <% } %>
                    <% } %>
                </div>
                <div class="side">
                    <%-- 누르면 확인 팝업(.story-quit) 을 연다 — 아이가 실수로 눌러 학습이
                         통째로 끝나면 안 된다. 예전엔 핸들러가 없어 아무 반응이 없었다. --%>
                    <button type="button" class="kd-later" data-ask>다음에 할래</button>
                    <% if (!storyCount.isEmpty()) { %>
                    <p class="child-count"><%= storyCount %></p>
                    <% } %>
                </div>
            </div>

            <%-- 학습을 끝낼지 물어보는 공통 팝업. [다음에 할래]·[이 동작은 하기 싫어] 가 연다.
                 기본은 '더 할래' 에 초점이 가게 두 버튼 순서를 잡았다 — 실수로 끝나면 안 된다. --%>
            <div class="story-quit" hidden role="dialog" aria-modal="true" aria-labelledby="askTitle">
                <div class="box">
                    <p class="t" id="askTitle">오늘은 여기까지 할까?</p>
                    <p class="d">지금까지 한 건 남아 있어요. 다음에 이어서 하면 돼요.</p>
                    <div class="ft">
                        <button type="button" class="no" data-ask-no>더 할래</button>
                        <a class="yes" href="/dashboard">그만할래</a>
                    </div>
                </div>
            </div>
<% } %>

            <%-- 표정·동작이 어긋났을 때 그 자리에서 알려 주는 띠 (2026-08-10 피드백 4).
                 화면을 바꾸지 않는다 — 아이 입장에선 페이지가 넘어가면 '새로고침'으로 느껴진다.
                 ponytail: 마이페이지에도 같은 모양(.mp-toast)이 있지만 그건 탭바 include 에
                 묶여 있어 아동 화면이 못 쓴다. 껍데기가 갈리는 동안은 이대로 두고,
                 공통 조각으로 뺄 일이 생기면 그때 합칠 것. --%>
            <div class="child-toast" hidden role="status" aria-live="polite"></div>
        </main>
    </div>
</div>
<%-- 낭독 mp3 대응표. story.js 보다 먼저 와야 한다 — window.KD_AUDIO 를 읽고 시작한다 --%>
<script src="/js/audio-map.js?v=222"></script>
<script src="/js/story.js?v=222"></script>
</body>
</html>
