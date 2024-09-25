package cn.gson.oasys;

import cn.gson.oasys.flowable.AppDispatcherServletConfiguration;
import cn.gson.oasys.flowable.ApplicationConfiguration;
import org.flowable.ui.modeler.conf.DatabaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;


//启用全局异常拦截器
@Import(value={
		ApplicationConfiguration.class,
		AppDispatcherServletConfiguration.class,
		DatabaseConfiguration.class})
@SpringBootApplication(exclude={SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@EnableTransactionManagement
@EnableScheduling
@MapperScan("cn.gson.oasys**.dao")
public class OasysApplication extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(OasysApplication.class);
	}
	public static void main(String[] args) {
		SpringApplication.run(OasysApplication.class, args);

	}
}
