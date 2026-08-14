package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.UserDTO;

public interface IUserService {

    /* ---------- 회원가입 ---------- */

    /** 아이디 중복 확인 */
    UserDTO getLoginIdExists(UserDTO pDTO) throws Exception;

    /** 이메일 중복 확인 */
    UserDTO getEmailExists(UserDTO pDTO) throws Exception;

    /** 회원가입 — 성공 1 / 실패 0 */
    int insertUser(UserDTO pDTO) throws Exception;

    /* ---------- 로그인 · 아이디 찾기 ---------- */

    /** 아이디·비밀번호가 맞는 회원. 없으면 null */
    UserDTO getLogin(UserDTO pDTO) throws Exception;

    /** 이름·이메일이 맞는 회원. 없으면 null */
    UserDTO getFindId(UserDTO pDTO) throws Exception;

    /* ---------- 비밀번호 찾기 ---------- */

    /** 아이디·이름·이메일이 모두 맞는 회원. 없으면 null */
    UserDTO getFindPwUser(UserDTO pDTO) throws Exception;

    /** 비밀번호 재설정 — 바뀐 행 수 */
    int newPasswordProc(UserDTO pDTO) throws Exception;

    /* ---------- 최초 1회 설정 (dev) ---------- */

    /** 보호자 PIN 저장 — 바뀐 행 수 */
    int updateParentPin(UserDTO pDTO) throws Exception;

    /** 이 보호자가 아이를 등록했는지(= 온보딩을 마쳤는지) */
    boolean hasChild(Long memberId) throws Exception;

    /* ---------- 마이페이지 ---------- */

    /**
     * 회원정보 화면용 한 명. 없으면 null.
     * 이메일은 DB 에 AES 암호문으로 있어 여기서 복호화해 돌려준다(화면은 사람이 읽는 값을 받는다).
     */
    UserDTO getUserInfo(UserDTO pDTO) throws Exception;

    /** 회원정보 저장(이름·휴대폰·관계) — 바뀐 행 수 */
    int updateUserInfo(UserDTO pDTO) throws Exception;

    /**
     * 계정 완전 삭제 — 아동·학습 기록까지 한 트랜잭션으로 지운다. 되돌릴 수 없다.
     *
     * @param pDTO memberId 만 있으면 된다
     * @return 지워진 member 행 수. 0 이면 이미 없는 계정
     */
    int deleteUser(UserDTO pDTO) throws Exception;
}
