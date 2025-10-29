package com.lumine3.lumsaiagent.app;

import com.lumine3.lumsaiagent.advisor.MyLoggerAdvisor;
import com.lumine3.lumsaiagent.advisor.ReReadingAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Component
public class LoveApp {

    // 手动创建 Logger
    //private static final Logger log = LoggerFactory.getLogger(LoveApp.class);

    /**
     * 使用构造器注入 ChatClient
     */
    private final ChatClient chatClient;

    /**
     * 用于启动知识库
     */
    @Resource
    private VectorStore loveAppVectorStore;

    /**
     * 系统提示词
     */
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。" +
            "开场向用户表明身份，告知用户可倾诉恋爱难题。围绕单身、恋爱、已婚三种状态提问：" +
            "单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；" +
            "已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n";
    @Resource
    private Advisor loveAppRagCloudAdvisor;
    /*private final VectorStore loveAppVectorStore;*/


    /**
     * 初始化app
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel, VectorStore loveAppVectorStore) {
        /**
         * 对话记忆, 基于内存实现
         */
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        //Advisor的几种分类
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor(),//自定义日志advisor
                        new ReReadingAdvisor() // 自定义推理增强advisor
                )
                .build();
        /**
         * 会话记忆, 基于我们设置的文件会话记忆来实现
         */
       /* //创建临时文件路径
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemoryByFile = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        //Advisor的几种分类
                        new MessageChatMemoryAdvisor(chatMemoryByFile),
                       *//* new ProhibitedWordsCheckAdvisor(),*//* //自定义的敏感词拦截advisor
                        new MyLoggerAdvisor()*//*,//自定义日志advisor
                        new ReReadingAdvisor()*//* // 自定义推理增强advisor
                )
                .build();
        this.loveAppVectorStore = loveAppVectorStore;*/
    }

    /**
     * 编写一个对话方法, 基于对话
     * @param msg 用户的对话内容
     * @param chatId //chatId用于隔离不同的对话
     * @return String
     */
    public String doChat(String msg , String chatId){
        ChatResponse chatResponse =
                chatClient
                .prompt()
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        //获取结果
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content,  {} ", content);
        return content;
    }

    /**
     * record 生成一个类 演示结构化输出
     */
    record LoveReport(String tittle, List<String> suggestions){}

    /**
     * 生成一个报告 , 演示结构化输出
     * 输出的结果以我们上面设置的record类输出
     * @return
     */
    public LoveReport doChatWithReport(String msg , String chatId){
        LoveReport report =
                chatClient
                        .prompt()
                        .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                        .user(msg)
                        .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                        .call()
                        .entity(LoveReport.class);
        //获取结果, 当前的返回结果应该是一个对象
        log.info("report : {} ", report);
        return report;
    }


    /**
     * rag的对话方法
     * @param msg
     * @param chatId
     * @return
     */


    public String doChatWithGag(String msg , String chatId){
        ChatResponse chatResponse =
                chatClient
                        .prompt()
                        .user(msg)
                        .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                        //开启一个自定义的日志, 便于观察
                        .advisors(new MyLoggerAdvisor())
                        // 开启问答拦截器, 调用知识库进行回答
                        //.advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                        // 引用RAG检索增强服务 基于云知识库
                        .advisors(loveAppRagCloudAdvisor)
                        .call()
                        .chatResponse();
        //获取结果
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content,  {} ", content);
        return content;
    }





}
