package com.lumine3.lumsaiagent.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Autowired
    private LoveApp loveApp;

    @Test
    public void appTest(){
        // 构造一个对话id
        String chatId = UUID.randomUUID().toString();
        // 构造多轮对话, 我们的对话应该是可以记住以前的对话的
        //1. 第一轮对话
        String msg1 = "你好,我是名字是政治!";
        String ans1 = loveApp.doChat(msg1,chatId);
        //2. 第二轮对话
        String msg2 = "我想让我的另一半(BB)更爱我";
        String ans2 = loveApp.doChat(msg2,chatId);
        //3. 第三轮对话
        String msg3  = "你还记得我是谁吗? 我的另一半是谁?";
        String ans3 = loveApp.doChat(msg3,chatId);
        System.out.println(ans3);

    }



    @Test
    void doChatWithReport() {
        // 构造一个对话id
        String chatId = UUID.randomUUID().toString();
        // 构造多轮对话, 我们的对话应该是可以记住以前的对话的
        //1. 第一轮对话
        String msg1 = "你好,我是名字是牢乔! 我现在和aa有一些小问题, 你能给我一些建议吗?";
        LoveApp.LoveReport ans = loveApp.doChatWithReport(msg1,chatId);
    }
}