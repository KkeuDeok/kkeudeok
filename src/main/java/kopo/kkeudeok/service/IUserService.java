package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.UserDTO;

public interface IUserService {

    UserDTO getLoginIdExists(UserDTO pDTO) throws Exception;

    UserDTO getEmailExists(UserDTO pDTO) throws Exception;

    int insertUser(UserDTO pDTO) throws Exception;

    UserDTO getLogin(UserDTO pDTO) throws Exception;

    UserDTO getFindId(UserDTO pDTO) throws Exception;

    UserDTO getFindPwUser(UserDTO pDTO) throws Exception;

    int newPasswordProc(UserDTO pDTO) throws Exception;

    int updateParentPin(UserDTO pDTO) throws Exception;

    String getParentPin(UserDTO pDTO) throws Exception;
    boolean hasChild(Long memberId) throws Exception;

    Long childIdOf(Long memberId) throws Exception;

    boolean memberExists(Long memberId) throws Exception;

    UserDTO getUserInfo(UserDTO pDTO) throws Exception;

    int updateUserInfo(UserDTO pDTO) throws Exception;
    int deleteUser(UserDTO pDTO) throws Exception;
}
