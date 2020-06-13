package com.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.gmall.config.LoginRequire;
import com.gmall.util.CookieUtil;
import com.gmall.util.HttpClientUtil;
import com.gmall.util.WebConst;
import io.jsonwebtoken.impl.Base64UrlCodec;
import javassist.util.proxy.MethodHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public static final int COOKIE_MAXAGE = 7 * 24 * 3600;

    public static final String VERIFY_ADDRESS = "http://passport.gmall.com/verify";

    public static final String LOGIN_ADDRESS = "http://passport.gmall.com/index";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //return super.preHandle(request, response, handler);
        String token = null;
        //new token
        token = request.getParameter("newToken");
        if (token != null) {
            //save into cookie, because it's first time
            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        } else {
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        Map userMap = new HashMap();
        if (token != null) {
            userMap = getUserMapFromToken(token);
            String nickName = (String) userMap.get("nickName");
            request.setAttribute("nickName", nickName);
        }
        //old token, already has info, token in cookie
        //不一定每次都发送认证，但是每次都拦截看一下token

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire loginRequire = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (loginRequire != null) {
            if (token != null) {
                String currentIp = request.getHeader("X-forwarded-for");
                String result = HttpClientUtil.doGet(VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + currentIp);
                if ("success".equals(result)) {
                    return true;
                } else if (!loginRequire.autoRedirect()) {
                    return true;
                } else {
                    String requestURL = request.getRequestURI().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(LOGIN_ADDRESS + "?originUrl=" + encodeURL);
                }
            } else {
                String requestURL = request.getRequestURI().toString();
                String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                response.sendRedirect(LOGIN_ADDRESS + "?originUrl=" + encodeURL);
            }
        }


        return true;
    }

    private Map getUserMapFromToken(String token) {
        //
        String userBase64 = StringUtils.substringBetween(token, ".");

        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode(userBase64);

        String userJson = new String(decode);
        return JSON.parseObject(userJson, Map.class);

    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }


}
