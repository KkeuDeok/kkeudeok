package kopo.kkeudeok.controller;

import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.mapper.IChildMapper;
import kopo.kkeudeok.service.IMailService;
import kopo.kkeudeok.service.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProcController.class)
class UserProcControllerTest {
    private static final String EMAIL = "test.parent9@kkeudeok.local";
    private static final String SS_AUTH_CODE = "SS_AUTH_CODE";
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private IMailService mailService;

    @MockitoBean
    private IChildMapper childMapper;

    private UserDTO exists(String yn) {
        UserDTO dto = new UserDTO();
        dto.setExistsYn(yn);
        return dto;
    }

    private MockHttpSession sessionWithCode() throws Exception {
        given(userService.getEmailExists(any())).willReturn(exists("N"));

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/sendAuthCodeProc")
                        .param("email", EMAIL)
                        .param("kind", "signup")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(1));

        assertThat(session.getAttribute(SS_AUTH_CODE)).as("발송하면 세션에 정답이 있어야 한다").isNotNull();
        return session;
    }

    @Test
    @DisplayName("아이디가 겹쳐 가입이 막혀도 인증번호는 살아 있다 — 아이디만 고쳐 다시 누르면 통과")
    void duplicateLoginIdDoesNotBurnAuthCode() throws Exception {
        MockHttpSession session = sessionWithCode();
        String code = (String) session.getAttribute(SS_AUTH_CODE);

        given(userService.getLoginIdExists(any())).willReturn(exists("Y"));

        mvc.perform(post("/signupProc")
                        .param("userName", "김보호").param("loginId", "taken")
                        .param("password", "kkeudeok1").param("email", EMAIL)
                        .param("authCode", code).session(session))
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.field").value("loginId"));

        assertThat(session.getAttribute(SS_AUTH_CODE))
                .as("아이디가 겹쳤다고 인증번호까지 날리면 안 된다")
                .isEqualTo(code);

        given(userService.getLoginIdExists(any())).willReturn(exists("N"));
        given(userService.insertUser(any())).willReturn(1);

        mvc.perform(post("/signupProc")
                        .param("userName", "김보호").param("loginId", "free")
                        .param("password", "kkeudeok1").param("email", EMAIL)
                        .param("authCode", code).session(session))
                .andExpect(jsonPath("$.result").value(1));

        assertThat(session.getAttribute(SS_AUTH_CODE))
                .as("가입을 끝냈으면 그때는 폐기해야 한다(재사용 방지)")
                .isNull();
    }

    @Test
    @DisplayName("인증번호가 틀리면 아이디·이메일 중복 오류보다 먼저 걸리지 않는다")
    void duplicateChecksRunBeforeAuthCode() throws Exception {
        MockHttpSession session = sessionWithCode();

        given(userService.getLoginIdExists(any())).willReturn(exists("Y"));

        mvc.perform(post("/signupProc")
                        .param("userName", "김보호").param("loginId", "taken")
                        .param("password", "kkeudeok1").param("email", EMAIL)
                        .param("authCode", "000000").session(session))
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.field").value("loginId"));
    }

    @Test
    @DisplayName("인증번호가 틀리면 가입이 막히고, 그래도 인증번호는 남는다(다시 칠 수 있게)")
    void wrongAuthCodeKeepsSession() throws Exception {
        MockHttpSession session = sessionWithCode();
        String code = (String) session.getAttribute(SS_AUTH_CODE);

        given(userService.getLoginIdExists(any())).willReturn(exists("N"));

        mvc.perform(post("/signupProc")
                        .param("userName", "김보호").param("loginId", "free")
                        .param("password", "kkeudeok1").param("email", EMAIL)
                        .param("authCode", "000000").session(session))
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.field").value("authCode"));

        assertThat(session.getAttribute(SS_AUTH_CODE)).isEqualTo(code);
    }

    private UserDTO member(Long id, String parentPin) {
        UserDTO dto = new UserDTO();
        dto.setMemberId(id);
        dto.setLoginId("pa1234");
        dto.setName("김보호");
        dto.setParentPin(parentPin);
        return dto;
    }
    @Test
    @DisplayName("PIN 을 아직 안 만들었으면 보호자 PIN 설정으로 보낸다")
    void loginGoesToPinWhenNotSet() throws Exception {
        given(userService.getLogin(any())).willReturn(member(1L, null));

        mvc.perform(post("/loginProc").param("loginId", "pa1234").param("password", "kkeudeok1"))
                .andExpect(jsonPath("$.result").value(1))
                .andExpect(jsonPath("$.next").value("/onboarding/pin"));
    }

    @Test
    @DisplayName("PIN 은 있고 아이가 없으면 온보딩 시작으로 보낸다")
    void loginGoesToOnboardingWhenNoChild() throws Exception {
        given(userService.getLogin(any())).willReturn(member(1L, "HASHED"));
        given(userService.hasChild(1L)).willReturn(false);

        mvc.perform(post("/loginProc").param("loginId", "pa1234").param("password", "kkeudeok1"))
                .andExpect(jsonPath("$.next").value("/onboarding/start"));
    }

    @Test
    @DisplayName("PIN 도 아이도 있으면 대시보드로 보낸다 — 최초 1회 화면을 다시 띄우지 않는다")
    void loginGoesToDashboardWhenAllDone() throws Exception {
        given(userService.getLogin(any())).willReturn(member(1L, "HASHED"));
        given(userService.hasChild(1L)).willReturn(true);

        mvc.perform(post("/loginProc").param("loginId", "pa1234").param("password", "kkeudeok1"))
                .andExpect(jsonPath("$.next").value("/dashboard"));
    }

    @Test
    @DisplayName("로그인하면 세션에 memberId 가 들어간다 — 없으면 아이 등록이 통째로 실패한다")
    void loginPutsMemberIdInSession() throws Exception {
        given(userService.getLogin(any())).willReturn(member(42L, "HASHED"));

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginProc").param("loginId", "pa1234")
                .param("password", "kkeudeok1").session(session));

        assertThat(session.getAttribute("SS_MEMBER_ID")).isEqualTo(42L);
    }

    @Test
    @DisplayName("PIN 을 저장하면 온보딩 시작으로 넘기고, 다시 안 묻도록 세션에 표시한다")
    void savingPinMovesToOnboarding() throws Exception {
        given(userService.getParentPin(any())).willReturn("");
        given(userService.updateParentPin(any())).willReturn(1);
        given(userService.hasChild(1L)).willReturn(false);
        given(userService.memberExists(1L)).willReturn(true);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SS_USER_ID", "pa1234");
        session.setAttribute("SS_MEMBER_ID", 1L);

        mvc.perform(post("/parentPinProc").param("pin", "1234").session(session))
                .andExpect(jsonPath("$.result").value(1))
                .andExpect(jsonPath("$.next").value("/onboarding/start"));

        assertThat(session.getAttribute(UserProcController.SS_PIN_SET)).isEqualTo(true);
    }

    @Test
    @DisplayName("이미 PIN 이 있으면 여기서는 못 바꾸고 재설정으로 보낸다")
    void existingPinCannotBeOverwritten() throws Exception {
        given(userService.getParentPin(any())).willReturn("ALREADY-HASHED");
        given(userService.memberExists(1L)).willReturn(true);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SS_USER_ID", "pa1234");
        session.setAttribute("SS_MEMBER_ID", 1L);

        mvc.perform(post("/parentPinProc").param("pin", "9999").session(session))
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.next").value("/mypage/pin-reset"));
        verify(userService, never()).updateParentPin(any());
    }

    @Test
    @DisplayName("로그인하지 않으면 PIN 을 만들 수 없다 — 컨트롤러에 닿기 전에 401 로 끊는다")
    void pinNeedsLogin() throws Exception {
        mvc.perform(post("/parentPinProc").param("pin", "1234"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.next").value("/login"));

        verify(userService, never()).updateParentPin(any());
    }

    @Test
    @DisplayName("아이디찾기: 이름이 안 맞아도 인증번호는 살아 있다")
    void findIdKeepsCodeWhenNameWrong() throws Exception {
        MockHttpSession session = sessionWithCode();
        String code = (String) session.getAttribute(SS_AUTH_CODE);

        given(userService.getEmailExists(any())).willReturn(exists("Y"));
        given(userService.getFindId(any())).willReturn(null);

        mvc.perform(post("/findIdProc")
                        .param("userName", "틀린이름").param("email", EMAIL)
                        .param("authCode", code).session(session))
                .andExpect(jsonPath("$.result").value(0))
                .andExpect(jsonPath("$.field").value("userName"));

        assertThat(session.getAttribute(SS_AUTH_CODE))
                .as("이름만 고쳐 다시 누를 수 있어야 한다")
                .isEqualTo(code);
    }

    @Test
    @DisplayName("로그인하면 아이 id 도 세션에 넣는다 — 다시 들어와도 학습을 찾는다")
    void loginPutsChildIdInSession() throws Exception {
        given(userService.getLogin(any())).willReturn(member(42L, "HASHED"));
        given(userService.childIdOf(42L)).willReturn(7L);

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginProc").param("loginId", "pa1234")
                .param("password", "kkeudeok1").session(session));

        assertThat(session.getAttribute("SS_CHILD_ID")).isEqualTo(7L);
    }

    @Test
    @DisplayName("아이가 없으면 세션에 넣지 않는다 — 온보딩 전이다")
    void loginWithoutChildLeavesSessionClean() throws Exception {
        given(userService.getLogin(any())).willReturn(member(42L, "HASHED"));
        given(userService.childIdOf(42L)).willReturn(null);

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/loginProc").param("loginId", "pa1234")
                .param("password", "kkeudeok1").session(session));

        assertThat(session.getAttribute("SS_CHILD_ID")).isNull();
    }
}
