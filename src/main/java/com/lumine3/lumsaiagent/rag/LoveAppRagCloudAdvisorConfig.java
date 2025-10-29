package com.lumine3.lumsaiagent.rag;


import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.*;

/**
 * 自定义基于阿里云知识库的 rag 增强检索顾问
 */

@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorConfig {

    /**
     * 获取api key
     * 注意引入spring的@Value
     */
    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    /*@Bean
    public Advisor loveAppRagCloudAdvisor(){
        DashScopeApi dashScopeApi = new DashScopeApi(dashscopeApiKey);
        *//**
         * 知识库的名字*//*


       DocumentRetriever documentRetriever =
                new DashScopeDocumentRetriever(dashScopeApi, DashScopeDocumentRetrieverOptions
                .builder()
                .withIndexName(KNOWLEDGE_INDEX).build());

        return RetrievalAugmentationAdvisor.builder().documentRetriever(documentRetriever).build();
    }*/

    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        DashScopeApi dashScopeApi = new DashScopeApi(dashscopeApiKey);
        final String KNOWLEDGE_INDEX = "my-test-lovemaster";
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());
        return  RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }

}
