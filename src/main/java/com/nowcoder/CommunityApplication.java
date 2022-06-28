package com.nowcoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

//	用来管理Bean的生命周期的，主要是用来管理Bean的初始化的方法
//	由该注解修饰的方法，会在构造器被调用之后执行，所以通常是初始化方法。
//	而这个Bean是最早被加载的
	@PostConstruct
	public void init(){
		//解决netty启动冲突的问题，(Redis和ES)
		//解决方案来自于：Netty4Utils.setAvailableProcessors();一个开关
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}


	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}
}
