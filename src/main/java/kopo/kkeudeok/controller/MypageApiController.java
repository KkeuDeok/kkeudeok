package kopo.kkeudeok.controller;

import java.util.Set;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.MsgDTO;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지의 POST 처리. 응답은 전부 {@link MsgDTO} JSON 이고 화면 이동은 JSP 의 fetch 가 정한다.
 *
 * ⚠ 주소를 `/mypage/...` 로 짓지 않는다 — WebConfig 의 로그인 인터셉터가 그 경로를 가로채
 *   로그인 화면(HTML)으로 리다이렉트하는데, fetch 는 JSON 을 기대하므로 파싱 오류가 난다.
 *   대신 이 컨트롤러가 직접 세션을 보고 JSON 으로 답한다(loginProc·signupProc 과 같은 이름 규칙).
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class MypageApiController {

    private final IUserService userService;

    /** AuthApiController.loginProc 이 세션에 넣어 둔 값 */
    private static final String SS_USER_ID = "SS_USER_ID";

    /** MsgDTO 를 만드는 짧은 도우미(AuthApiController 와 같은 모양) */
    private MsgDTO msg(int result, String text) {
        MsgDTO dto = new MsgDTO();
        dto.setResult(result);
        dto.setMsg(text);
        return dto;
    }

    /** 아이와의 관계 — 화면 select 의 4개와 같아야 한다. DB 컬럼이 VARCHAR(10) 이라 길이도 이 안에서 지킨다 */
    private static final Set<String> RELATIONS = Set.of("어머니", "아버지", "조부모", "기타 보호자");

    /**
     * 회원정보 저장 — 이름·휴대폰·관계.
     *
     * 검사는 전부 여기서 다시 한다. 화면 검사는 개발자도구로 우회되고,
     * 길이를 넘긴 값이 그대로 가면 DB 가 잘라내거나(경고) SQL 오류로 500 이 난다.
     */
    @PostMapping("/updateUserInfoProc")
    public MsgDTO updateUserInfoProc(@RequestParam String userName,
                                     @RequestParam(required = false) String phone,
                                     @RequestParam(required = false) String relation,
                                     HttpSession session) {

        log.info("{}.updateUserInfoProc Start!", this.getClass().getName());

        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute(SS_USER_ID));

            if (loginId.isEmpty()) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.");
            }

            String name = CmmUtil.nvl(userName).trim();
            String ph = CmmUtil.nvl(phone).trim();
            String rel = CmmUtil.nvl(relation).trim();

            if (name.isEmpty()) {
                return msg(0, "보호자 이름을 입력해 주세요.");
            }
            if (name.length() > 50) {                       // member.name VARCHAR(50)
                return msg(0, "보호자 이름은 50자까지 입력할 수 있어요.");
            }
            // 숫자와 - 만. 010-1234-5678(13) ~ 국제번호까지 고려해 20자 상한(member.phone VARCHAR(20)).
            if (!ph.isEmpty() && !ph.matches("[0-9-]{9,20}")) {
                return msg(0, "휴대폰 번호는 숫자와 - 만 써서 입력해 주세요.");
            }
            if (!rel.isEmpty() && !RELATIONS.contains(rel)) {
                return msg(0, "아이와의 관계를 다시 골라 주세요.");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);
            pDTO.setName(name);
            // 빈 칸은 빈 문자열이 아니라 NULL 로 넣는다 — "아직 안 적음"과 "빈 값"을 구분한다.
            pDTO.setPhone(ph.isEmpty() ? null : ph);
            pDTO.setRelation(rel.isEmpty() ? null : rel);

            if (userService.updateUserInfo(pDTO) < 1) {
                return msg(0, "저장에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            }

            // 로그인할 때 세션에 넣어 둔 이름이 옛 값으로 남지 않게 같이 갱신한다.
            session.setAttribute("SS_USER_NAME", name);

            return msg(1, "회원정보를 저장했어요");

        } catch (Exception e) {
            log.error("updateUserInfoProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }

    /**
     * 계정 완전 삭제.
     *
     * 대상은 세션의 로그인 아이디로 정한다 — 화면에서 아이디를 받으면
     * 남의 아이디를 실어 보내 남의 계정을 지울 수 있다.
     *
     * ⚠ 비밀번호 재확인은 팀 결정으로 뺐다(2026-08-14). 모달 확인 한 번이면 바로 지워진다.
     *   로그인된 채 자리를 비운 화면에서 누가 눌러도 막을 방법이 없다는 뜻이다.
     *   되살리려면 password 파라미터를 받아 getLogin 으로 대조하면 된다(그게 원래 코드였다).
     */
    @PostMapping("/deleteAccountProc")
    public MsgDTO deleteAccountProc(HttpSession session) {

        log.info("{}.deleteAccountProc Start!", this.getClass().getName());

        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute(SS_USER_ID));

            // 인터셉터가 화면은 막지만 이 주소는 직접 호출될 수 있다 — 서버가 다시 본다.
            if (loginId.isEmpty()) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);

            UserDTO rDTO = userService.getUserInfo(pDTO);   // member_id 를 얻으려고 조회한다

            if (rDTO == null) {
                return msg(0, "이미 삭제된 계정입니다.");
            }

            // 자식 테이블이 member_id 로 물려 있어 아이디가 아니라 번호로 지운다.
            UserDTO dDTO = new UserDTO();
            dDTO.setMemberId(rDTO.getMemberId());

            if (userService.deleteUser(dDTO) < 1) {
                return msg(0, "계정 삭제에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            }

            // 계정이 사라졌으니 세션도 같이 버린다. 안 버리면 없는 계정으로 로그인된 채 남는다.
            session.invalidate();

            return msg(1, "계정이 삭제되었습니다.");

        } catch (Exception e) {
            log.error("deleteAccountProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }
}
