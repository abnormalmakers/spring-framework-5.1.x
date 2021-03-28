package com.qmy.test;

import com.qmy.config.AppConfig;
import com.qmy.service.X;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestXY {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext app = new AnnotationConfigApplicationContext(AppConfig.class);
		X x = app.getBean(X.class);
		System.out.println(x.getY());




	}
}
