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
                           /* TODO: '하기 싫어' 목적지는 기획 미정. 지금은 다음 단계로 건너뛴다 */
                           String skipTo = "face".equals(storyStep) ? "/story/situation" : "/story/result";
                    %>
                    <a class="kd-skip" href="<%= skipTo %>?emo=<%= emo %>">이 <%= "face".equals(storyStep) ? "표정" : "동작" %>은 하기 싫어</a>
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
                    <%-- TODO: 종료 확인 팝업(Figma 237:402 안에 겹쳐 그려져 있음) 미구현 --%>
                    <button type="button" class="kd-later">다음에 할래</button>
                    <% if (!storyCount.isEmpty()) { %>
                    <p class="child-count"><%= storyCount %></p>
                    <% } %>
                </div>
            </div>
<% } %>
        </main>
    </div>
</div>
</body>
</html>
