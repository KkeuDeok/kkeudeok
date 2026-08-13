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
}
