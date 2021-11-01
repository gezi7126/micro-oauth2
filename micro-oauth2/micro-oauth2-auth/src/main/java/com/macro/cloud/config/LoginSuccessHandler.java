package com.macro.cloud.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    /**
     * 创建token
     * @param request
     * @param authentication
     */
    private Map<String, String> createToken(HttpServletRequest request, Authentication authentication){

        return null;
    }

}
