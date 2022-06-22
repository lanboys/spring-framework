package org.springframework.aop.bing;

import org.junit.Test;
import org.springframework.aop.bing.service.CatService;
import org.springframework.aop.bing.service.DogService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class AopTests {

  private static final Class<?> CLASS = AopTests.class;
  private static final String CLASSNAME = CLASS.getSimpleName();

  ClassPathResource resource_proxyFactoryBean = new ClassPathResource(CLASSNAME + "-proxyFactoryBean.xml", getClass());
  ClassPathResource resource = new ClassPathResource(CLASSNAME + ".xml", getClass());

  @Test
  public void testProxyFactoryBean() {
    BeanFactory beanFactory = new DefaultListableBeanFactory();
    new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory).loadBeanDefinitions(resource_proxyFactoryBean);
    DogService dog = (DogService) beanFactory.getBean("dog");
    dog.run();
  }

  @Test
  public void testAop() {
    // 不能直接用 DefaultListableBeanFactory，因为没有注册后置处理器 AspectJAwareAdvisorAutoProxyCreator，无法创建代理对象
    //DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    //new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory).loadBeanDefinitions(resource);

    BeanFactory beanFactory = new ClassPathXmlApplicationContext("AopTests.xml", AopTests.class);
    DogService dog = (DogService) beanFactory.getBean("dog");
    dog.run();

    System.out.println("testAop(): =================");

    CatService cat = (CatService) beanFactory.getBean("cat");
    cat.go();
  }
}
