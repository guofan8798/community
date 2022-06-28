package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @Author guofan
 * @Date 2022-05-26 15:50
 * @Description
 */

@Configuration
public class KaptchaConfig {

    /**
     * @return Producer 类型的 kaptcha实例，有两个方法：
     *          1.创建验证码；createImage(String var1)
     *          2.创建图片；createText()
     */
    @Bean
    public Producer kaptchaProducer(){
        //写配置
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        //font字体，size字号
        properties.setProperty("kaptcha.textproducer.font.size","32");
        //0,0,0是黑色，也可以写black
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        //随机字符串的可取值
        properties.setProperty("kaptcha.textproducer.char.string","0123456789abcdefghijklmnopqrstuvwxyz");
        //随机字符的个数
        properties.setProperty("kaptcha.textproducer.char.length","4");
        //要采用的干扰/噪声 类
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        //装载配置
        Config config = new Config(properties);
        //对Producer类型的kaptcha进行配置
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
