package kopo.kkeudeok.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.kkeudeok.dto.MsgDTO;
import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.service.IUserService;
import kopo.kkeudeok.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.catalina.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Slf4j
@RequestMapping(value = "/user")
@RequiredArgsConstructor
@Controller
public class UserController {
    private final IUserService userService;

    //@GetMapping(value = "userRegForm")
//    public String userRegForm(){
//        log.info("{}.user/userRegForm",this.getClass().getName());
//        return "/user/userRegForm";
//    }

    @ResponseBody
    @PostMapping(value="getLoginIdExists")
    public UserDTO getUserExists(HttpServletRequest request) throws Exception{

        log.info("{}.getLoginIdExists Start!",this.getClass().getName());

        String loginId = kopo.poly.util.CmmUtil.nvl(request.getParameter("loginId"));

        log.info("loginId : {}",loginId);

        UserDTO pDTO = new UserDTO();
        pDTO.setLoginId(loginId);

        UserDTO rDTO = Optional.ofNullable(userService.getLoginIdExists(pDTO)).orElseGet(UserDTO::new);
        log.info("{}.getLoginIdExists End!", this.getClass().getName());
        return rDTO;
    }
    @ResponseBody
    @PostMapping(value = "getEmailExists")
    public UserDTO getEmailExists(HttpServletRequest request) throws Exception {
        log.info("{}.getEmailExists Strat!", this.getClass().getName());
        String email = kopo.poly.util.CmmUtil.nvl(request.getParameter("email"));
        log.info("email : {}", email);

        UserDTO pDTO = new UserDTO();
        pDTO.setEmail(EncryptUtil.encAES128CBC(email));
        UserDTO rDTO = Optional.ofNullable(userService.getEmailExists(pDTO)).orElseGet(UserDTO::new);
        log.info("{}.getEmailExists End!",this.getClass().getName());
        return rDTO;
    }
 //여긴 Msg쪽 만들어야함 ㅇㅇ
    @ResponseBody
    @PostMapping(value = "insertUser")
    public MsgDTO insertUser(HttpServletRequest request) {
        log.info("{}.insertUserInfo start!", this.getClass().getName());

        int res = 0; //회원가입 결과
        String msg = "";//회원 가입 결과에 대한 메시지 전달할 변수
        MsgDTO dto; //결과 메시지 구조
        //웹(회원정보 입력 화면)에서 받는 정보를 저장할 변수
        UserDTO pDTO;

        try {
            /*
             * ####################################
             * 웹(회원정보 입력화면)에서 받는 정보를 String변수에 저장 시작
             * 무조건 웹으로 받은 정보는 DTO에 저장하기 위해 임시로 String 변수에 저장
             * #######################
             */
            String loginId = kopo.poly.util.CmmUtil.nvl(request.getParameter("loginId")); //아이디
            String name = kopo.poly.util.CmmUtil.nvl(request.getParameter("name"));//이름
            String password = kopo.poly.util.CmmUtil.nvl(request.getParameter("password"));//비번
            String email = kopo.poly.util.CmmUtil.nvl(request.getParameter("email"));//이메일
//            String addr1 = kopo.poly.util.CmmUtil.nvl(request.getParameter("addr1"));//주소
//            String addr2 = kopo.poly.util.CmmUtil.nvl(request.getParameter("addr2"));//상세주소
            /*
             * ###################
             * 웹(회원정보 입력화면)에서 받는 정보를 String 변수에 저장 끝
             * 무조건 웹으로 받은 정보는 DTO에 저장하기 위해 임시 String 변수에 저장
             * ######################
             */
            /*
             * ############
             * 반드시 값을 받으면 꼭 로그를 찍어서 값이 제대로 들어오는지 파악 해야함
             * 반드시 작성
             * ##########
             */
            log.info("loginId :" + loginId);
            log.info("name :" + name);
            log.info("password : " + password);
            log.info("email : " + email);
//            log.info("addr1 :" + addr1);
//            log.info("addr2 : " + addr2);
            /*
             * ##############
             * 웹 (회원 정보 입력화면)에서 받는 정보를  DTO에 저장 하기 시작!
             * 무조건 웹으로 받은 정보는 DTO에 저장해야한다고 이해하길 권함
             * ##################
             */
            //웹(회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
            pDTO = new UserDTO();

            pDTO.setLoginId(loginId);
            pDTO.setName(name);

            //비번는 절대로 복호화 되지 않도록 해시 알고리즘으로 암호화
            pDTO.setPassword(EncryptUtil.encHashSHA256(password));

            //민감 정보인 이메일은 AES128-CBC로 암호화
            pDTO.setEmail(EncryptUtil.encAES128CBC(email));
//            pDTO.setAddr1(addr1);
//            pDTO.setAddr2(addr2);
            /*
             * #########################################
             * 웹(회원 정보 입력화면)에서 받는 정보를 DTO에 저장하기끝
             * 무조건 웹으로 받은 정보는 DTO에 저장해야한다고 이해하길 권장
             * ##########################################
             */
            //회원 가입
            res = userService.insertUser(pDTO);
            log.info("회원 가입 결과(res) : " + res);
            if (res == 1) {
                msg = "회원 가입 되었습니다";
                //추후 회원 가입 입력화면에서 ajax를 활용해서 아이디 중복, 이메일 중복을 체크하길 바람
            } else if (res == 2) {
                msg = "이미 가입된 아이디입니다";
            } else {
                msg = "오류로 인해 회원가입 실패";
            }
        } catch (Exception e) {
            //저장 실패되면 사용자에게 보여줄 메시지
            msg = "실패하였습니다 : " + e;
            log.info(e.toString());
        } finally {
            //결과 메시지 전달하기
            dto = new MsgDTO();
            dto.setResult(res);
            dto.setMsg(msg);

            log.info("{}.insertUserInfo End", this.getClass().getName());
        }
        return dto;
      }

    /**
     * 중요
     * searchPassword
     * ########################################################################
     *  이거 중요함 이거는 인터페이스랑 연결해야해서 나 혼자 못함
     * ########################################################################
     *
     */
    @GetMapping(value = "searchPassword")
    public String searchPassword(HttpSession session) {
        log.info("{}.searchPassword Start!", this.getClass().getName());
        //강제 URL입력등 오는 경우가 있어서 세션삭제
        //비밀번호 재생성하는 화면은 보안을 위해 생성한 NEW_PASSWORD 세션 삭제
        session.setAttribute("NEW_PASSWORD", "");
        session.removeAttribute("NEW_PASSWORD");
        log.info("{}.searchPassword End!", this.getClass().getName());
        return "user/searchPassword";
    }
    /**
     * 비밀번호 찾기 로직 수행
     * <p>
     * 아이디 이름 이메일일치하면 비밀번호 재발급 화면
     */
    @PostMapping(value = "searchPasswordProc")
    public String searchPasswordProc(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {
        /**
         * #######################
         * 웹(회원 정보 입력화면)에서 받는 정보를 String 변수에저장
         * 무조건 웹으로 받은 정보는 dTO에 저장하기 위해 임시로 String 변수에 저장
         * ###############
         */
        String userId = kopo.poly.util.CmmUtil.nvl(request.getParameter("userId"));// 아이디
        String userName = kopo.poly.util.CmmUtil.nvl(request.getParameter("userName"));// 이름
        String email = kopo.poly.util.CmmUtil.nvl(request.getParameter("email"));//이메일
        /*
        ######################
        반드시 값을 받았으면 꼭 로그를 찍어서 값이 제대호 들어오는지 파악해야함
        반드시 작성
         */
        log.info("userId : {} / userName : {} / email : {}", userId, userName, email);
        /*
        ################################
        웹(호원 정보 입력화면)에서 받는 정보를 DTO에 저장하기
        무조건 웹으로 받은 정보는 DTO에ㅐ 저장해야한다고 이해하길 권함
         */
        UserDTO pDTO = new UserDTO();
        pDTO.setLoginId(userId);
        pDTO.setName(userName);
        pDTO.setEmail(EncryptUtil.encAES128CBC(email));
        //비밀번호 찾기 가능한지 확인
        UserDTO rDTO = Optional.ofNullable(userService.getLoginIdExists(pDTO)).orElseGet(UserDTO::new);
        model.addAttribute("rDTO", rDTO);
        //비밀번호 재생성하는 화면은 보안을 위해 반드시 NEW_PASSWORD 세션이 존재해야 접속 가능 하도록 구현
        //userId 값을 넣은 이유는 비밀 번호 재설정하는 newPasswordProc 함수에서 사용하기 위한
        session.setAttribute("NEW_PASSWORD", userId);
        log.info("{}.searchPasswordProc End!", this.getClass().getName());
        return "user/newPassword";
    }

    /*
    비밀 번호 찾기 로직
    <p>
    아이디 이름 이메일 일치하면 비밀 번호 재발급 화면 이동
     */
    @PostMapping(value = "newPasswordProc")
    public String newPasswordProc(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {
        log.info("{}.user/newPassword Start!", this.getClass().getName());
        String msg; //웹에서 보여줄 메시지
        //정상 접급인지 체크
        String newPassword = kopo.poly.util.CmmUtil.nvl((String) session.getAttribute("NEW_PASSWORD"));
        if (!newPassword.isEmpty()) { //정상 접근
            /**
             * ####################
             * 웹(회원정보 입력화면)에서 받는 정보를 String변수에 저장
             * 무조건 웹으로 받은 정보는 DTO에 저장하기위해 임시 String 변수에 저장
             * ########################
             */
            String password = kopo.poly.util.CmmUtil.nvl(request.getParameter("password")); //신규 비밀번호
            /*
            ###############
            반드시 값을 받았으면 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함
            반드시 작성
            ###############
             */
            log.info("password : {}", password);
            /*
            ###########################
            웹(회원정보 입력정보)에서 받는 정보를 DTO에 저장하기
            무조건 웹으로 받은 정보는 DTO에 저장히기를 권함
             */
            UserDTO pDTO = new UserDTO();
            pDTO.setLoginId(newPassword);
            pDTO.setPassword(EncryptUtil.encHashSHA256(password));

            userService.newPasswordProc(pDTO);
            //비밀번호 재생성하는 화면은 보안을 위해 생선한 NEW_PASSWORD세션 삭제
            session.setAttribute("NEW_PASSWORD", "");
            session.removeAttribute("NEW_PASSWORD");
            msg = "비밀번호가 재설정 되었습니다";
        } else {//비정상 접근
            msg = "비정상 접근";
        }
        model.addAttribute("msg", msg);
        log.info("{}.user/newPasswordProc End!", this.getClass().getName());
        return "user/newPasswordResult";
    }
}
