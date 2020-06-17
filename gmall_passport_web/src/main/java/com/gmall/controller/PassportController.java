package com.gmall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.user.UserInfo;
import com.gmall.service.UserService;
import com.gmall.util.JwtUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PassportController {

    @Reference
    UserService userService;

    @GetMapping("/index")
    public String index(@RequestParam("originUrl") String originUrl, Model model) {
        model.addAttribute("originUrl", originUrl);
        return "index";
    }


    String JWTKEY = "HAHA";

    @PostMapping("/login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request) {
        //deal with database, so go into service

        //check if the user is valid
        UserInfo userInfoEx = userService.login(userInfo);

        if (userInfoEx != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userInfoEx.getId());
            map.put("nickName", userInfoEx.getNickName());
            //request.getRemoteAddr()  //如果有nginx,需要从nginx里面取出用户真实信息

            String header = request.getHeader("X-forwarded-for");

            String token = JwtUtil.encode(JWTKEY, map, header);
            return token;
        }
        return "failed";
    }

    //redirect/forward, 验证系统来访问这个method
    @GetMapping("verify")
    @ResponseBody
    public String verify(@RequestParam("token") String token,
                         @RequestParam("currentIp") String currentIp) {
        Map<String, Object> userMap = JwtUtil.decode(token, JWTKEY, currentIp);
        if(userMap!=null){
            String userId = (String) userMap.get("userId");
            Boolean isLogin = userService.verify(userId);

            if(isLogin){
                return "success";
            }
        }

        return "fail";

    }
}
