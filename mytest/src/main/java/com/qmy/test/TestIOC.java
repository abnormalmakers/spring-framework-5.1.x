package com.qmy.test;

import com.qmy.beanFactoryPostProcessor.MyBeanFactoryPostProcessor;
import com.qmy.config.AppConfig;
import com.qmy.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestIOC {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext app = new AnnotationConfigApplicationContext();
		app.register(AppConfig.class);
		app.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor());
		app.refresh();

		AppConfig appConfig = (AppConfig) app.getBean("appConfig");
//		AppConfig bean = app.getBean(AppConfig.class);
//		IndexDao indexDao = app.getBean("indexDaoImpl",IndexDao.class);
		IndexDao indexDao = app.getBean(IndexDao.class);
		indexDao.query();



//		System.out.println(indexDao);
	}
}
