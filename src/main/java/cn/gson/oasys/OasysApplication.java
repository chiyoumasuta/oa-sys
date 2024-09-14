package cn.gson.oasys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@MapperScan("cn.gson.oasys**.dao")
public class OasysApplication {
	public static void main(String[] args) {
		SpringApplication.run(OasysApplication.class, args);

	}
}

