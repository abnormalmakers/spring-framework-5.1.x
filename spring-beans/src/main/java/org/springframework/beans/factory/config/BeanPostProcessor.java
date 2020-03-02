/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * BeanPostProcessor 是 Spring 框架通过的一个扩展类点(不止一个)
 * 通过实现 BeanPostProcessor 接口 ，程序员可以插手 bean 的实例化过程 ， 减轻 BeanFactory 的负担
 * 这个接口可设置多个，形成一个列表，然后依次执行
 * 具体查看 AbstractBeanFactory ， 其中
 * private final List<BeanPostProcessor> beanPostProcessors=new CopyOnWriteArrayList<>(); 此属性 维护一个 List<BeanPostProcessor>
 * addBeanPostProcessor(BeanPostProcessor beanPostProcessor) 方法添加一个 BeanPostProcessor
 * List<BeanPostProcessor> getBeanPostProcessors() 此方法返回 BeanPostProcessors 列表
 * 在整个 bean 的实例化过程中，依次循环这个 List<BeanPostProcessor> 来达到插手 bean 实例化的过程
 * 比如 AOP 就是在 bean 的实例化后将切面逻辑动态的织入 bean实例中，AOP 也正是通过 BeanPostProcessor 和 IOC 容器建立起了联系
 * Spring 默认提供了喝多 PostProcessor
 * 1. ApplicationContextAwareProcessor
 * 		作用： 当应用程序定义的 Bean 实现了 ApplicationContextAware 接口时给其注入 ApplicationContext 对象
 *
 * 2.InitDestroyAnnotationBeanPostProcessor
 * 	 	作用： 用来处理自定义的初始化方法he销毁方法
 * 	 	Spring 提供了 3 种自定义初始化和销毁方法
 * 	 		1.通过 @Bean 指定 init-method 和 destroy-method	 属性
 * 	 		2.Bean 实现 InitializingBean 和 DisposableBean 接口
 * 			3.@PostConstructor  和 @PreDestroy
 *
 * 3.InstantiationAwareBeanPostProcessor
 * 4.CommonAnnotationBeanPostProcessor
 * 5.AutowiredAnnotationBeanPostProcessor
 * 6.RequireAnnotationBeanPostProcessor
 * 7.BeanValidationPostProcessor
 * 8.AbstractAutoProxyCreator	(AOP)
 * ......
 *
 * Factory hook that allows for custom modification of new bean instances,
 * e.g. checking for marker interfaces or wrapping them with proxies.
 *
 * <p>ApplicationContexts can autodetect BeanPostProcessor beans in their
 * bean definitions and apply them to any beans subsequently created.
 * Plain bean factories allow for programmatic registration of post-processors,
 * applying to all beans created through this factory.
 *
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement {@link #postProcessBeforeInitialization},
 * while post-processors that wrap beans with proxies will normally
 * implement {@link #postProcessAfterInitialization}.
 *
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * 在 bean 初始化之前执行
	 * Apply this BeanPostProcessor to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * 在 bean 初始化之后执行
	 * Apply this BeanPostProcessor to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
	 * instance and the objects created by the FactoryBean (as of Spring 2.0). The
	 * post-processor can decide whether to apply to either the FactoryBean or created
	 * objects or both through corresponding {@code bean instanceof FactoryBean} checks.
	 * <p>This callback will also be invoked after a short-circuiting triggered by a
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
	 * in contrast to all other BeanPostProcessor callbacks.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
