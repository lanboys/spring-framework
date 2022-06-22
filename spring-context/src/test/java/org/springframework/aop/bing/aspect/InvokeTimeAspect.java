package org.springframework.aop.bing.aspect;

public class InvokeTimeAspect {

  public void invokeStart() {
    System.out.println("invokeStart(): 开始调用 " + System.currentTimeMillis());
  }

  public void invokeEnd() {
    System.out.println("invokeEnd(): 结束调用 " + System.currentTimeMillis());
  }
}
