package com.qmy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Y {
	@Autowired
	private X x;


	public X getX() {
		return x;
	}
}
