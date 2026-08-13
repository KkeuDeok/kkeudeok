package kopo.kkeudeok.service;

import kopo.kkeudeok.dto.UserDTO;
import org.apache.catalina.User;

public interface IUserService {

    //로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기 // getlogin이 UserDTO를 요구로한다
    UserDTO getLogin(UserDTO pDTO) throws Exception;
    // 아이디 찾기를 위해서 이름이랑 비밀번호가 일치하는지 확인하는거 pDTO 맞는 아이디 정보를 돌려준다
    UserDTO getFindId(UserDTO pDTO) throws Exception;
     //어떻게 사용하는지를 적어놓은 것 이 메서드가 뭘 필요로하고 뭘 반환하는지
    // getFindId , getLogin 메서드가 -> 원하는것 UserDTo 를 원한다. 는게 적어져있음 => 가이드북 느낌?
    // 반환하는 것은 USerDTO
}
