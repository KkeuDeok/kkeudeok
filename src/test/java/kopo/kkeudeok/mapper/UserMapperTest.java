package kopo.kkeudeok.mapper;

import kopo.kkeudeok.dto.UserDTO;
import kopo.kkeudeok.util.EncryptUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
// Boot 4 에서 패키지가 옮겨졌다 — 3.x 의 org.springframework.boot.test.autoconfigure.jdbc 가 아니다
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 회원가입 → 로그인 → 아이디찾기 → 비밀번호 재설정까지 UserMapper.xml 의 SQL 을 실제로 돌려 본다.
 *
 * 이 테스트가 잡아 주는 것
 *  - namespace·id 오타 (기동은 되는데 호출하면 터지는 종류)
 *  - 암호화 규칙 어긋남: 넣을 때와 찾을 때 다른 방식으로 암호화하면 로그인이 안 된다.
 *    (합치기 전 두 사람이 서로 다른 해시를 써서 실제로 안 맞았다 — 2026-08-13)
 */
@MybatisTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:db/schema-h2.sql")
class UserMapperTest {

    @Autowired
    private IUserMapper userMapper;

    /** 매퍼가 안 돌려주는 컬럼(시각)을 직접 확인하려고 쓴다. */
    @Autowired
    private DataSource dataSource;

    private static final String LOGIN_ID = "pa1234";
    private static final String NAME = "김보호";
    private static final String RAW_EMAIL = "test.parent9@kkeudeok.local";
    private static final String RAW_PW = "kkeudeok1";

    /** 화면에서 받은 원문을 저장 규칙대로 바꿔 담는다 — AuthApiController 와 같은 방식이어야 한다. */
    private UserDTO signupDTO() throws Exception {
        UserDTO pDTO = new UserDTO();
        pDTO.setLoginId(LOGIN_ID);
        pDTO.setName(NAME);
        pDTO.setPassword(EncryptUtil.encHashSHA256(RAW_PW));
        pDTO.setEmail(EncryptUtil.encAES128CBC(RAW_EMAIL));
        // agree_*·notify_* 는 NOT NULL 이라 반드시 채워야 한다(안 채우면 INSERT 가 터진다)
        pDTO.setAgreeService(1);
        pDTO.setAgreePrivacy(1);
        pDTO.setAgreeSensitive(1);
        pDTO.setNotifyWeeklyReport(1);
        pDTO.setNotifyReminder(1);
        pDTO.setAgreeMarketing(0);
        // 시각은 DB 의 NOW() 가 아니라 앱이 넣는다(UserService.insertUser 와 같은 방식)
        LocalDateTime now = LocalDateTime.now();
        pDTO.setAgreedAt(now);
        pDTO.setCreatedAt(now);
        pDTO.setUpdatedAt(now);
        return pDTO;
    }

    @Test
    @DisplayName("회원가입 후 아이디·이메일 중복 확인이 Y 로 바뀐다")
    void insertUserThenExists() throws Exception {
        UserDTO pDTO = signupDTO();

        assertThat(userMapper.getLoginIdExists(pDTO).getExistsYn()).isEqualTo("N");
        assertThat(userMapper.getEmailExists(pDTO).getExistsYn()).isEqualTo("N");

        assertThat(userMapper.insertUser(pDTO)).isEqualTo(1);

        assertThat(userMapper.getLoginIdExists(pDTO).getExistsYn()).isEqualTo("Y");
        assertThat(userMapper.getEmailExists(pDTO).getExistsYn()).isEqualTo("Y");
    }

    /**
     * VM 의 MariaDB 시계가 실제보다 하루 넘게 뒤처져 있어(2026-08-13 확인) 가입 시각이
     * 과거로 찍혔다. SQL 에서 NOW() 를 쓰면 다시 그 시계를 읽게 되므로, 앱이 넘긴 값을
     * 그대로 쓰는지 못 박아 둔다. 현재 시각으로 검사하면 H2 의 NOW() 와 구분이 안 돼서
     * 일부러 한참 떨어진 시각을 넣어 본다.
     */
    @Test
    @DisplayName("가입 시각은 DB 의 NOW() 가 아니라 앱이 넘긴 값으로 저장된다")
    void timestampsComeFromApp() throws Exception {
        LocalDateTime marker = LocalDateTime.of(2020, 1, 2, 3, 4, 5);

        UserDTO pDTO = signupDTO();
        pDTO.setAgreedAt(marker);
        pDTO.setCreatedAt(marker);
        pDTO.setUpdatedAt(marker);
        userMapper.insertUser(pDTO);

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        for (String col : new String[]{"agreed_at", "created_at", "updated_at"}) {
            Timestamp saved = jdbc.queryForObject(
                    "SELECT " + col + " FROM member WHERE login_id = ?", Timestamp.class, LOGIN_ID);
            assertThat(saved).as(col).isNotNull();
            assertThat(saved.toLocalDateTime()).as(col).isEqualTo(marker);
        }
    }

    @Test
    @DisplayName("가입할 때 쓴 해시로 로그인이 된다 (해시 방식이 어긋나면 여기서 깨진다)")
    void loginWithSameHash() throws Exception {
        userMapper.insertUser(signupDTO());

        UserDTO pDTO = new UserDTO();
        pDTO.setLoginId(LOGIN_ID);
        pDTO.setPassword(EncryptUtil.encHashSHA256(RAW_PW));

        UserDTO rDTO = userMapper.getLogin(pDTO);

        assertThat(rDTO).isNotNull();
        assertThat(rDTO.getName()).isEqualTo(NAME);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 null 이다")
    void loginWithWrongPassword() throws Exception {
        userMapper.insertUser(signupDTO());

        UserDTO pDTO = new UserDTO();
        pDTO.setLoginId(LOGIN_ID);
        pDTO.setPassword(EncryptUtil.encHashSHA256("wrong-password"));

        assertThat(userMapper.getLogin(pDTO)).isNull();
    }

    /**
     * 보호자 PIN 은 로그인 직후 "최초 1회"를 판단하는 근거다.
     * getLogin 이 parent_pin 을 안 가져오면 이미 만든 사람도 매번 다시 만들게 된다.
     */
    @Test
    @DisplayName("보호자 PIN 을 저장하면 로그인 조회에 딸려 나온다")
    void updateParentPinThenReadBack() throws Exception {
        userMapper.insertUser(signupDTO());

        UserDTO login = new UserDTO();
        login.setLoginId(LOGIN_ID);
        login.setPassword(EncryptUtil.encHashSHA256(RAW_PW));

        UserDTO before = userMapper.getLogin(login);
        assertThat(before).isNotNull();
        assertThat(before.getParentPin()).as("가입 직후에는 PIN 이 없다").isNull();

        UserDTO upd = new UserDTO();
        upd.setMemberId(before.getMemberId());
        upd.setParentPin(EncryptUtil.encHashSHA256("1234"));
        upd.setUpdatedAt(LocalDateTime.now());

        assertThat(userMapper.updateParentPin(upd)).isEqualTo(1);

        UserDTO after = userMapper.getLogin(login);
        assertThat(after.getParentPin())
                .as("저장한 PIN 해시가 그대로 나와야 한다")
                .isEqualTo(EncryptUtil.encHashSHA256("1234"));
    }

    @Test
    @DisplayName("이름·이메일로 아이디를 찾는다 (이메일은 암호문으로 대조)")
    void findIdByNameAndEmail() throws Exception {
        userMapper.insertUser(signupDTO());

        UserDTO pDTO = new UserDTO();
        pDTO.setName(NAME);
        pDTO.setEmail(EncryptUtil.encAES128CBC(RAW_EMAIL));

        UserDTO rDTO = userMapper.getFindId(pDTO);

        assertThat(rDTO).isNotNull();
        assertThat(rDTO.getLoginId()).isEqualTo(LOGIN_ID);
    }

    @Test
    @DisplayName("이메일로 비밀번호 찾기 대상을 찾고, 새 비밀번호로 바꾸면 그 비밀번호로 로그인된다")
    void resetPasswordThenLogin() throws Exception {
        userMapper.insertUser(signupDTO());

        UserDTO find = new UserDTO();
        find.setEmail(EncryptUtil.encAES128CBC(RAW_EMAIL));

        UserDTO target = userMapper.getFindPwUser(find);
        assertThat(target).isNotNull();
        assertThat(target.getLoginId()).isEqualTo(LOGIN_ID);

        String newRawPw = "kkeudeok2";
        UserDTO upd = new UserDTO();
        upd.setLoginId(target.getLoginId());
        upd.setPassword(EncryptUtil.encHashSHA256(newRawPw));
        upd.setUpdatedAt(LocalDateTime.now());

        assertThat(userMapper.updatePassword(upd)).isEqualTo(1);

        // 옛 비밀번호는 막히고
        UserDTO oldTry = new UserDTO();
        oldTry.setLoginId(LOGIN_ID);
        oldTry.setPassword(EncryptUtil.encHashSHA256(RAW_PW));
        assertThat(userMapper.getLogin(oldTry)).isNull();

        // 새 비밀번호로는 들어가진다
        UserDTO newTry = new UserDTO();
        newTry.setLoginId(LOGIN_ID);
        newTry.setPassword(EncryptUtil.encHashSHA256(newRawPw));
        assertThat(userMapper.getLogin(newTry)).isNotNull();
    }
}
