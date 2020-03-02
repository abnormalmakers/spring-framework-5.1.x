package com.luban.test;

import com.luban.config.AppConfig;
import com.luban.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestIOC {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext app = new AnnotationConfigApplicationContext();
		app.register(IndexDao.class);
		app.refresh();
		IndexDao indexDao = (IndexDao) app.getBean("indexDao");
		indexDao.query();
	}
}
