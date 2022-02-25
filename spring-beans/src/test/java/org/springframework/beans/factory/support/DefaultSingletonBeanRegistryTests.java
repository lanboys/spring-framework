/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.tests.sample.beans.DerivedTestBean;
import org.springframework.tests.sample.beans.TestBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 04.07.2006
 */
public class DefaultSingletonBeanRegistryTests {

  @Test
  public void testSingletons() {
    DefaultSingletonBeanRegistry beanRegistry = new DefaultSingletonBeanRegistry();

    TestBean tb = new TestBean();
    beanRegistry.registerSingleton("tb", tb);
    assertSame(tb, beanRegistry.getSingleton("tb"));

    TestBean tb2 = (TestBean) beanRegistry.getSingleton("tb2", new ObjectFactory<Object>() {
      @Override
      public Object getObject() throws BeansException {
        return new TestBean();
      }
    });
    assertSame(tb2, beanRegistry.getSingleton("tb2"));

    assertSame(tb, beanRegistry.getSingleton("tb"));
    assertSame(tb2, beanRegistry.getSingleton("tb2"));
    assertEquals(2, beanRegistry.getSingletonCount());
    String[] names = beanRegistry.getSingletonNames();
    assertEquals(2, names.length);
    assertEquals("tb", names[0]);
    assertEquals("tb2", names[1]);

    beanRegistry.destroySingletons();
    assertEquals(0, beanRegistry.getSingletonCount());
    assertEquals(0, beanRegistry.getSingletonNames().length);
  }

  @Test
  public void testDisposableBean() {
    DefaultSingletonBeanRegistry beanRegistry = new DefaultSingletonBeanRegistry();

    DerivedTestBean tb = new DerivedTestBean();
    beanRegistry.registerSingleton("tb", tb);
    beanRegistry.registerDisposableBean("tb", tb);
    assertSame(tb, beanRegistry.getSingleton("tb"));

    assertEquals(1, beanRegistry.getSingletonCount());
    String[] names = beanRegistry.getSingletonNames();
    assertEquals(1, names.length);
    assertEquals("tb", names[0]);

    assertFalse(tb.wasDestroyed());
    beanRegistry.destroySingletons();
    assertEquals(0, beanRegistry.getSingletonCount());
    assertEquals(0, beanRegistry.getSingletonNames().length);
    assertTrue(tb.wasDestroyed());
  }

  @Test
  public void testDependentRegistration() {
    DefaultSingletonBeanRegistry beanRegistry = new DefaultSingletonBeanRegistry();

    beanRegistry.registerDependentBean("a", "b");
    // b c 相互依赖，spring里面不允许循环依赖
    beanRegistry.registerDependentBean("b", "c");
    beanRegistry.registerDependentBean("c", "b");

    assertTrue(beanRegistry.isDependent("a", "b"));
    assertTrue(beanRegistry.isDependent("b", "c"));
    assertTrue(beanRegistry.isDependent("c", "b"));
    assertTrue(beanRegistry.isDependent("a", "c"));

    assertFalse(beanRegistry.isDependent("c", "a"));
    assertFalse(beanRegistry.isDependent("b", "a"));
    assertFalse(beanRegistry.isDependent("a", "a"));

    // b c 相互依赖，所以 b 也依赖 b
    assertTrue(beanRegistry.isDependent("b", "b"));
    assertTrue(beanRegistry.isDependent("c", "c"));
  }

  @Test
  public void testDependentRegistrationDestroy() {
    DefaultSingletonBeanRegistry beanRegistry = new DefaultSingletonBeanRegistry();

    beanRegistry.registerDisposableBean("b", new Disposable("b-Disposable"));
    beanRegistry.registerDisposableBean("c", new Disposable("c-Disposable"));
    beanRegistry.registerDisposableBean("a", new Disposable("a-Disposable"));
    // 依赖关系：a <- b <-> c
    beanRegistry.registerDependentBean("a", "b");
    beanRegistry.registerDependentBean("b", "c");
    beanRegistry.registerDependentBean("c", "b");
    // 可调整注册顺序测试
    // 理论上按registerDisposableBean的【倒序】进行销毁：a -> c -> b
    // 因为依赖关系，实际销毁顺序 c -> b -> a
    // 结论：要销毁a, 要先销毁依赖 a 的对象 b和c，没有依赖关系的按倒序销毁
    beanRegistry.destroySingletons();
  }

  @Test
  public void testRegisterContainedBean() {
    DefaultSingletonBeanRegistry beanRegistry = new DefaultSingletonBeanRegistry();

    beanRegistry.registerDisposableBean("c", new Disposable("c-Disposable"));
    beanRegistry.registerDisposableBean("a", new Disposable("a-Disposable"));
    beanRegistry.registerDisposableBean("b", new Disposable("b-Disposable"));
    // a 是外部bean,  b c 是内部bean,  先销毁外部容器才能销毁内部的bean，销毁外部容器后，会继续销毁内部容器， a -> c -> b
    beanRegistry.registerContainedBean("b", "a");
    beanRegistry.registerContainedBean("c", "a");

    beanRegistry.destroySingletons();
  }

  public static class Disposable implements DisposableBean {

    String name;

    public Disposable(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "Disposable{" +
          "name='" + name + '\'' +
          '}';
    }

    @Override
    public void destroy() throws Exception {
      System.out.println(name + "was destroyed");
    }
  }
}
