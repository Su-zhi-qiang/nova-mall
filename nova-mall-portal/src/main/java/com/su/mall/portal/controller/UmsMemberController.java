package com.su.mall.portal.controller;

import com.su.mall.common.api.CommonResult;
import com.su.mall.model.UmsMember;
import com.su.mall.portal.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员管理Controller
 * @author Su
 */
@RestController
@Tag(name = "UmsMemberController", description = "会员登录注册管理")
@RequestMapping("/sso")
@RequiredArgsConstructor
public class UmsMemberController {
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    private final UmsMemberService memberService;

    @Operation(summary = "会员注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public CommonResult<Void> register(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String telephone,
                                 @RequestParam String authCode) {
        memberService.register(username, password, telephone, authCode);
        return CommonResult.success(null,"注册成功");
    }

    @Operation(summary = "会员登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public CommonResult<Map<String, String>> login(@RequestParam String username,
                              @RequestParam String password) {
        String token = memberService.login(username, password);
        if (token == null) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }

    @Operation(summary = "获取会员信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<UmsMember> info(Principal principal) {
        if(principal==null){
            return CommonResult.unauthorized(null);
        }
        UmsMember member = memberService.getCurrentMember();
        return CommonResult.success(member);
    }

    @Operation(summary = "获取验证码")
    @RequestMapping(value = "/getAuthCode", method = RequestMethod.GET)
    public CommonResult<String> getAuthCode(@RequestParam String telephone) {
        String authCode = memberService.generateAuthCode(telephone);
        return CommonResult.success(authCode,"获取验证码成功");
    }

    @Operation(summary = "会员修改密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public CommonResult<Void> updatePassword(@RequestParam String telephone,
                                 @RequestParam String password,
                                 @RequestParam String authCode) {
        memberService.updatePassword(telephone,password,authCode);
        return CommonResult.success(null,"密码修改成功");
    }


    @Operation(summary = "刷新token")
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    public CommonResult<Map<String, String>> refreshToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String refreshToken = memberService.refreshToken(token);
        if (refreshToken == null) {
            return CommonResult.failed("token已经过期！");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }
}
