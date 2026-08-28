package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.ChildDTO;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.mapper.IChildMapper;
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
    private final IChildMapper childMapper;

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

        UserDTO rDTO = Optional.ofNullable(userMapper.getEmailExists(pDTO)).orElseGet(UserDTO::new);

        log.info("{}.getEmailExists End!", this.getClass().getName());
        return rDTO;
    }

    @Override
    public int insertUser(UserDTO pDTO) throws Exception {
        log.info("{}.insertUser Start!", this.getClass().getName());

        int res = 0;

        LocalDateTime now = LocalDateTime.now();
        pDTO.setAgreedAt(now);
        pDTO.setCreatedAt(now);
        pDTO.setUpdatedAt(now);

        if (userMapper.insertUser(pDTO) > 0) {
            res = 1;

            mailService.sendWelcome(
                    EncryptUtil.decAES128CBC(CmmUtil.nvl(pDTO.getEmail())),
                    CmmUtil.nvl(pDTO.getName()));
        }

        log.info("{}.insertUser End! res={}", this.getClass().getName(), res);
        return res;
    }

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

    @Override
    public UserDTO getFindPwUser(UserDTO pDTO) throws Exception {
        log.info("{}.getFindPwUser Start!", this.getClass().getName());
        return userMapper.getFindPwUser(pDTO);
    }

    @Override
    public int newPasswordProc(UserDTO pDTO) throws Exception {
        log.info("{}.newPasswordProc Start!", this.getClass().getName());

        pDTO.setUpdatedAt(LocalDateTime.now());

        int success = userMapper.updatePassword(pDTO);

        log.info("{}.newPasswordProc End! success={}", this.getClass().getName(), success);
        return success;
    }

    @Override
    public int updateParentPin(UserDTO pDTO) throws Exception {
        log.info("{}.updateParentPin Start!", this.getClass().getName());

        pDTO.setUpdatedAt(LocalDateTime.now());

        int success = userMapper.updateParentPin(pDTO);

        log.info("{}.updateParentPin End! success={}", this.getClass().getName(), success);
        return success;
    }

    @Override
    public String getParentPin(UserDTO pDTO) throws Exception {
        log.info("{}.getParentPin Start!", this.getClass().getName());

        return CmmUtil.nvl(userMapper.getParentPin(pDTO));
    }

    @Override
    public boolean hasChild(Long memberId) throws Exception {
        if (memberId == null) {
            return false;
        }
        return childIdOf(memberId) != null;
    }

    @Override
    public boolean memberExists(Long memberId) throws Exception {
        return memberId != null && userMapper.countMember(memberId) > 0;
    }

    @Override
    public Long childIdOf(Long memberId) throws Exception {

        if (memberId == null) {
            return null;
        }

        ChildDTO child = childMapper.selectChildByMember(memberId);
        return child == null ? null : child.getChildId();
    }

    @Override
    public UserDTO getUserInfo(UserDTO pDTO) throws Exception {
        log.info("{}.getUserInfo Start!", this.getClass().getName());

        UserDTO rDTO = userMapper.getUserInfo(pDTO);

        if (rDTO != null) {
            rDTO.setEmail(EncryptUtil.decAES128CBC(CmmUtil.nvl(rDTO.getEmail())));
        }

        log.info("{}.getUserInfo End!", this.getClass().getName());
        return rDTO;
    }

    @Override
    public int updateUserInfo(UserDTO pDTO) throws Exception {
        log.info("{}.updateUserInfo Start!", this.getClass().getName());

        pDTO.setUpdatedAt(LocalDateTime.now());

        int success = userMapper.updateUserInfo(pDTO);

        log.info("{}.updateUserInfo End! success={}", this.getClass().getName(), success);
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUser(UserDTO pDTO) throws Exception {
        log.info("{}.deleteUser Start! memberId={}", this.getClass().getName(), pDTO.getMemberId());
        userMapper.deleteStorySessionByMember(pDTO);
        userMapper.deleteStoryByMember(pDTO);
        userMapper.deleteChildByMember(pDTO);

        int success = userMapper.deleteUser(pDTO);
        log.info("{}.deleteUser End! success={}", this.getClass().getName(), success);
        return success;
    }
}
