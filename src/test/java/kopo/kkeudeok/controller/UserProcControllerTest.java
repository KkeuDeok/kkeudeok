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

/**
 * 인증번호를 "언제 폐기하는가"를 못 박는 테스트.
 *
 * 겪은 일(2026-08-14): 이메일 인증까지 마치고 [확인]을 눌렀더니 아이디가 겹친다고 나왔다.
 * 아이디만 고쳐 다시 눌렀더니 이번엔 인증번호가 틀렸다고 했다 — 맞게 쳤는데도.
 * 첫 시도에서 인증번호 대조에 성공하자마자 세션에서 지워 버려서였다.
 * 되돌릴 수 없는 일(폐기)은 모든 검사를 통과한 뒤에 해야 한다.
 */
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

    /** ChildInfoAdvice(@ControllerAdvice)가 이 슬라이스에도 올라온다 — 쓰지는 않지만 빈이 있어야 뜬다. */
    @MockitoBean
    private IChildMapper childMapper;

    /** existsYn 을 담은 UserDTO 하나. */
    private UserDTO exists(String yn) {
        UserDTO dto = new UserDTO();
        dto.setExistsYn(yn);
        return dto;
    }

    /** 인증번호를 발송받은 상태의 세션을 만든다. 실제 발송 흐름을 그대로 탄다. */
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

        // 1) 겹치는 아이디로 시도 → 아이디 칸 오류
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

        // 2) 아이디만 바꿔 다시 시도 → 같은 인증번호로 통과
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

        // 인증번호를 틀리게 보내도, 먼저 알려 줄 것은 아이디 중복이다
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

    /* ================================================================
     * 로그인 뒤 어디로 가는가 — 최초 1회 설정이 남았는지로 갈린다.
     * 예전에는 화면이 무조건 /onboarding/pin 으로 보내서, PIN 을 이미 만든 사람도
     * 로그인할 때마다 PIN 설정 화면을 다시 봤다(= 최초 1회가 아니었다).
     * ================================================================ */

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
        /* 아직 PIN 을 안 만든 상태 — 실제 구현은 CmmUtil.nvl 을 거쳐 빈 문자열을 준다(null 아님) */
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

    /**
     * 최초 1회 전용 통로다. 로그인만 돼 있으면 이 주소를 직접 불러 남의 PIN 을 갈아 끼우고
     * 들어갈 수 있으면 게이트가 무의미해진다 — 바꾸는 길은 메일 인증을 거치는 재설정뿐이다.
     */
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

    /**
     * 온보딩에서만 CHILD_ID 를 넣던 시절에는, 온보딩을 마친 뒤 다시 로그인하면 세션에
     * 아이가 없었다. 그러면 학습·리포트가 아이를 못 찾아 [학습 시작하기] 가
     * '이야기 생성중…' 에서 안 풀렸다 — 로드맵은 멀쩡히 있는데도(2026-08-18).
     */
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