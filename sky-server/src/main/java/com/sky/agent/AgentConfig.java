package com.sky.agent;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean
    public SkyOrderAgent skyOrderAgent(ChatLanguageModel chatModel, DishAgentTools dishAgentTools) {
        return AiServices.builder(SkyOrderAgent.class)
                .chatLanguageModel(chatModel)
                // 注入短期记忆，记住最近的 10 条对话，这样用户可以说“上一份太辣了，换一个”
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) 
                // 装备我们写好的查询菜品的 Tool
                .tools(dishAgentTools) 
                .build();
    }
}