package com.qmy.invocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvocatiobHandler implements InvocationHandler {

	private Object target;

	public MyInvocatiobHandler(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("this is proxy");
		return method.invoke(target, args);

	}
}
