package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author guofan
 * @Date 2022-05-24 17:15
 * @Description
 */
public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密
    //key=原始密码+随机字符串（用户表里的salt）
    public static String md5(String key){
        if (StringUtils.isBlank(key)){
            return null;
        }
        //将传入结果key转换成一个16进制的字符串返回
        //要求传入的参数是byte，而key是字符串，所以key.getBytes()转换成为byte。
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     *
     * @param code 编号
     * @param msg 提示
     * @param map
     * @return json格式的字符串
     */
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map!=null){
            for (String key:map.keySet()){
                json.put(key,map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code,String msg){
        return getJSONString(code,msg,null);
    }

    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("name","张三");
        map.put("age","25");
        System.out.println(getJSONString(0,"ok",map));
        //{"msg":"ok","code":0,"name":"张三","age":"25"}
    }

}
