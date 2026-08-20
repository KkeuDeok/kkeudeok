package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.MsgDTO;
import kopo.kkeudeok.service.IChildService;
import kopo.kkeudeok.util.SessionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChildControllerTest {

    private IChildService childService;
    private ChildController controller;

    private static final Long MEMBER = 7L;
    private static final Long MY_CHILD = 3L;

    @BeforeEach
    void setUp() {
        childService = mock(IChildService.class);

        when(childService.getChildByMember(MEMBER))
                .thenReturn(ChildDTO.builder().childId(MY_CHILD).memberId(MEMBER).build());
        when(childService.updateProfile(any())).thenReturn(1);
        when(childService.updateCharacter(any())).thenReturn(1);

        controller = new ChildController(childService);
    }

    private HttpSession loggedIn() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.MEMBER_ID, MEMBER);
        return session;
    }

    /** 화면(child.jsp)이 보내는 그대로가 DTO 로 풀려야 한다 — birthDate 는 "YYYY-MM-DD" 문자열이다. */
    @Test
    @DisplayName("화면이 보내는 JSON 이 ChildDTO 로 그대로 풀린다")
    void jsonBindsToChildDto() {

        String body = """
                {"childId":3,"name":"김지우","birthDate":"2019-03-05",
                 "gender":"여","disorderType":"자폐 장애","severity":"경도"}
                """;

        ChildDTO dto = new ObjectMapper().readValue(body, ChildDTO.class);

        assertThat(dto.getChildId()).isEqualTo(3L);
        assertThat(dto.getBirthDate()).isEqualTo(LocalDate.of(2019, 3, 5));
        assertThat(dto.getSeverity()).isEqualTo("경도");
    }

    @Test
    @DisplayName("내 아이면 저장한다")
    void savesOwnChild() {

        ChildDTO req = ChildDTO.builder().childId(MY_CHILD).name("김지우").build();

        MsgDTO res = controller.updateProfile(req, loggedIn());

        assertThat(res.getResult()).isEqualTo(1);
        verify(childService).updateProfile(req);
    }

    /**
     * 남의 아이 번호를 실어 보내도 고쳐지면 안 된다.
     * 예전 ProfileController 는 요청 본문의 childId 를 그대로 믿고 UPDATE 했다.
     */
    @Test
    @DisplayName("남의 아이 번호를 보내면 저장하지 않는다")
    void rejectsOtherChild() {

        ChildDTO req = ChildDTO.builder().childId(999L).name("남의아이").build();

        MsgDTO res = controller.updateProfile(req, loggedIn());

        assertThat(res.getResult()).isZero();
        verify(childService, never()).updateProfile(any());
    }

    @Test
    @DisplayName("로그인하지 않았으면 저장하지 않는다")
    void rejectsAnonymous() {

        ChildDTO req = ChildDTO.builder().childId(MY_CHILD).build();

        MsgDTO res = controller.updateCharacter(req, new MockHttpSession());

        assertThat(res.getResult()).isZero();
        verify(childService, never()).updateCharacter(any());
    }
}
