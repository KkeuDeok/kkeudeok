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

        int success = userMapper.updatePassword(pDTO);

        log.info("{}.newPasswordProc End! success={}", this.getClass().getName(), success);
        return success;
    }
}
