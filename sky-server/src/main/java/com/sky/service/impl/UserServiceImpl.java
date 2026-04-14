package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import io.swagger.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    //微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO loginDTO) {
        // 1、创建HttpClient对象发送登录请求
        // 先获取用户端openId
        HashMap hashMap = new HashMap(); // 创建哈希集合封装需要发送到json信息
        hashMap.put("appid",weChatProperties.getAppid());
        hashMap.put("secret",weChatProperties.getSecret());
        hashMap.put("js_code",loginDTO.getCode());
        hashMap.put("grant_type","authorization_code");

        String res = HttpClientUtil.doGet(WX_LOGIN, hashMap);
        log.info("微信服务返回结果：{}",res);

        // 解析响应结果
        JSONObject jsonObject = JSON.parseObject(res);
        String openid = jsonObject.getString("openid");
        // 判断是否为空
        if (openid ==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 判断是否是新用户
        User user = userMapper.getUser(openid);
        // 是新用户就进行数据库新增操作
        if (user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insertUser(user);
        }
        // 不是就直接返回
        return user;
    }
}
