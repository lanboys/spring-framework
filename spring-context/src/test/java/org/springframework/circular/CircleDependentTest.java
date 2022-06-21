package org.springframework.circular;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CircleDependentTest {

  @Test
  public void test() {
    ApplicationContext context = new ClassPathXmlApplicationContext("org/springframework/circular/applicationContext-circular.xml");

    BeanB beanB = (BeanB) context.getBean("beanB");
    System.out.println("main(): " + beanB);
  }
}