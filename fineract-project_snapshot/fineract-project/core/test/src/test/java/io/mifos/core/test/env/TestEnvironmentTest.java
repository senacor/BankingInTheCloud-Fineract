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
package io.mifos.core.test.env;

import org.junit.Assert;
import org.junit.Test;

public class TestEnvironmentTest {

  private static final String UNIQUE_ID_PREFIX = "blah-";

  public TestEnvironmentTest() {
    super();
  }

  @Test
  public void shouldPopulateAllProperties() throws Exception {
    final TestEnvironment testEnvironment = new TestEnvironment("mifosio-core");
    testEnvironment.populate();

    for (final Object key : testEnvironment.properties.keySet()) {
      Assert.assertNotNull(key + " not found.", System.getProperty(key.toString()));
    }
  }

  @Test
  public void shouldGenerateUniqueId() {
    final TestEnvironment testEnvironment = new TestEnvironment("mifosio-core");
    final String uniqueId = testEnvironment.generateUniqueIdentifer(UNIQUE_ID_PREFIX);
    final String uniqueId2 = testEnvironment.generateUniqueIdentifer(UNIQUE_ID_PREFIX);
    Assert.assertNotEquals(uniqueId, uniqueId2);
    Assert.assertTrue(uniqueId.startsWith(UNIQUE_ID_PREFIX));
    Assert.assertTrue(uniqueId2.startsWith(UNIQUE_ID_PREFIX));
  }

  @Test
  public void shouldGenerateZeroBufferedUniqueId() {
    final TestEnvironment testEnvironment = new TestEnvironment("mifosio-core");
    final String uniqueId = testEnvironment.generateUniqueIdentifer(UNIQUE_ID_PREFIX, 5);
    Assert.assertTrue(uniqueId.startsWith(UNIQUE_ID_PREFIX));
    Assert.assertEquals(uniqueId.length() - UNIQUE_ID_PREFIX.length(), 5);
  }
}
