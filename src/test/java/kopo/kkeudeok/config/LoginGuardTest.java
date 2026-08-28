package kopo.kkeudeok.config;

import kopo.kkeudeok.controller.RoadmapController;
import kopo.kkeudeok.controller.UserController;
import kopo.kkeudeok.mapper.IChildMapper;
import kopo.kkeudeok.mapper.IStorySessionMapper;
import kopo.kkeudeok.service.IChildService;
import kopo.kkeudeok.service.IRoadmapService;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.service.impl.LearningPreparer;
import kopo.kkeudeok.util.SessionKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, RoadmapController.class})
class LoginGuardTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private IChildService childService;

    @MockitoBean
    private IChildMapper childMapper;

    @MockitoBean
    private IRoadmapService roadmapService;

    @MockitoBean
    private IStorySessionMapper sessionMapper;

    @MockitoBean
    private LearningPreparer learningPreparer;

    @ParameterizedTest
    @ValueSource(strings = {
            "/dashboard", "/learn",
            "/report", "/report/express", "/report/social",
            "/story/home", "/story/scene",
            "/onboarding/start", "/onboarding/profile", "/onboarding/done",
            "/mypage", "/mypage/account"
    })
    @DisplayName("로그인하지 않으면 어느 화면을 직접 불러도 로그인 화면으로 보낸다")
    void screensBounceToLogin(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/roadmap", "/api/roadmap/status"})
    @DisplayName("API 는 화면으로 돌리지 않고 401 을 준다 — fetch 가 JSON 을 기대한다")
    void apisAnswer401(String path) throws Exception {
        mvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.next").value("/login"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/login",
            "/signup/terms", "/signup/form", "/signup/done",
            "/find-id",
            "/find-pw", "/find-pw/new", "/find-pw/done"
    })
    @DisplayName("회원가입·아이디찾기·비밀번호찾기는 로그인 없이 들어갈 수 있어야 한다")
    void publicScreensStayOpen(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk());
    }

    @DisplayName("아이디찾기 결과는 앞 단계로 돌릴 뿐 로그인으로 보내지 않는다")
    @ParameterizedTest
    @ValueSource(strings = {"/find-id/result"})
    void findIdResultBouncesToItsOwnStep(String path) throws Exception {
        mvc.perform(get(path)).andExpect(redirectedUrl("/find-id"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/phpinfo.php", "/admin/phpinfo.php", "/test.php"})
    @DisplayName("없는 주소는 가드를 타지 않는다 — 스캐너 요청까지 로그인으로 돌리면 로그만 쌓인다")
    void unknownPathsAreNotGuarded(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isNotFound());
    }

    @DisplayName("로그인했으면 대시보드가 열린다")
    @ParameterizedTest
    @ValueSource(strings = {"/dashboard", "/learn"})
    void screensOpenWhenLoggedIn(String path) throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SS_USER_ID", "pa1234");
        session.setAttribute(SessionKeys.MEMBER_ID, 1L);

        given(userService.memberExists(1L)).willReturn(true);

        mvc.perform(get(path).session(session)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("세션이 가리키는 회원이 없으면 세션을 버리고 로그인으로 보낸다")
    void deadSessionIsThrownAway() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SS_USER_ID", "pa1234");
        session.setAttribute(SessionKeys.MEMBER_ID, 1L);

        given(userService.memberExists(1L)).willReturn(false);

        mvc.perform(get("/dashboard").session(session))
                .andExpect(redirectedUrl("/login"));

        assertThat(session.isInvalid()).as("세션을 버려야 다음 요청도 막힌다").isTrue();
    }

    @Test
    @DisplayName("회원 확인은 세션마다 한 번만 한다")
    void memberIsCheckedOncePerSession() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SS_USER_ID", "pa1234");
        session.setAttribute(SessionKeys.MEMBER_ID, 1L);

        given(userService.memberExists(1L)).willReturn(true);

        mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());
        mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());
        mvc.perform(get("/learn").session(session)).andExpect(status().isOk());

        verify(userService, times(1)).memberExists(1L);
    }

    @Test
    @DisplayName("회원 확인이 실패하면 통과시킨다")
    void dbFailureDoesNotLogOut() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SS_USER_ID", "pa1234");
        session.setAttribute(SessionKeys.MEMBER_ID, 1L);

        given(userService.memberExists(1L)).willThrow(new RuntimeException("DB down"));

        mvc.perform(get("/dashboard").session(session)).andExpect(status().isOk());
    }
}
