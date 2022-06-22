package org.springframework.aop.bing.interceptor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * Created by oopcoder at 2022/6/22 8:40 .
 */

public class MyAdvisor implements Advisor {

  // 需要有适配器 AdvisorAdapter 支持的 Advice
  AfterReturningAdvice afterReturningAdvice = new AfterReturningAdvice() {
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
      System.out.println("MyAdvisor afterReturning(): ");
    }
  };

  @Override
  public Advice getAdvice() {
    System.out.println("getAdvice(): ");
    return afterReturningAdvice;
  }

  @Override
  public boolean isPerInstance() {
    return false;
  }
}
