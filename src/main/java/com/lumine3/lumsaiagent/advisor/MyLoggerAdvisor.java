package com.lumine3.lumsaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义advisor
 * 自定义日志advisor 输出info级别的日志
 * 只输出单次用户提示词和 AI 回复的文本
 * 因为SpringAI 内置的一个日志的拦截器输出debug级别的日志, 而Spring Boot默认是info 所以会看不到
 * 因此我们自定义advisor来输出info级别的日志
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    // 两个接口都要实现...

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * before来处理请求日志
     * @param request
     * @return
     */
    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI Request: {}", request.userText());
        return request;
    }

    /**
     * 处理相应日志
     * @param advisedResponse
     */
    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        /**
         * 请求处理
         */
        advisedRequest = this.before(advisedRequest);

        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

        observeAfter(advisedResponse);

        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }





}
