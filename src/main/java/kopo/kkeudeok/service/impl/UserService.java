package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.MailDTO;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.mapper.IUserMapper;
import kopo.kkeudeok.service.IMailService;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService {

    private final IUserMapper userMapper;
    private final IMailService mailService;
    // 메일 발송을 위한 자바 객체 가져오는데 아직 없음

    @Override
    public UserDTO getLoginIdExists(UserDTO pDTO) throws Exception {
        log.info("{}getLoginIdExists Start!", this.getClass().getName());
        UserDTO rDTO = userMapper.getLoginIdExists(pDTO);
        log.info("{}.getUserIdExists End", this.getClass().getName());
        return rDTO;
    }

    @Override
    public UserDTO getEmailExists(UserDTO pDTO) throws Exception {
        log.info("{}.email Start!", this.getClass().getName());
        UserDTO rDTO = Optional.ofNullable(userMapper.getEmailExists(pDTO)).orElseGet(UserDTO::new);
        log.info("rDTO : {}",rDTO);

        if(kopo.poly.util.CmmUtil.nvl(rDTO.getExistsYn()).equals("N")) {
            int authNumber = ThreadLocalRandom.current().nextInt(100000,1000000);
            log.info("authNumber : {}",authNumber);
            MailDTO dto = new MailDTO();
            dto.setTitle("아매알 중복 확인 인증번호 발송 메일");
            dto.setContents("인증번호"+authNumber+"입니다");
            dto.setToMail(EncryptUtil.decAES128CBC(kopo.poly.util.CmmUtil.nvl(pDTO.getEmail())));

            mailService.doSendMail(dto);

            rDTO.setAuthNumber(authNumber);//인증번호를 결과 값에 넣어주기
        }
        log.info("{}.emilAuth End!",this.getClass().getName());
        return rDTO;
    }

    @Override
    public int insertUser(UserDTO pDTO) throws Exception {
        log.info("{}.insertUserInfo Start!",this.getClass().getName());
        int res;
        int success = userMapper.insertUser(pDTO);
        //
        if (success > 0){
            res = 1;
            MailDTO mDTO  = new MailDTO();

            mDTO.setToMail(EncryptUtil.decAES128CBC(kopo.poly.util.CmmUtil.nvl(pDTO.getEmail())));
            mDTO.setTitle("회원 가입을 축하드립니다.");
            mDTO.setContents(kopo.poly.util.CmmUtil.nvl(pDTO.getName())+ "님의 회원가입 ㅊㅋㅊㅋ");
            mailService.doSendMail(mDTO);
        } else {
            res = 0;
        }
        log.info("{}.insertUserInfo End",this.getClass().getName());
        return res;
    }
    //private final IMailService mailService;
    // 메일 발송을 위한 객체 가져오는데 이건 아직 없음

    //비번 함수 구현
    @Override
    public int newPasswordProc(UserDTO pDTO) throws Exception{
        log.info("{}.newPasswordProc Strart!", this.getClass().getName());
        int success = userMapper.updatePassword(pDTO);
        log.info("{}.newPasswordProc End!",this.getClass().getName());
        return success;
    }


}
