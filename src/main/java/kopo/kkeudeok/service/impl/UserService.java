package kopo.kkeudeok.service.impl;

import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.mapper.IUserMapper;
import kopo.kkeudeok.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService implements IUserService { //IUserService 이렇게 적어져있으면 얘가 정의 되어야 함
    //IUser 뭐가 필요하고 뭐 있어아야 하며, 멀 반환하는지 설명했습니다ㅣ -> 여기 실제 구현하는 로직이 있어야 함.

    private final IUserMapper userMapper; //서비스가 메퍼한테 얘기함.

    /** 아이디·비밀번호가 맞는 회원을 찾는다. 없으면 null. */
    @Override
    public UserDTO getLogin(UserDTO pDTO) throws Exception {

        log.info("{}.getLogin Start!", this.getClass().getName());

        return userMapper.getLogin(pDTO);
    }

    @Override
    public UserDTO getFindId(UserDTO pDTO) throws Exception {
        return userMapper.getFindId(pDTO);
    }

}
