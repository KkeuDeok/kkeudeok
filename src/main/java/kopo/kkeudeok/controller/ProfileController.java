package kopo.kkeudeok.controller;

import kopo.kkeudeok.dto.MsgDTO;
import kopo.kkeudeok.dto.ProfileDTO;
import kopo.kkeudeok.service.IProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping(value = "/profile")
@RequiredArgsConstructor
@RestController
public class ProfileController {

    private final IProfileService profileService;

    /**
     * 아동 프로필 및 캐릭터 정보 조회
     */
    @GetMapping(value = "getProfile")
    public ProfileDTO getProfile(HttpSession session) throws Exception {
        log.info("{}.getProfile Start!", this.getClass().getName());

        Long memberId = (Long) session.getAttribute("name");

        log.info("session memberId : {}", memberId);

        ProfileDTO pDTO = new ProfileDTO();
        pDTO.setMemberId(memberId);

        ProfileDTO rDTO = profileService.getProfile(pDTO);

        log.info("{}.getProfile End!", this.getClass().getName());

        return rDTO;
    }

    /**
     * 아동 프로필 정보 수정 (프로필 정보 탭)
     */
    @PostMapping(value = "updateProfile")
    public MsgDTO updateProfile(@RequestBody ProfileDTO pDTO) throws Exception {
        log.info("{}.updateProfile Start!", this.getClass().getName());

        String msg = "프로필 정보 수정 실패";
        int res = 0;
        MsgDTO dto = new MsgDTO();

        try {
            log.info("childId : {}", pDTO.getChildId());
            log.info("name : {}", pDTO.getName());

            res = profileService.updateProfile(pDTO);

            if (res > 0) {
                msg = "프로필 정보가 성공적으로 수정되었습니다.";
            }
            /**
             *  굳이 실패를 넣지 않고 수정중 오류로 하는게 좋을 것 같음
             * else {
                msg = "프로필 정보 수정에 실패했습니다.";
            } */
        } catch (Exception e) {
            msg = "수정 중 오류가 발생했습니다: " + e.getMessage();
            log.error("updateProfile error : ", e);
        }
        dto.setMsg(msg);
        dto.setResult(res);

        log.info("{}.updateProfile End!", this.getClass().getName());

        return dto;
    }

    /**
     * 캐릭터 및 애칭 수정 (캐릭터 관리 탭)
     */
    @PostMapping(value = "updateCharacter")
    public MsgDTO updateCharacter(@RequestBody ProfileDTO pDTO) throws Exception {
        log.info("{}.updateCharacter Start!", this.getClass().getName());

        String msg = "캐릭터 정보 수정 실패";
        int res = 0;
        MsgDTO dto = new MsgDTO();

        try {
            log.info("childId : {}", pDTO.getChildId());
            log.info("characterType : {}", pDTO.getCharacterType());

            // 1. 서비스 호출하여 수정 처리
            res = profileService.updateCharacter(pDTO);

            if (res > 0) {
                msg = "캐릭터 정보가 성공적으로 수정되었습니다.";
            }
            /**
             * 이것도 마찬가지
             * else {
                msg = "캐릭터 정보 수정에 실패했습니다.";
            }
             */

        } catch (Exception e) {
            msg = "수정 중 오류가 발생했습니다: " + e.getMessage();
            log.error("updateCharacter error : ", e);
        }

        dto.setMsg(msg);
        dto.setResult(res);

        log.info("{}.updateCharacter End!", this.getClass().getName());

        return dto;
    }
}