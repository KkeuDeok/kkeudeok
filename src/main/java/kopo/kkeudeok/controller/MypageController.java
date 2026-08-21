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

@RestController
@RequiredArgsConstructor
@Slf4j
public class MypageController {

    private final IUserService userService;

    private static final String SS_USER_ID = "SS_USER_ID";

    private MsgDTO msg(int result, String text) {
        MsgDTO dto = new MsgDTO();
        dto.setResult(result);
        dto.setMsg(text);
        return dto;
    }

    private static final Set<String> RELATIONS = Set.of("어머니", "아버지", "조부모", "기타 보호자");

    // 회원정보 저장
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
            if (name.length() > 50) {
                return msg(0, "보호자 이름은 50자까지 입력할 수 있어요.");
            }
            if (!ph.isEmpty() && !ph.matches("[0-9-]{9,20}")) {
                return msg(0, "휴대폰 번호는 숫자와 - 만 써서 입력해 주세요.");
            }
            if (!rel.isEmpty() && !RELATIONS.contains(rel)) {
                return msg(0, "아이와의 관계를 다시 골라 주세요.");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);
            pDTO.setName(name);
            pDTO.setPhone(ph.isEmpty() ? null : ph);
            pDTO.setRelation(rel.isEmpty() ? null : rel);

            if (userService.updateUserInfo(pDTO) < 1) {
                return msg(0, "저장에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            }

            session.setAttribute("SS_USER_NAME", name);

            return msg(1, "회원정보를 저장했어요");

        } catch (Exception e) {
            log.error("updateUserInfoProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }

    // 계정 삭제
    @PostMapping("/deleteAccountProc")
    public MsgDTO deleteAccountProc(HttpSession session) {

        log.info("{}.deleteAccountProc Start!", this.getClass().getName());

        try {
            String loginId = CmmUtil.nvl((String) session.getAttribute(SS_USER_ID));

            if (loginId.isEmpty()) {
                return msg(0, "로그인이 필요합니다. 다시 로그인해 주세요.");
            }

            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(loginId);

            UserDTO rDTO = userService.getUserInfo(pDTO);

            if (rDTO == null) {
                return msg(0, "이미 삭제된 계정입니다.");
            }

            UserDTO dDTO = new UserDTO();
            dDTO.setMemberId(rDTO.getMemberId());

            if (userService.deleteUser(dDTO) < 1) {
                return msg(0, "계정 삭제에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            }

            session.invalidate();

            return msg(1, "계정이 삭제되었습니다.");

        } catch (Exception e) {
            log.error("deleteAccountProc 실패", e);
            return msg(2, "시스템 오류가 발생했습니다.");
        }
    }
}
