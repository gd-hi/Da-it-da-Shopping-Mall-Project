package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    MemberService memberService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.authorizeRequests(auth -> auth.requestMatchers("/", "/members/**", "/item/**" , "/images/**", "/error", "/map", "/load-more", "/Da-it-da-api-docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // 누구나 접근 가능한 페이지
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/item/**", "/notice/**", "/sortItems", "/websocket","/websocket/**").permitAll() // static 폴더 안에 있는 /css, /js, /img 하위 모든 파일은 인증 무시
                        .requestMatchers("/chat", "/chattingRoomList", "/chattingRoom", "/chattingRoom-enter", "/chattingRoom-exit", "/socket/**", "/topic/**").permitAll()
                        .requestMatchers("/members/memberDeleteForm","/members/memberEditForm", "/members/update", "/members/delete").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // ADMIN만 접근 가능한 페이지
                        .anyRequest().authenticated())
                // 로그인
                .formLogin(formLogin -> formLogin
                        .loginPage("/members/login")
                        .defaultSuccessUrl("/")
                        .usernameParameter("email")
                        .failureUrl("/members/login/error"))
                // 로그아웃
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                        .logoutSuccessUrl("/"))
                .oauth2Login(oauth2Login -> oauth2Login
                        .defaultSuccessUrl("/")
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService)))
                // 자동 로그인
                .rememberMe(remember -> remember
                        .key("rememberKey") // token 생성용 키값 (필수)
                        .rememberMeCookieName("rememberMeCookieName") // 쿠키 이름
                        .rememberMeParameter("remember-me") //파라미터
                        .tokenValiditySeconds(60 * 60 * 24 * 7) // 토큰 유지 기간
                        .userDetailsService(userDetailsService))
        ;

        http.csrf(csrf -> csrf.ignoringRequestMatchers(
                "/item/**","/notice/**", "/admin/item/**", "/cart/**", "/order/**"
                , "/cartItem/**", "/orders/**", "/assets/**", "/favicon.ico", "/items/**", "/sortItems", "/websocket/**", "/websocket",
                "/chat", "/chattingRoomList", "/chattingRoom", "/chattingRoom-enter", "/chattingRoom-exit", "/socket/**", "/topic/**"
        ).csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));


        http.exceptionHandling(exceptHand -> exceptHand
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint()));


        return  http.build();

    }

    // 비밀번호 암호화
    @Bean
    public static PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }




    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder());
    }

}
