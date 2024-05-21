package cholog.config;

import cholog.ui.AuthenticationPrincipalArgumentResolver;
import cholog.ui.CheckLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    // 스프링은 WebMvcConfigurer 라는 인터페이스 제공하여, 애플리케이션 개발자가 쉽게 MVC 설정을 커스터마이징할 수 있도록 한다.

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 특정 요청에 대해 뷰를 응답한다.
        // Q. [더 생각해보기] WebMvcConfigurer 를 사용하는 방식과 직접 Controller 를 작성하는 방식 중 어떤 방식이 더 좋을까요? 그렇게 생각하는 이유는 무엇인가요?
        // A. (공식 문서) It is recommended to avoid splitting URL handling across an annotated controller and a view controller.
        // A. (나의 의견) Controller 를 직접 작성할 때 더 다양한 응답을 처리할 수 있다고 생각한다.

        // TODO: "/" 요청 시 hello.html 페이지 응답하기
        registry.addViewController("/").setViewName("hello");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 주로 HTTP 요청의 사전 처리와 사후 처리를 관리하는 데 사용되는 컴포넌트이다.
        // Q. 스프링이라는 프레임워크가 HandlerInterceptor 인터페이스를 제공하는 이유는 무엇일까요? 어떤 상황에서 Interceptor 를 사용할 수 있을까요?
        // A. 웹 요청의 처리 과정에서 특정 로직을 실행하거나 요청을 가로채고 제어하기 위함? HTTP 요청과 응답 사이에서 중복으로 발생할 수 있는 로직을 손쉽게 구현할 수 있도록 하기 위함?
        // A. 로그인 상태를 체크하는 로직, 로깅 등과 같이, 특정 로직이 반복되는 경우 사용하면 좋을 듯 하다.

        // TODO: "/admin/**" 요청 시 LoginInterceptor 동작하게 하기
        registry.addInterceptor(new CheckLoginInterceptor()).addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 요청 데이터를 메서드의 매개변수로 변환할 때 사용하는 전략 인터페이스이다.
        // 컨트롤러의 메서드가 호출될 때 매개변수에 전달할 객체를 생성하거나 조작하는 로직을 구현할 수 있다.

        // TODO: AuthenticationPrincipalArgumentResolver 등록하기
        resolvers.add(new AuthenticationPrincipalArgumentResolver());
    }
}
