package org.springframework.beans.factory.support;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

public class DefaultSingletonBeanRegistry2Tests {

  /**
   *  测试怎么解决循环依赖
   *
   *  模拟 AbstractBeanFactory # doGetBean ，注意方法名，都是一一对应
   *
   */
  @Test
  public void testDoGetBean() {
    DefaultSingletonBeanRegistry beanRegistry = new DefaultSingletonBeanRegistry();
    String beanName = "tb";
    String result = doGetBean(beanRegistry, beanName);
    System.out.println("result: " + result);
  }

  private String doGetBean(DefaultSingletonBeanRegistry beanRegistry, String beanName) {
    Object singleton = beanRegistry.getSingleton(beanName);
    if (singleton == null) {
      singleton = beanRegistry.getSingleton(beanName, new ObjectFactory<Object>() {
        @Override
        public Object getObject() throws BeansException {
          String tBean = createBeanAndDoCreateBean(beanName, beanRegistry);
          return tBean;
        }
      });
    }
    System.out.println("获取bean成功: " + singleton);
    return (String) singleton;
  }

  private String createBeanAndDoCreateBean(String beanName, DefaultSingletonBeanRegistry beanRegistry) {
    String tBean = beanName;
    System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
    System.out.println("创建bean: " + tBean);
    // 缓存起来
    beanRegistry.addSingletonFactory(beanName, new ObjectFactory<Object>() {
      @Override
      public Object getObject() throws BeansException {
        System.out.println("从三级缓存中获取: " + tBean);
        return tBean;
      }
    });
    System.out.println("放入三级缓存成功: " + tBean);
    // 两者相互依赖
    //populateBean1(beanRegistry, tBean);
    // 三者依赖成一个环
    populateBean2(beanRegistry, tBean);
    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    return tBean;
  }

  /**
   *  ta -> tb -> ta
   */
  private void populateBean1(DefaultSingletonBeanRegistry beanRegistry, String tBean) {
    System.out.println("开始处理依赖注入: " + tBean);

    // 此处模拟 相互依赖
    if ("ta".equals(tBean)) {
      // ta 依赖 tb, 需要注入 tb
      String tb = doGetBean(beanRegistry, "tb");
      System.out.println("ta 注入 tb 成功");
    } else if ("tb".equals(tBean)) {
      // tb 依赖 ta, 需要注入 ta
      String ta = doGetBean(beanRegistry, "ta");
      System.out.println("tb 注入 ta 成功");
    }
  }

  /**
   *  ta -> tb -> tc -> ta
   */
  private void populateBean2(DefaultSingletonBeanRegistry beanRegistry, String tBean) {
    System.out.println("开始处理依赖注入: " + tBean);

    // 此处模拟 相互依赖
    if ("ta".equals(tBean)) {
      // ta 依赖 tb, 需要注入 tb
      String tb = doGetBean(beanRegistry, "tb");
      System.out.println("ta 注入 tb 成功");
    } else if ("tb".equals(tBean)) {
      // tb 依赖 tc, 需要注入 tc
      String tc = doGetBean(beanRegistry, "tc");
      System.out.println("tb 注入 tc 成功");
    } else if ("tc".equals(tBean)) {
      // tc 依赖 ta, 需要注入 ta
      String ta = doGetBean(beanRegistry, "ta");
      System.out.println("tc 注入 ta 成功");
    }
  }
}
