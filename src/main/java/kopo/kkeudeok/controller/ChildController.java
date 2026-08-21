package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.MsgDTO;
import kopo.kkeudeok.service.IChildService;
import kopo.kkeudeok.util.SessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 아동 프로필 수정
@Slf4j
@RestController
@RequestMapping("/child")
@RequiredArgsConstructor
public class ChildController {

    private final IChildService childService;

    @PostMapping("/updateProfile")
    public MsgDTO updateProfile(@RequestBody ChildDTO pDTO, HttpSession session) {
        return save(pDTO, session, true);
    }

    @PostMapping("/updateCharacter")
    public MsgDTO updateCharacter(@RequestBody ChildDTO pDTO, HttpSession session) {
        return save(pDTO, session, false);
    }

    private MsgDTO save(ChildDTO pDTO, HttpSession session, boolean profile) {

        String what = profile ? "프로필 정보" : "캐릭터 정보";

        MsgDTO dto = new MsgDTO();
        dto.setResult(0);

        try {
            Long memberId = SessionKeys.longOf(session, SessionKeys.MEMBER_ID);

            if (memberId == null) {
                dto.setMsg("로그인이 필요합니다. 다시 로그인해 주세요.");
                return dto;
            }

            ChildDTO mine = childService.getChildByMember(memberId);

            if (mine == null || pDTO.getChildId() == null
                    || !mine.getChildId().equals(pDTO.getChildId())) {
                log.warn("남의 아이를 고치려 했습니다 — memberId={}, 요청 childId={}",
                        memberId, pDTO.getChildId());
                dto.setMsg("아이 정보를 찾을 수 없습니다.");
                return dto;
            }

            int res = profile ? childService.updateProfile(pDTO)
                              : childService.updateCharacter(pDTO);

            dto.setResult(res);
            dto.setMsg(res > 0 ? what + "가 저장되었어요" : what + " 수정 실패");

        } catch (Exception e) {
            log.error("아동 {} 수정 실패", what, e);
            dto.setMsg("수정 중 오류가 발생했습니다.");
        }

        return dto;
    }
}
