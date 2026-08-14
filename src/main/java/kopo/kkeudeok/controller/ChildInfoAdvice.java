package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.mapper.ChildMapper;
import kopo.kkeudeok.util.SessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 로그인한 보호자의 아이 이름·나이를 모든 화면에 실어 준다.
 *
 * 왜 필요한가 — 화면의 아이 이름은 sessionStorage(kdOnb)에서만 채워졌다. 그 값은 온보딩을
 * 막 마친 그 탭에만 있어서, 다시 로그인하거나 새 탭으로 들어오면 비어 있었다. 그러면
 * JSP 에 박아 둔 예시값 '지우'가 그대로 남아 모든 화면이 남의 아이 이름을 달고 있었다
 * (2026-08-14 지적). DB 에 있는 값을 서버가 처음부터 실어 보내면 그럴 일이 없다.
 *
 * 화면 라우팅 컨트롤러에만 붙인다 — API 응답(JSON)에는 넣을 이유가 없다.
 */
@Slf4j
@ControllerAdvice(assignableTypes = UserController.class)
@RequiredArgsConstructor
public class ChildInfoAdvice {

    private final ChildMapper childMapper;

    @ModelAttribute
    public void addChildInfo(HttpSession session, Model model) {

        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        if (memberId == null) {
            return;     // 로그인 전 화면(로그인·회원가입)은 예시값 그대로 둔다
        }

        try {
            ChildDTO child = childMapper.selectChildByMember(memberId);

            if (child == null) {
                return; // 아직 온보딩 전 — 채울 값이 없다
            }

            model.addAttribute("kdChildName", child.getName());
            model.addAttribute("kdChildCall", child.getCallName());
            model.addAttribute("kdChildAge", child.getAge());

        } catch (Exception e) {
            // 이름을 못 채운다고 화면 자체가 못 뜰 이유는 없다 — 예시값으로 그려진다
            log.warn("아이 정보 조회 실패 memberId={}", memberId, e);
        }
    }
}
