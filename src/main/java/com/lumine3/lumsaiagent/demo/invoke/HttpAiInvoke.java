package com.lumine3.lumsaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class HttpAiInvoke {
    
    public static void main(String[] args) {
        String response = invokeTextGeneration();
        System.out.println(response);
    }
    
    public static String invokeTextGeneration() {
        // 从环境变量或配置中获取 API Key
        String apiKey = TestApiKey.API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("DASHSCOPE_API_KEY 环境变量未设置");
        }
        
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen-plus");
        
        JSONObject input = new JSONObject();
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");
        
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "你是谁？");
        
        input.set("messages", JSONUtil.createArray()
                .put(systemMessage)
                .put(userMessage));
        
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");
        
        requestBody.set("input", input);
        requestBody.set("parameters", parameters);
        
        // 发送请求
        return HttpRequest.post("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(30000) // 设置30秒超时
                .execute()
                .body();
    }
}