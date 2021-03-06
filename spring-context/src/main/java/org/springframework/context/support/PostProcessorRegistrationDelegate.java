/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}

	/**
	 * 此方法就是取出实现了 BeanFactoryPostProcessor 以及 BeanDefinitionRegistryPostProcessor 接口的类
	 * 如果开发者没有自定义类实现以上接口，那么 Spring 此时只有一个类实现了以上接口 ConfigurationClassPostProcessor
	 * 因为 BeanDefinitionRegistryPostProcessor 是 BeanFactoryPostProcessor 的子接口
	 * 所以 Spring 用 regularPostProcessors ， registryProcessors 两个 List 来分别存储
	 * 实现了 BeanDefinitionRegistryPostProcessor，BeanFactoryPostProcessor 的类，
	 * 先判断是否是 BeanDefinitionRegistryPostProcessor 的子类，如果是，调用实现了 BeanDefinitionRegistryPostProcessor 接口的类的 postProcessBeanDefinitionRegistry 方法，
	 * 如果不是，那就是实现了父接口 BeanFactoryPostProcessor ，则用 regularPostProcessors 存储，最后统一调用 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法
	 * 但父接口 BeanFactoryPostProcessor 还有一个方法 postProcessBeanFactory，暂时不会调用
	 * 所以先用 registryProcessors 把此对象存储起来，方便之后同一调用 postProcessBeanFactory
	 * 所以 Spring 用 regularPostProcessors ， registryProcessors 两个 List 来分别存储
	 * @param beanFactory
	 * @param beanFactoryPostProcessors
	 */
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		/**
		 * List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
		 * List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
		 * 为什么下边 要定义两个 list？
		 *  目的是分开存放不同接口类型的 bean 工厂后置处理器
		 *  BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor
		 *  BeanDefinitionRegistryPostProcessor 扩展了 BeanFactoryPostProcessor接口，
		 *  开发者自定义的 BeanFactoryProcessor 可以有两种实现方式，实现 BeanDefinitionRegistryPostProcessor 或者  BeanDefinitionRegistryPostProcessor
		 * 	实现功能不同，所以准备两个 list 存放不同的  BeanFactoryProcessor，做不同处理
		 */
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			/** 这里判断自定义的 beanFactoryPostProcessor 实现的是那个接口
			 *  子接口 BeanDefinitionRegistryPostProcessor 还是 父接口 beanFactoryPostProcessors
			 *  针对实现的不同接口做不同处理，分别用不同 list 来存放
			 * **/
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			/**
			 *  上边也有一个 List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
			 *  为什么 Spring 要定义两个  List<BeanDefinitionRegistryPostProcessor> ？
			 *  因为 上边的  registryProcessors 存放的是开发者自己定义实现的 BeanDefinitionRegistryPostProcessor 接口的类
			 *  这里的 currentRegistryProcessors 存放的是 Spring 内部自己定义实现了 BeanDefinitionRegistryPostProcessor 接口的类
			 *  上边已经处理完了开发者自定义的 BeanDefinitionRegistryPostProcessor 的 postProcessBeanDefinitionRegistry 方法,
			 *  注意，还有一个 postProcessorBeanFactory 方法未执行
			 *  此时 Spring 自己的 BeanDefinitionRegistryPostProcessor 还未注册
			 *  接下来 Spring 会完成自己的 BeanDefinitionRegistryPostProcessor
			 */
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			/**
			 * 首先,调用实现了 PriorityOrdered 接口的 BeanDefinitionRegistryPostProcessors
			 * ConfigurationClassPostProcessor 实现了 PriorityOrdered 接口，所以在这里会被调用
			 */
			/** 根据类型获取 bean 的名字，这个类型是 BeanDefinitionRegistryPostProcessor.class
			 * 	BeanDefinitionRegistryPostProcessor.class，此时 Spring 内部只有 ConfigurationClassPostProcessor
			 * 	ConfigurationClassPostProcessor implements  BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor
			 * **/
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			/**
			 * 这里可以得到一个 BeanFactoryPostProcessor , 是 Spring 在最开始默认自己注册的
			 * 为什么要在最开始注册呢？
			 * 因为 Spring 的工厂需要有 扫描、解析 等功能
			 * 而这些功能都需要在 Spring 工厂初始化之前执行
			 * 要么在工厂初始化之前，要么在初始化期间，但不能在初始化之后
			 * 因为 Spring 工厂初始化之后就已经需要使用了
			 * 所以 Spring 在一开始就注册了一个 BeanFactoryPostProcessor -->currentRegistryProcessors.add(),
			 * 用来插手 Spring 工厂的实例化过程
			 * 在此处断点可知，currentRegistryProcessors add的这个类是 ConfigurationClassPostProcessor
			 * ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
			 */
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					/**
					 * 这里 getBean() 实例化了一些 Spring 初始化需要的对象
					 * ConfigurationClassPostProcessor 类的就是在这里实例化的，
					 * 因为下边调用 invokeBeanDefinitionRegistryPostProcessors 方法，需要调用
					 * ConfigurationClassPostProcessor 的 postProcessBeanDefinitionRegistry 方法
					 * 所以在这里必须先实例化 ConfigurationClassPostProcessor
					 */
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			/** 合并list  registryProcessors 、 currentRegistryProcessors
			 *  即开发者自定义的 BeanDefinitionRegistryPostProcessor 和 Spring 内部的 BeanDefinitionRegistryPostProcessor
			 * */
			registryProcessors.addAll(currentRegistryProcessors);
			/**
			 * 此时，currentRegistryProcessors 列表中就只有 ConfigurationClassPostProcessor 一个类
			 * 此方法循环所有 BeanDefinitionRegistryPostProcessor 类型，
			 * 调用 他的扩展方法 postProcessBeanDefinitionRegistry()
			 * 就在这里进行包扫描，并将所有交由 Spring 创建的类封装为 BeanDefinition 注册进 beanDefinitionMap 中
			 */
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			/**
			 *接下来,调用 实现了 Ordered 接口的 BeanDefinitionRegistryPostProcessors
			 */
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			/**
			 *最后,调用其他所有 BeanDefinitionRegistryPostProcessors
			 */
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			/**
			 * 这里调用的是 继承了 BeanFactoryPostProcessor 接口的 bean 工厂后置处理器
			 * 上边调用的则是 继承了 BeanDefinitionRegistryPostProcessor 接口的 bean 工厂后置处理器
			 */
			/** 调用 BeanDefinitionRegistryPostProcessor 的 postProcessBeanFactory 方法
			 * @Configuration 注解的作用，配置类采用 cglib 代理，就在这个方法里完成
			 * **/
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);

			/** 调用开发者自定义的 BeanFactoryPostProcessor 的 postProcessBeanFactory 方法 **/
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		/** 从 BeanDefinitionMap 中得到所有的 BeanPostProcessor
		 * (这句话是重点,AOP 的后置处理器也是从 map 中获取,但并不是所有的 BeanPostProcessor 都需要注册进 map)
		 * 包括开发者自定义的 BeanPostProcessor
		 * 注册顺序 PriorityOrdered ---> Ordered ---> regular
		 * interface PriorityOrdered extends Ordered
		 * 没有实现以上两个接口的为 regular，最后注册
		 * **/
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		/**
		 *  AutoProxyCreator 一个 beanPostProcessor
		 *  AOP 的 后置处理器，
		 *  在之后 InitializeBean 方法中调用该后置处理器为实现了 AOP 的对象创建代理
		 */
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 * 这里调用的是 继承了 BeanDefinitionRegistry 接口的 bean 工厂后置处理器
	 * 此方法循环所有 BeanDefinitionRegistryPostProcessor 类型，
	 * 调用 他的扩展方法 postProcessBeanDefinitionRegistry()
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		/** 这里正常情况 postProcessors 只有一条数据，即 ConfigurationClassPostProcessor **/
		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 * 这里调用的是 继承了 BeanFactoryPostProcessor 接口的 bean 工厂后置处理器
	 * 此方法循环所有 BeanFactoryPostProcessor 类型，
	 * 调用 他的扩展方法 postProcessBeanFactory()
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 * 当 Spring 配置中的后置处理器还没有被注册就已经开始了 bean 初始化,
	 * 就是检查 bean 的后置处理器有没有被执行
	 * 便会打印出 BeanPostProcessorChecker 中设定的信息
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
