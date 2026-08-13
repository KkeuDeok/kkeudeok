package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.UserDTO;

public interface IUserService {
    UserDTO getLoginIdExists(UserDTO pDTO) throws Exception;

    UserDTO getEmailExists(UserDTO pDTO) throws Exception;

    int insertUser(UserDTO pDTO) throws Exception;

    //비번 재설정
    int newPasswordProc(UserDTO pDTO) throws Exception;
}
