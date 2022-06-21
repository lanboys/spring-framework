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
    String result = doGetBean(beanRegistry, beanName, 0);
    System.out.println("result: " + result);
  }

  private String doGetBean(DefaultSingletonBeanRegistry beanRegistry, String beanName, int recursion) {
    //模拟 AbstractBeanFactory 246 行
    printLog(recursion, "============>>>>>>>>>>>>>");
    printLog(recursion, "将要从容器中获取bean: " + beanName);
    Object singleton = beanRegistry.getSingleton(beanName);
    printLog(recursion, "容器一二三级缓存中获取结果：" + singleton);
    if (singleton == null) {
      // 模拟 AbstractBeanFactory 321 行
      singleton = beanRegistry.getSingleton(beanName, new ObjectFactory<Object>() {
        @Override
        public Object getObject() throws BeansException {
          printLog(recursion, "开始实例化 bean: " + beanName);
          String tBean = createBeanAndDoCreateBean(beanName, beanRegistry, recursion);
          // 这里是原始的 bean，不是代理的 bean , 最外层的代理的 bean 是在后置处理器中创建，比如这里的 tb 代理，
          // 其实在循环依赖的时候已经被创建，那么后置处理器处理的时候直接复用
          return tBean;
        }
      });
      // getSingleton 执行完成后，就会放入一级缓存 singletonObjects，并清除二级三级缓存中的数据
      printLog(recursion, "放入一级缓存成功，并清除二级三级缓存: " + beanName);
    }
    printLog(recursion, "获取bean成功: " + singleton);
    printLog(recursion, "<<<<<<<<<<<<============");
    return (String) singleton;
  }

  private String createBeanAndDoCreateBean(String beanName, DefaultSingletonBeanRegistry beanRegistry, int recursion) {
    String tBean = beanName + "-instance";// 这里模拟创建 bean 实例
    printLog(recursion, "真正的实例化 bean: " + tBean);
    // 创建 bean 后，先放三级缓存里面, 提前暴露出去，方便的后面循环依赖注入获取
    // 模拟 AbstractAutowireCapableBeanFactory 551 行
    beanRegistry.addSingletonFactory(beanName, new ObjectFactory<Object>() {
      @Override
      public Object getObject() throws BeansException {
        String reference = getEarlyBeanReference(beanName, tBean, recursion);
        printLog(recursion, "从三级缓存中获取对象或者提前创建的代理对象: " + reference);
        return reference;
      }
    });
    // 此时 ：一级：  singletonObjects             暂无对象
    //       二级：  earlySingletonObjects        暂无对象
    //       三级：  singletonFactories           有上面放进去的对象
    printLog(recursion, "单例工厂放入三级缓存成功: " + beanName);
    // 两者相互依赖
    populateBean1(beanRegistry, tBean, recursion);
    // 三者依赖成一个环
    //populateBean2(beanRegistry, tBean, recursion);
    return tBean;
  }

  /**
   *  ta -> tb -> ta
   */
  private void populateBean1(DefaultSingletonBeanRegistry beanRegistry, String tBean, int recursion) {
    printLog(recursion, "开始处理依赖注入: " + tBean);

    // 此处模拟 相互依赖
    if ("ta-instance".equals(tBean)) {
      // ta 依赖 tb, 需要注入 tb
      String tb = doGetBean(beanRegistry, "tb", recursion + 1);
      printLog(recursion, "ta 注入 tb 成功");
    } else if ("tb-instance".equals(tBean)) {
      // tb 依赖 ta, 需要注入 ta
      String ta = doGetBean(beanRegistry, "ta", recursion + 1);
      printLog(recursion, "tb 注入 ta 成功");
    }
    printLog(recursion, "处理依赖注入处理结束: " + tBean);
  }

  /**
   *  ta -> tb -> tc -> ta
   */
  private void populateBean2(DefaultSingletonBeanRegistry beanRegistry, String tBean, int recursion) {
    printLog(recursion, "开始处理依赖注入: " + tBean);

    // 此处模拟 相互依赖
    if ("ta-instance".equals(tBean)) {
      // ta 依赖 tb, 需要注入 tb
      String tb = doGetBean(beanRegistry, "tb", recursion + 1);
      printLog(recursion, "ta 注入 tb 成功");
    } else if ("tb-instance".equals(tBean)) {
      // tb 依赖 tc, 需要注入 tc
      String tc = doGetBean(beanRegistry, "tc", recursion + 1);
      printLog(recursion, "tb 注入 tc 成功");
    } else if ("tc-instance".equals(tBean)) {
      // tc 依赖 ta, 需要注入 ta
      String ta = doGetBean(beanRegistry, "ta", recursion + 1);
      printLog(recursion, "tc 注入 ta 成功");
    }
    printLog(recursion, "处理依赖注入处理结束: " + tBean);
  }

  private String getEarlyBeanReference(String beanName, String originBean, int recursion) {
    // aop 里面会缓存提前创建过的代理，在后置处理器处理的时候会复用

    // 这里可能是代理的 bean 或者 原始的bean
    //return originBean;
    String proxyBean = originBean + "-proxy";
    printLog(recursion, "创建[" + beanName + "]的代理对象：" + proxyBean + " ，并返回");
    return proxyBean;
  }

  private void printLog(int recursion, String msg) {
    for (int i = 0; i < recursion; i++) {
      System.out.print("                               ");
    }
    System.out.println("| " + msg);
  }
}
