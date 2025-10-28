package com.lumine3.lumsaiagent.advisor;

import com.lumine3.lumsaiagent.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;


import java.util.List;
import java.util.Set;

/**
 *
 * 检验用户是否有违禁词
 * 自定义advisor , 简单实现
 */
@Slf4j
public class ProhibitedWordsCheckAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    //违禁词库
    private final Set<String> prohibitedWords ;
    //是否开启, 默认是开启的
    private final boolean enable;

    public ProhibitedWordsCheckAdvisor(){
        this.enable = true;
        //默认违禁词
        this.prohibitedWords = Set.of("死亡","杀人","赌博","暴力","政治");
    }

    public ProhibitedWordsCheckAdvisor(List<String> prohibitedWords ,boolean enable) {
        this.prohibitedWords = Set.copyOf(prohibitedWords);
        this.enable = enable;
    }


    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
    private AdvisedRequest before(AdvisedRequest advisedRequest){
        if(!enable){
            return advisedRequest;
        }
        // 对当前的请求进行违禁词检验
        String userText = advisedRequest.userText();

        if (userText == null || userText.trim().isEmpty()) {
            return advisedRequest;
        }

        for(String word : prohibitedWords){
            if(userText.contains(word)){
                log.warn("包含违禁词: {}", word);
                throw new BusinessException(400,"参数错误","参数含有违禁词");
            }
        }
        return advisedRequest;
    }



    // 实现两个方法
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        return chain.nextAroundCall(advisedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        return chain.nextAroundStream(advisedRequest);
    }
}
