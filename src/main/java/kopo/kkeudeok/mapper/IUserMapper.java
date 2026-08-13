package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserMapper {
    //메퍼는 인퍼테이스 밖에 없어 자바에서는 얘도 결국ㄱ에는 가이드라인인거임 이거 필요한 틀 => 진짜 필요한 구체화 실체 이런건 매퍼
    // xml에 있다
    // 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기
    UserDTO getLogin(UserDTO pDTO) throws Exception;

    UserDTO getFindId(UserDTO pDTO) throws Exception;
}
