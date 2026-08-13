package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * member 테이블 접근. 실제 SQL 은 resources/mapper/UserMapper.xml 에 있다.
 * ⚠ XML 의 namespace 와 이 인터페이스의 전체 이름이 같아야 하고,
 *   여기 선언한 메서드는 XML 에 같은 id 로 전부 있어야 한다(하나라도 없으면 기동 시 바인딩 실패).
 */
@Mapper
public interface IUserMapper {

    /* ---------- 회원가입 (팀원1) ---------- */

    /** 회원 저장 */
    int insertUser(UserDTO pDTO) throws Exception;

    /** 아이디 중복 확인 — EXISTS_YN 만 담아 돌려준다 */
    UserDTO getLoginIdExists(UserDTO pDTO) throws Exception;

    /** 이메일 중복 확인 — EXISTS_YN 만 담아 돌려준다 */
    UserDTO getEmailExists(UserDTO pDTO) throws Exception;

    /* ---------- 로그인 · 아이디 찾기 (팀원2) ---------- */

    /** 아이디·비밀번호가 맞는 회원 1명. 없으면 null */
    UserDTO getLogin(UserDTO pDTO) throws Exception;

    /** 이름·이메일이 맞는 회원 1명. 없으면 null */
    UserDTO getFindId(UserDTO pDTO) throws Exception;

    /* ---------- 비밀번호 찾기 ---------- */

    /** 아이디·이름·이메일이 모두 맞는 회원 1명. 없으면 null */
    UserDTO getFindPwUser(UserDTO pDTO) throws Exception;

    /** 비밀번호 재설정 */
    int updatePassword(UserDTO pDTO) throws Exception;
}
