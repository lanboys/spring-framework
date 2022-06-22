/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.web.servlet.mvc.condition;

import org.junit.Test;
import org.springframework.mock.web.test.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition.HeaderExpression;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Arjen Poutsma
 */
public class HeadersRequestConditionTests {

  @Test
  public void headerEquals() {
    assertEquals(new HeadersRequestCondition("foo"), new HeadersRequestCondition("foo"));
    assertEquals(new HeadersRequestCondition("foo"), new HeadersRequestCondition("FOO"));
    assertNotEquals(new HeadersRequestCondition("foo"), new HeadersRequestCondition("bar"));
    assertEquals(new HeadersRequestCondition("foo=bar"), new HeadersRequestCondition("foo=bar"));
    assertEquals(new HeadersRequestCondition("foo=bar"), new HeadersRequestCondition("FOO=bar"));
  }

  @Test
  public void headerPresent() {
    // 没有等号 = ，只匹配请求头是否存在，存在就匹配成功
    HeadersRequestCondition condition = new HeadersRequestCondition("foo");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("foo", "bar");

    // 要求存在请求头 foo, 所以匹配成功
    assertNotNull(condition.getMatchingCondition(request));
  }

  @Test
  public void headerNotPresent() {
    HeadersRequestCondition condition = new HeadersRequestCondition("!foo");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("foo", "bar");

    // 要求没有请求头 foo, 所以匹配失败
    assertNull(condition.getMatchingCondition(request));
  }

  @Test
  public void headerValueMatch() {
    HeadersRequestCondition condition = new HeadersRequestCondition("foo=bar");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("foo", "bar");

    // 要求请求头 key value 都匹配上，所以这里匹配成功
    assertNotNull(condition.getMatchingCondition(request));
  }

  @Test
  public void headerValueMatchButNot() {
    HeadersRequestCondition condition = new HeadersRequestCondition("foo=bar");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("foo", "bazz");

    // 要求请求头 key value 都匹配上，所以这里匹配失败
    assertNull(condition.getMatchingCondition(request));
  }

  @Test
  public void headerValueMatchButNotPresent() {
    HeadersRequestCondition condition = new HeadersRequestCondition("foo=bar");

    MockHttpServletRequest request = new MockHttpServletRequest();
    // 不存在，则值相当于 null
    //request.addHeader("foo", null);

    // 要求请求头 key value 都匹配上，所以这里匹配失败
    assertNull(condition.getMatchingCondition(request));
  }

  @Test
  public void headerValueMatchNegated() {
    HeadersRequestCondition condition = new HeadersRequestCondition("foo!=bar");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("foo", "baz");

    // 要求值不一样，匹配成功
    assertNotNull(condition.getMatchingCondition(request));
  }

  @Test
  public void headerValueMatchNegatedButMatch() {
    HeadersRequestCondition condition = new HeadersRequestCondition("foo!=bar");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("foo", "bar");

    // 要求值不一样，匹配失败
    assertNull(condition.getMatchingCondition(request));
  }

  @Test
  public void headerValueMatchNegatedButNoPresent() {
    HeadersRequestCondition condition = new HeadersRequestCondition("foo!=bar");
    MockHttpServletRequest request = new MockHttpServletRequest();
    // 不存在，则值相当于 null
    //request.addHeader("foo", null);

    // 要求值不一样，匹配成功
    assertNotNull(condition.getMatchingCondition(request));
  }

  @Test
  public void headerCaseSensitiveValueMatch() {
    HeadersRequestCondition condition = new HeadersRequestCondition("foo=Bar");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("foo", "bar");

    // 要求大小写一致，这里匹配失败
    assertNull(condition.getMatchingCondition(request));
  }

  @Test
  public void compareTo() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    HeadersRequestCondition condition1 = new HeadersRequestCondition("foo", "bar", "baz");
    HeadersRequestCondition condition2 = new HeadersRequestCondition("foo", "bar");

    int result = condition1.compareTo(condition2, request);
    assertTrue("Invalid comparison result: " + result, result < 0);

    result = condition2.compareTo(condition1, request);
    assertTrue("Invalid comparison result: " + result, result > 0);
  }

  @Test
  public void combine() {
    HeadersRequestCondition condition1 = new HeadersRequestCondition("foo=bar");
    HeadersRequestCondition condition2 = new HeadersRequestCondition("foo=baz");

    HeadersRequestCondition result = condition1.combine(condition2);
    Collection<HeaderExpression> conditions = result.getContent();
    assertEquals(2, conditions.size());
  }

  @Test
  public void getMatchingCondition() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("foo", "bar");

    HeadersRequestCondition condition = new HeadersRequestCondition("foo");

    HeadersRequestCondition result = condition.getMatchingCondition(request);
    assertEquals(condition, result);

    condition = new HeadersRequestCondition("bar");

    result = condition.getMatchingCondition(request);
    assertNull(result);
  }
}
