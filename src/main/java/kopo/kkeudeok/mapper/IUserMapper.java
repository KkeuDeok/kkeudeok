package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;

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

    /* ---------- 최초 1회 설정 (dev) ---------- */

    /** 보호자 PIN 설정(최초 1회) */
    int updateParentPin(UserDTO pDTO) throws Exception;

    /* ---------- 마이페이지 ---------- */

    /** 회원정보 화면에 뿌릴 한 명. 비밀번호 없이 login_id 로만 찾는다(이미 로그인한 본인) */
    UserDTO getUserInfo(UserDTO pDTO) throws Exception;

    /** 회원정보 저장 — 이름·휴대폰·관계만. 아이디·이메일·비밀번호는 여기서 안 건드린다 */
    int updateUserInfo(UserDTO pDTO) throws Exception;

    /* ---------- 마이페이지: 계정 완전 삭제 ----------
       외래키 때문에 반드시 아래(자식)부터 지워야 한다. 순서를 바꾸면 FK 위반으로 실패한다.
       네 개를 한 트랜잭션으로 묶는 건 UserService.deleteUser 가 한다. */

    /** 1. 학습 세션 — mission_log 는 CASCADE 로 같이 지워진다 */
    int deleteStorySessionByMember(UserDTO pDTO) throws Exception;

    /** 2. 이야기 — story_node 는 CASCADE. story_session 이 먼저 없어야 지울 수 있다 */
    int deleteStoryByMember(UserDTO pDTO) throws Exception;

    /** 3. 아동 — checklist·roadmap·감정통계·스냅샷·표정보정은 CASCADE 로 같이 지워진다 */
    int deleteChildByMember(UserDTO pDTO) throws Exception;

    /** 4. 회원 본인 */
    int deleteUser(UserDTO pDTO) throws Exception;
}
