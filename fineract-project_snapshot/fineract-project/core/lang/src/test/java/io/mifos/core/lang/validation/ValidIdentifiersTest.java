/*
 * Copyright 2017 The Mifos Initiative
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
package io.mifos.core.lang.validation;

import io.mifos.core.lang.validation.constraints.ValidIdentifiers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.mifos.core.lang.validation.TestHelper.isValid;

/**
 * @author Myrle Krantz
 */
public class ValidIdentifiersTest {

  @Test
  public void valid()
  {
    final AnnotatedClass annotatedInstance = new AnnotatedClass(Collections.singletonList("xxxx"));
    Assert.assertTrue(isValid(annotatedInstance));
  }

  @Test
  public void toolong()
  {
    final AnnotatedClass annotatedInstance = new AnnotatedClass(Collections.singletonList("xxxxxx"));
    Assert.assertFalse(isValid(annotatedInstance));
  }

  @Test
  public void nullList()
  {
    final AnnotatedClass annotatedInstance = new AnnotatedClass(null);
    Assert.assertFalse(isValid(annotatedInstance));
  }

  @Test
  public void nullInList()
  {
    final AnnotatedClass annotatedInstance = new AnnotatedClass(Collections.singletonList(null));
    Assert.assertFalse(isValid(annotatedInstance));
  }

  @Test
  public void badApple()
  {
    final AnnotatedClass annotatedInstance = new AnnotatedClass(Arrays.asList("xxxx", null));
    Assert.assertFalse(isValid(annotatedInstance));
  }

  private static class AnnotatedClass {
    @ValidIdentifiers(maxLength = 5)
    List<String> identifiers;

    AnnotatedClass(final List<String> identifiers) {
      this.identifiers= identifiers;
    }
  }
}
