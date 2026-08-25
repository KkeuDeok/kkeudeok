package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.CharacterType;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.mapper.IChildMapper;
import kopo.kkeudeok.util.SessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@ControllerAdvice(assignableTypes = UserController.class)
@RequiredArgsConstructor
public class ChildInfoAdvice {

    private final IChildMapper childMapper;

    private static String charLabel(String key) {
        return CharacterType.labelOf(key);
    }

    @ModelAttribute
    public void addChildInfo(HttpSession session, Model model) {

        Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

        if (memberId == null) {
            return;
        }

        try {
            ChildDTO child = childMapper.selectChildByMember(memberId);

            if (child == null) {
                return;
            }

            model.addAttribute("kdChildName", child.getName());
            model.addAttribute("kdChildCall", child.getCallName());
            model.addAttribute("kdChildVocative", child.getVocative());
            model.addAttribute("kdChildAge", child.getAge());

            String key = child.getCharacterType() == null ? "tori" : child.getCharacterType().trim();
            String nick = child.getCharacterNickname();

            model.addAttribute("kdCharKey", key.isEmpty() ? "tori" : key);
            model.addAttribute("kdCharName",
                    (nick == null || nick.isBlank()) ? charLabel(key) : nick.trim());

        } catch (Exception e) {
            log.warn("아이 정보 조회 실패 memberId={}", memberId, e);
        }
    }
}
