package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IUserMapper {
    int countMember(@Param("memberId") Long memberId) throws Exception;

    int insertUser(UserDTO pDTO) throws Exception;

    UserDTO getLoginIdExists(UserDTO pDTO) throws Exception;

    UserDTO getEmailExists(UserDTO pDTO) throws Exception;

    UserDTO getLogin(UserDTO pDTO) throws Exception;

    UserDTO getFindId(UserDTO pDTO) throws Exception;

    UserDTO getFindPwUser(UserDTO pDTO) throws Exception;

    int updatePassword(UserDTO pDTO) throws Exception;

    int updateParentPin(UserDTO pDTO) throws Exception;

    String getParentPin(UserDTO pDTO) throws Exception;

    UserDTO getUserInfo(UserDTO pDTO) throws Exception;

    int updateUserInfo(UserDTO pDTO) throws Exception;

    int deleteStorySessionByMember(UserDTO pDTO) throws Exception;

    int deleteStoryByMember(UserDTO pDTO) throws Exception;

    int deleteChildByMember(UserDTO pDTO) throws Exception;

    int deleteUser(UserDTO pDTO) throws Exception;
}
