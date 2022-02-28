package org.springframework.aop.bing.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class TimeCountInterceptor implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    long start = System.currentTimeMillis();
    System.out.println(invocation.getMethod().getName() + " 方法调用计时开始...");
    Object proceed = invocation.proceed();
    System.out.println(invocation.getMethod().getName() + " 方法调用计时结束，耗时: " + (System.currentTimeMillis() - start) + "ms");
    return proceed;
  }
}
