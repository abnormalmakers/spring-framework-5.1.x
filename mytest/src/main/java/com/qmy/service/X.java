package com.qmy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("x")
public class X {
	@Autowired
	private Y y;

//	@Resource
//	private IndexDao indexDao;

	public Y getY() {
		return y;
	}

	@PostConstruct
	public void postConstruct(){
		System.out.println("postConstruct");
	}

	public void init(){
		System.out.println("init");
	}

}
