package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * @Author guofan
 * @Date 2022-06-01 16:03
 * @Description 敏感词过滤器
 */

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //将敏感词替换为***
    private static final String REPLACEMENT = "***";

    //初始化前缀树
    //先初始化根节点
    private TrieNode rootNode = new TrieNode();

    //该注解表示这是一个初始化方法。当容器实例化这个bean(SensitiveFilter)以后，在调用过滤器之后，
    //init方法就会被自动调用(bean在服务器启动的时候就被初始化了)
    @PostConstruct
    public void init() {
        //先把含有敏感字符的文件读出来

        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                //把字节流转为字符流,再转为缓冲流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        ) {
            //通过reader读取每一个敏感词
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //读到了之后，就把敏感词添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    /**
     * 定义一个方法，把敏感词添加到前缀树中去
     */
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            //试图获取一个子节点(可能没有，需要后续判断)
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                //初始化子节点(没有子节点，新建子节点，写入字符)
                subNode = new TrieNode();
                //挂到当前节点之下
                tempNode.addSubNode(c, subNode);
            }

            //调整指针指向子节点，进入下一轮循环
            tempNode = subNode;

            //循环完之后，设置标识
            if (i == keyword.length() - 1) {
                //表示这是一个敏感词
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        //先判空
        if (StringUtils.isBlank(text)) {
            return null;
        }

        //指针1，指向树
        TrieNode tempNode = rootNode;
        //指针2,指向待检测text
        int begin = 0;
        //指针3,指向待检测text
        int position = 0;

        //每一次过滤(并替换敏感词)后的结果结果
        StringBuffer sb = new StringBuffer();

        //结束遍历
        while (position < text.length()) {
            char c = text.charAt(position);

            //跳过符号
            if (isSymbol(c)) {
                //若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头还是中间，指针3都向下走一步
                position++;
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            //下级没有节点
            if (tempNode == null) {
                //以begin为开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeyWordEnd()) {
                //发现了敏感词，将begin~position内的字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;
            } else {
                //检查下一个字符
                position++;
            }
        }
        //将最后一批字符計入結果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    /**
     * 判断是否为符号
     */
    private boolean isSymbol(Character c) {
        //CharUtils.isAsciiAlphanumeric() 是普通符号则返回true，非常规字符则返回false。这里需要取反
        //0X2E80 ~ 0X9FFF 表示东亚文字范围(包括中文、日文、韩文等)
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0X2E80 || c > 0X9FFF);
    }

    /**
     * 定义前缀树树形结构:①子节点(是一个map，key是字符，value是树形结构)；②结尾标志(有标志说明是敏感词)
     * 定义一个内部类(前缀树的结构)。因为只在这个类里调用，所以采用内部类的方式，不允许外部访问
     */
    private class TrieNode {

        //描述关键词结束的标识（树形结构某个分支尾部是否有标志）
        private boolean isKeyWordEnd = false;

        //描述当前节点的子节点(key是下级节点的字符，value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
