package com.mvcboot;

import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class MySpringWebApplication implements WebApplicationInitializer {
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
	}
}
