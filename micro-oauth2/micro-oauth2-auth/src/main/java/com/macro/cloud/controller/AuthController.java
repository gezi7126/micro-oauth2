package com.macro.cloud.controller;

import com.macro.cloud.api.CommonResult;
import com.macro.cloud.constant.RedisConstant;
import com.macro.cloud.domain.Oauth2TokenDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 自定义Oauth2获取令牌接口
 * Created by macro on 2020/7/17.
 */
@RestController
@RequestMapping("/oauth")
public class AuthController {

    @Autowired
    private TokenEndpoint tokenEndpoint;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * Oauth2登录认证
     */
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public CommonResult<Oauth2TokenDto> postAccessToken(Principal principal, @RequestParam Map<String, String> parameters) throws HttpRequestMethodNotSupportedException {
        OAuth2AccessToken oAuth2AccessToken = tokenEndpoint.postAccessToken(principal, parameters).getBody();
        Oauth2TokenDto oauth2TokenDto = Oauth2TokenDto.builder()
                .token(oAuth2AccessToken.getValue())
                .refreshToken(oAuth2AccessToken.getRefreshToken().getValue())
                .expiresIn(oAuth2AccessToken.getExpiresIn())
                .tokenHead("Bearer ").build();
       //存放token
        if(redisTemplate.opsForValue().get(RedisConstant.JWT_TOKEN)!=null){
            redisTemplate.delete(RedisConstant.JWT_TOKEN);
        }
        //set(K key, V value, long timeout, TimeUnit unit)
        redisTemplate.opsForValue().set(RedisConstant.JWT_TOKEN,oAuth2AccessToken.getValue(),60*60, TimeUnit.SECONDS);
        return CommonResult.success(oauth2TokenDto);
    }

    @GetMapping("/test")
    public String getStr(){
        return "test login...";
    }

}
