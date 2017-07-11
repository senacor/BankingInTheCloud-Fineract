/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.core.test.domain;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Support class for testing correct validation of API domain objects.
 *
 * @author Myrle Krantz
 *
 * @param <T> the type of domain object being tested.
 */
@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public abstract class ValidationTest<T> {
  private final ValidationTestCase<T> testCase;

  public ValidationTest(final ValidationTestCase<T> testCase)
  {
    this.testCase = testCase;
  }

  @Test()
  public void test(){
    final T testSubject = createValidTestSubject();
    testCase.applyAdjustment(testSubject);
    Assert.assertTrue(testCase.toString(), testCase.check(testSubject));
  }

  @SuppressWarnings("WeakerAccess")
  abstract protected T createValidTestSubject();
}
