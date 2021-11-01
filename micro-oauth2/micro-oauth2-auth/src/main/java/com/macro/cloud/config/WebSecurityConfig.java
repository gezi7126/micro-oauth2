package com.macro.cloud.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * SpringSecurity配置
 * Created by macro on 2020/6/19.
 */
@EnableGlobalMethodSecurity(prePostEnabled = true,securedEnabled = true)
@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    //认证管理器
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //设置退出登录时访问路径及跳转到那个页面
        http.logout().permitAll()
                //.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout","POST"))// 用户退出所访问的路径，需要使用Post方式
                //.logoutUrl("/logout")
                .addLogoutHandler(getLogoutHandler()).logoutSuccessHandler(getLogoutSuccessHandler());

        //设置没有权限访问时跳转到自定义页面
        /*http.exceptionHandling().accessDeniedPage("/403.html");
        http.formLogin()
                //.loginPage("/login.html")//配置登录頁面
                .loginPage("/crsf/test")
                //.loginProcessingUrl("/user/login")//配置登录页面点击登录按钮后访问的路径
                .loginProcessingUrl("/crsf/update_token")
                .usernameParameter("username").passwordParameter("password")  //username和password对应前端表单的name键
                //.defaultSuccessUrl("/user/index",true)//设置成功后跳转的路径，可以是/
                .successForwardUrl("/user/index")//设置成功后跳转的路径，不能是/,而且请求必须是post
                //自定义登录成功处理逻辑
                // .successHandler(new MyAuthSuccessHandler())
                .failureUrl("/user/login?error").permitAll()//登录成功后访问的路径*/
        http.formLogin()
                .and()
                .authorizeRequests()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .antMatchers("/rsa/publicKey").permitAll()
                .antMatchers("/oauth/test").permitAll()
                .antMatchers("/logout/**").permitAll()
                .anyRequest().authenticated()
                .and().csrf().disable();//关闭csrf,否则验证通过后页面不跳转;
    }

    //退出登录处理业务类
    @Bean
    public LogoutHandler getLogoutHandler(){
        LogoutHandler myLogoutHandler = new LoginOutHandler();
        return myLogoutHandler;
    }

    //成功退出后回调业务处理类
    @Bean
    public LogoutSuccessHandler getLogoutSuccessHandler(){
        LogoutSuccessHandler logoutSuccessHandler = new LoginOutSuccessHandler();
        return logoutSuccessHandler;
    }



}
