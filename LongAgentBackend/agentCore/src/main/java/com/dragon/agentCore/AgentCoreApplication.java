package com.dragon.agentCore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.dragon.agentCore.mapper")
@SpringBootApplication
public class AgentCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgentCoreApplication.class, args);
	}

}
