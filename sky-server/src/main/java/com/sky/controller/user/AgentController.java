package com.sky.controller.user;

import com.sky.agent.SkyOrderAgent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/agent")
@Api(tags = "C端-智能点餐助理")
public class AgentController {

    @Autowired
    private SkyOrderAgent skyOrderAgent;

    @GetMapping("/chat")
    @ApiOperation("与智能助理对话")
    public String chat(@RequestParam String message) {
        // 直接调用刚才配置好的接口，底层的参数提取、工具调用、记忆管理全部由 LangChain4j 自动完成了！
        return skyOrderAgent.chat(message);
    }
}