package kopo.kkeudeok.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정 — 만든 인터셉터를 '어느 주소에' 걸지 정한다.
 * 인터셉터는 클래스만 만들어 두면 아무 일도 안 한다. 여기 등록해야 동작한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 마이페이지 화면 전체(게이트·PIN 재설정·회원정보·아동 프로필·캐릭터)만 막는다.
        // ⚠ 대시보드·학습홈·리포트는 일부러 뺐다 — 시연·검증 스크립트가 로그인 없이 바로
        //   그 주소로 들어가기 때문이다. 그쪽까지 막을지는 팀장과 정할 것.
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/mypage", "/mypage/**");
    }
}
