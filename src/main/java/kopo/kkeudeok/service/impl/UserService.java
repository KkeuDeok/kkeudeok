package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.MailDTO;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.mapper.IUserMapper;
import kopo.kkeudeok.service.IMailService;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.CmmUtil;
import kopo.kkeudeok.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private final IUserMapper userMapper;
    private final IMailService mailService;

    /* ---------- 회원가입 ---------- */

    @Override
    public UserDTO getLoginIdExists(UserDTO pDTO) throws Exception {
        log.info("{}.getLoginIdExists Start!", this.getClass().getName());

        UserDTO rDTO = Optional.ofNullable(userMapper.getLoginIdExists(pDTO)).orElseGet(UserDTO::new);

        log.info("{}.getLoginIdExists End!", this.getClass().getName());
        return rDTO;
    }

    @Override
    public UserDTO getEmailExists(UserDTO pDTO) throws Exception {
        log.info("{}.getEmailExists Start!", this.getClass().getName());

        // 인증번호 발송은 AuthApiController 가 세션 기반으로 처리한다.
        // 여기서는 "이미 가입된 메일인지"만 본다.
        UserDTO rDTO = Optional.ofNullable(userMapper.getEmailExists(pDTO)).orElseGet(UserDTO::new);

        log.info("{}.getEmailExists End!", this.getClass().getName());
        return rDTO;
    }

    @Override
    public int insertUser(UserDTO pDTO) throws Exception {
        log.info("{}.insertUser Start!", this.getClass().getName());

        int res = 0;

        // 시각은 DB 의 NOW() 가 아니라 앱에서 넣는다.
        // VM 의 MariaDB 시계가 실제 시각보다 하루 넘게 뒤처져 있어(2026-08-13 확인)
        // NOW() 를 쓰면 가입 시각이 과거로 찍힌다. VM 시계를 맞추더라도 이 편이 안전하다.
        LocalDateTime now = LocalDateTime.now();
        pDTO.setAgreedAt(now);
        pDTO.setCreatedAt(now);
        pDTO.setUpdatedAt(now);

        if (userMapper.insertUser(pDTO) > 0) {
            res = 1;

            // 가입 축하 메일 — 실패해도 가입 자체는 성공으로 둔다(MailService 가 예외를 삼킨다).
            MailDTO mDTO = new MailDTO();
            mDTO.setToMail(EncryptUtil.decAES128CBC(CmmUtil.nvl(pDTO.getEmail())));
            mDTO.setTitle("[끄덕] 회원가입을 축하합니다");
            mDTO.setContents(MailService.welcomeHtml(CmmUtil.nvl(pDTO.getName())));
            mailService.doSendMail(mDTO);
        }

        log.info("{}.insertUser End! res={}", this.getClass().getName(), res);
        return res;
    }

    /* ---------- 로그인 · 아이디 찾기 ---------- */

    @Override
    public UserDTO getLogin(UserDTO pDTO) throws Exception {
        log.info("{}.getLogin Start!", this.getClass().getName());
        return userMapper.getLogin(pDTO);
    }

    @Override
    public UserDTO getFindId(UserDTO pDTO) throws Exception {
        log.info("{}.getFindId Start!", this.getClass().getName());
        return userMapper.getFindId(pDTO);
    }

    /* ---------- 비밀번호 찾기 ---------- */

    @Override
    public UserDTO getFindPwUser(UserDTO pDTO) throws Exception {
        log.info("{}.getFindPwUser Start!", this.getClass().getName());
        return userMapper.getFindPwUser(pDTO);
    }

    @Override
    public int newPasswordProc(UserDTO pDTO) throws Exception {
        log.info("{}.newPasswordProc Start!", this.getClass().getName());

        pDTO.setUpdatedAt(LocalDateTime.now());   // DB 시계를 믿지 않는다(insertUser 주석 참고)

        int success = userMapper.updatePassword(pDTO);

        log.info("{}.newPasswordProc End! success={}", this.getClass().getName(), success);
        return success;
    }

    /* ---------- 마이페이지 ---------- */

    @Override
    public UserDTO getUserInfo(UserDTO pDTO) throws Exception {
        log.info("{}.getUserInfo Start!", this.getClass().getName());

        UserDTO rDTO = userMapper.getUserInfo(pDTO);

        // 이메일만 되돌린다. 비밀번호는 단방향 해시라 애초에 되돌릴 수 없고 조회하지도 않는다.
        if (rDTO != null) {
            rDTO.setEmail(EncryptUtil.decAES128CBC(CmmUtil.nvl(rDTO.getEmail())));
        }

        log.info("{}.getUserInfo End!", this.getClass().getName());
        return rDTO;
    }

    @Override
    public int updateUserInfo(UserDTO pDTO) throws Exception {
        log.info("{}.updateUserInfo Start!", this.getClass().getName());

        pDTO.setUpdatedAt(LocalDateTime.now());   // DB 시계를 믿지 않는다(insertUser 주석 참고)

        int success = userMapper.updateUserInfo(pDTO);

        log.info("{}.updateUserInfo End! success={}", this.getClass().getName(), success);
        return success;
    }

    /**
     * 계정 완전 삭제. DELETE 가 네 번 나가므로 하나라도 실패하면 전부 되돌려야 한다
     * — 안 그러면 아이 기록만 지워지고 계정은 남는 반쪽 상태가 된다.
     *
     * ⚠ @Transactional 은 기본적으로 RuntimeException 에만 롤백한다.
     *   이 메서드는 throws Exception(검사 예외)이라 rollbackFor 를 반드시 적어야 한다.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUser(UserDTO pDTO) throws Exception {
        log.info("{}.deleteUser Start! memberId={}", this.getClass().getName(), pDTO.getMemberId());

        // 순서 = 외래키 순서. 바꾸면 FK 위반으로 실패한다(UserMapper.xml 주석 참고).
        userMapper.deleteStorySessionByMember(pDTO);
        userMapper.deleteStoryByMember(pDTO);
        userMapper.deleteChildByMember(pDTO);

        int success = userMapper.deleteUser(pDTO);

        log.info("{}.deleteUser End! success={}", this.getClass().getName(), success);
        return success;
    }
}
