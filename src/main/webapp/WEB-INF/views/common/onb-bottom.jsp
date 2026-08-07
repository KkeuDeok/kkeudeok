<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- 온보딩 공통 껍데기(아래쪽) — onb-top.jsp 가 연 태그를 닫는다.
     버튼 레일은 본문 열 바깥에 있어야 Y=845 에 고정된다(내용 길이 영향 없음).
     사용법: include 전에 스크립틀릿으로
         String onbNext = "kdSubmitOnbProfile()";   (필수)
         boolean onbPrev = false;                   (선택 — 선언 안 하면 true)
     onbPrev = false 면 [다음] 하나가 520 폭을 다 쓴다. PIN 설정처럼 뒤로 갈 곳이
     로그인밖에 없는 화면에 쓴다(뒤로 가면 로그인으로 튕겨 흐름이 깨진다). --%>
        </div>

        <div class="onb-actions">
            <% if (onbPrev) { %>
            <button type="button" class="kd-btn kd-btn-outline" onclick="<%= onbPrevAction %>"><%= onbPrevLabel %></button>
            <% } %>
            <button type="button" class="kd-btn kd-btn-primary<%= onbPrev ? "" : " onb-btn-wide" %>"
                    onclick="<%= onbNext %>"><%= onbNextLabel %></button>
        </div>
    </div>
</div>
</body>
</html>
