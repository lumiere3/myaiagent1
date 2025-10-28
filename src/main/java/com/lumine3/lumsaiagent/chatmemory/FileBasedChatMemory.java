package com.lumine3.lumsaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个基于文件的持久化方案
 *
 */
public class FileBasedChatMemory implements ChatMemory {

    //自定义一个用户的保存路径
    private final String BASE_DIR;

    // java序列化工具
    private static final Kryo kryo = new Kryo();

    //实例化策略
    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }
    // 构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) { //判断文件是否存在, 如果不存在就创建一个文件
            baseDir.mkdirs();
        }
    }


    /**
     * 保存单条消息
     * @param conversationId
     * @param messages
     */
    /*@Override
    public void add(String conversationId, Message messages) {
        saveConversation(conversationId , List.of(messages));
    }
*/
    /**
     * 保存消息到文件
     * @param conversationId
     * @param messages
     */
    @Override
    public void add(String conversationId, List <Message> messages) {
        // 1.获取以前的会话集合
        List<Message> messageList = getOrCreateConversation(conversationId);
        // 2.修改集合, 把当前message加入
        messageList.addAll(messages);
        // 3.保存集合
        saveConversation(conversationId , messageList);
    }

    /**
     * 获取文件里面的最后n条消息
     * @param conversationId
     * @param lastN
     * @return
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        //1. 获取全部消息
        List<Message> messageList = getOrCreateConversation(conversationId);
        // 2.得到最后的n条消息, 用lamda表达式, 跳过前size - n 个
        return messageList.stream()
                .skip(Math.max(messageList.size() - lastN, 0))
                .toList();
    }

    /**
     * 删除会话
     * @param conversationId
     */
    @Override
    public void clear(String conversationId) {
        // 获取文件
        File file = getConversationFile(conversationId);
        //如果存在就删掉
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * 获取或创建会话信息的列表
     * @param conversationId
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 保存对话信息
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 每个会话按照id单独保存会话以 (会话id + .kryo) 为文件名
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
