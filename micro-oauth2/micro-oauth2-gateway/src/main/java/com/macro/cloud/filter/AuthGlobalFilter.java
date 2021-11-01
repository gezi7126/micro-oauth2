package com.macro.cloud.filter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.macro.cloud.api.EncryptUtil;
import com.macro.cloud.component.RestAuthenticationEntryPoint;
import com.macro.cloud.constant.RedisConstant;
import com.nimbusds.jose.JWSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 将登录用户的JWT转化成用户信息的全局过滤器
 * Created by macro on 2020/6/17.
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static Logger LOGGER = LoggerFactory.getLogger(AuthGlobalFilter.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //以/auth/**访问，没有token也放行
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StrUtil.isEmpty(token)) {
            return chain.filter(exchange);
        }
        try {
            //从token中解析用户信息并设置到Header中去
            String realToken = token.replace("Bearer ", "");
            JWSObject jwsObject = JWSObject.parse(realToken);
            String userStr = jwsObject.getPayload().toString();
            LOGGER.info("AuthGlobalFilter.filter() user:{}",userStr);
            ServerHttpRequest request = exchange.getRequest().mutate().header("user", userStr).build();
            exchange = exchange.mutate().request(request).build();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return chain.filter(exchange);
    }

    //@Override
    public Mono<Void> filteres(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUrl = exchange.getRequest().getPath().value();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        //1 auth服务所有放行
        if (pathMatcher.match("/auth/**", requestUrl)) {
            return chain.filter(exchange);
        }

        //2 检查token是否存在
        String token = getToken(exchange);
        if (StrUtil.isBlank(token)) {
            return noTokenMono(exchange);
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof OAuth2Authentication)) { // 无token访问网关内资源的情况，目前仅有uua服务直接暴露
                return null;
            }
            OAuth2Authentication oauth2Authentication = (OAuth2Authentication) authentication;
            Authentication userAuthentication = oauth2Authentication.getUserAuthentication();
            Object principal = userAuthentication.getPrincipal();

            /**
             * 2.组装明文token，转发给微服务，放入header，名称为user
             */

            List<String> authorities = userAuthentication.getAuthorities().stream().map(s -> {
                return s.getAuthority();
            }).collect(Collectors.toList());

            OAuth2Request oAuth2Request = oauth2Authentication.getOAuth2Request();
            Map<String, String> requestParameters = oAuth2Request.getRequestParameters();
            Map<String, Object> jsonToken = new HashMap<>(requestParameters);
            if (userAuthentication != null) {
                jsonToken.put("principal", userAuthentication.getName());
                jsonToken.put("authorities", authorities);
            }

            //给header里面添加值
            String base64 = EncryptUtil.encodeUTF8StringBase64(JSON.toJSONString(jsonToken));
            ServerHttpRequest tokenRequest = exchange.getRequest().mutate().header("user", base64).build();
            ServerWebExchange build = exchange.mutate().request(tokenRequest).build();
            return chain.filter(build);
        } catch (InvalidTokenException e) {
            //log.info("无效的token: {}", token);
            return invalidTokenMono(exchange);
        }

    }



    /**
     * 获取token
     */

    private String getToken(ServerWebExchange exchange) {
        String tokenStr = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StrUtil.isBlank(tokenStr)) {
            return null;
        }
        String token = tokenStr.split(" ")[1];
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return token;
    }



    /**
     * 无效的token
     */

    private Mono<Void> invalidTokenMono(ServerWebExchange exchange) {
        JSONObject json = new JSONObject();
        json.put("status", HttpStatus.UNAUTHORIZED.value());
        json.put("data", "无效的token");
        return buildReturnMono(json, exchange);
    }

    private Mono<Void> noTokenMono(ServerWebExchange exchange) {
        JSONObject json = new JSONObject();
        json.put("status", HttpStatus.UNAUTHORIZED.value());
        json.put("data", "没有token");
        return buildReturnMono(json, exchange);
    }


    private Mono<Void> buildReturnMono(JSONObject json, ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        byte[] bits = json.toJSONString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "text/plain;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
