package com.qmy.beanpostprocessor;

import com.qmy.invocationHandler.MyInvocatiobHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;

import java.lang.reflect.Proxy;

//@Component

public class TestBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(beanName.equals("indexDaoImpl")){
			System.out.println("this is beforeInitialization");
		}
		Object proxy = Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getInterfaces(), new MyInvocatiobHandler(bean));
		return proxy;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(beanName.equals("indexDaoImpl")) {
			System.out.println("this is afterInitialization");
		}
		return bean;
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
