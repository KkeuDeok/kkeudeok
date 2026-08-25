package kopo.kkeudeok.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ChildSessionInterceptor childSessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/", "/login", "/loginProc", "/logoutProc",
                        "/signup/**", "/signupProc", "/checkLoginIdProc", "/sendAuthCodeProc",
                        "/find-id", "/find-id/**", "/findIdProc",
                        "/find-pw", "/find-pw/**", "/findPwProc", "/newPasswordProc",
                        "/css/**", "/js/**", "/img/**", "/favicon.ico", "/error");

        registry.addInterceptor(childSessionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/favicon.ico", "/error");
    }
}
