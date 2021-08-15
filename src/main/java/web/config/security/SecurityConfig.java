package web.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import web.config.handler.AuthenticationFailureHandler;
import web.config.handler.AuthenticationSuccessHandler;
import web.repository.RoleRepositoryImpl;
import web.service.AppService;

@EnableWebSecurity(debug = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // сервис, с помощью которого тащим пользователя
    private final AppService appService;

    // класс, в котором описана логика перенаправления пользователей по ролям
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    // класс, в котором описана логика при неудачной авторизации
    private final AuthenticationFailureHandler authenticationFailureHandler;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(AppService appService,
                          AuthenticationSuccessHandler authenticationSuccessHandler,
                          AuthenticationFailureHandler authenticationFailureHandler,
                          PasswordEncoder passwordEncoder) {
        this.appService = appService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.passwordEncoder = passwordEncoder;
       }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        // конфигурация для прохождения аутентификации
        auth.userDetailsService(appService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() //выключаем кроссдоменную секьюрность
                .authorizeRequests(authorize -> authorize
                        .antMatchers("/").permitAll()
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                );

        http.formLogin()
                .permitAll()  // даем доступ к форме логина всем
                .successHandler(authenticationSuccessHandler) //указываем логику обработки при удачном логине
                .failureHandler(authenticationFailureHandler) //указываем логику обработки при неудачном логине
                .usernameParameter("email"); // Указываем параметры логина с формы логина


        http.logout()
                .permitAll() // разрешаем делать логаут всем
                .logoutUrl("/logout")
                .clearAuthentication(true)
                .invalidateHttpSession(true) // сделать невалидной текущую сессию
                .and().formLogin(); // указываем URL при удачном логауте
    }
}