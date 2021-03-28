package com.qmy.config;

import com.qmy.imports.MyImportBeanDefinitionRegistrar;
import com.qmy.service.ServiceImpl;
import com.qmy.service.ServiceImpl2;
import org.springframework.context.annotation.*;


@ComponentScan("com.qmy")
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(MyImportBeanDefinitionRegistrar.class)
public class AppConfig {
	@Bean
	public ServiceImpl serviceImpl(){
		return new ServiceImpl();
	}

	@Bean
	public ServiceImpl2 serviceImpl2(){
		serviceImpl();
		return new ServiceImpl2();
	}


}
