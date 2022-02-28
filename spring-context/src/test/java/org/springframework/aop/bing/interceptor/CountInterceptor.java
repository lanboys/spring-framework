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
package org.springframework.aop.bing.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class CountInterceptor implements MethodInterceptor {

  private int count;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    this.count++;
    System.out.println(invocation.getMethod().getName() + " 方法调用次数加 1");
    // 可以自己处理各种逻辑
    Object proceed = invocation.proceed();
    System.out.println(invocation.getMethod().getName() + " 方法调用结束, 返回值: " + proceed);
    return proceed;
  }
}
