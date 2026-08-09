<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 아동 학습 흐름 공통 껍데기(아래쪽) — child-top.jsp 가 연 태그를 닫는다.
     하단 바는 학습 화면 8개가 공통으로 쓴다(상황 선택·세션 결과에는 없다 — 그때 끄는 값 추가할 것). --%>
            <div class="child-foot">
                <div class="side">
                    <%-- TODO: 이야기 음성 재생·힌트는 백엔드/TTS 붙을 때 동작을 넣는다 --%>
                    <button type="button" class="kd-sub kd-sub-listen">다시 들려줘</button>
                    <button type="button" class="kd-sub kd-sub-hint">잘 모르겠어</button>
                </div>
                <div class="side">
                    <%-- TODO: 종료 확인 팝업(Figma 237:402 안에 겹쳐 그려져 있음) 미구현 --%>
                    <button type="button" class="kd-later">다음에 할래</button>
                    <% if (!storyCount.isEmpty()) { %>
                    <p class="child-count"><%= storyCount %></p>
                    <% } %>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
