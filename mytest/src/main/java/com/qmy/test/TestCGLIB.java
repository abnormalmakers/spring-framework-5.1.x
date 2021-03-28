package com.qmy.test;

import com.qmy.service.ServiceImpl;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class TestCGLIB implements MethodInterceptor {

	public Object getProxy(Class cls){
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(cls);
		enhancer.setCallback(this);
		return enhancer.create();
	}

	@Override
	public Object intercept(Object proxy, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
		System.out.println("before method invoked");
		Object o = methodProxy.invokeSuper(proxy, objects);
		System.out.println("after method invoked");
		return o;
	}

	public static void main(String[] args) {
		TestCGLIB testCGLIB = new TestCGLIB();

		ServiceImpl proxy = (ServiceImpl) testCGLIB.getProxy(ServiceImpl.class);
		proxy.query();



	}
}
