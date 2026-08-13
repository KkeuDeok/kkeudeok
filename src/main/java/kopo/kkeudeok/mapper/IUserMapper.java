package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserMapper {
    int insertUser(UserDTO pDTO) throws Exception;
    UserDTO getLoginIdExists(UserDTO pDTO) throws Exception;
    UserDTO getEmailExists(UserDTO pDTO) throws Exception;

    //비번 재설정
    int updatePassword(UserDTO pDTO) throws Exception;
}
