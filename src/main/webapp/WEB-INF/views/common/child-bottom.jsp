<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% if (!storyFootOff) {
       boolean camStep = "face".equals(storyStep) || "act".equals(storyStep);
%>
            <div class="child-foot">
                <div class="side">
                    <% if (camStep) {
                           boolean isFace = "face".equals(storyStep);
                    %>
                    <% if (isFace) { %>
                    <a class="kd-skip" href="/story/situation?emo=<%= emo %>">이 표정은 하기 싫어</a>
                    <% } else { %>
                    <button type="button" class="kd-skip" data-ask>이 동작은 하기 싫어</button>
                    <% } %>
                    <% } else { %>
                    <button type="button" class="kd-sub kd-sub-listen">다시 들려줘</button>
                    <% if ("feel".equals(storyStep)) { %>
                    <a class="kd-sub kd-sub-hint" href="/story/feel-hint?emo=<%= emo %>">잘 모르겠어</a>
                    <% } else { %>
                    <button type="button" class="kd-sub kd-sub-hint">잘 모르겠어</button>
                    <% } %>
                    <% } %>
                </div>
                <div class="side">
                    <button type="button" class="kd-later" data-ask>다음에 할래</button>
                    <% if (!storyCount.isEmpty()) { %>
                    <p class="child-count"><%= storyCount %></p>
                    <% } %>
                </div>
            </div>

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

            <div class="child-toast" hidden role="status" aria-live="polite"></div>
        </main>
    </div>
</div>
<script src="/js/story.js?v=302"></script>
<script src="/js/story-session.js?v=302"></script>
<script src="/js/kd-mediapipe.js?v=299"></script>
</body>
</html>
