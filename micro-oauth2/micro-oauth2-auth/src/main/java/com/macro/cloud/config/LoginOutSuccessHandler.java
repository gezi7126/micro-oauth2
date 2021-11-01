package com.macro.cloud.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginOutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

            //logger.info("开始执行退出逻辑===");
            // 获取Token
            //String accessToken = request.getHeader("Authorization");
            //accessToken = accessToken.replace("Bearer ", "");

            //logger.info("执行退出成功==");

    }
}
