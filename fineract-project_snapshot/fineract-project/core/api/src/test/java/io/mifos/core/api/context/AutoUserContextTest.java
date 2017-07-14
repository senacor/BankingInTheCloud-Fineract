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
package io.mifos.core.api.context;

import io.mifos.core.api.util.UserContextHolder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Myrle Krantz
 */
public class AutoUserContextTest {
  @Test
  public void stateIsUndisturbedOutsideOfTryBlock()
  {
    UserContextHolder.setAccessToken("x", "y");

    //noinspection EmptyTryBlock
    try (final AutoUserContext ignored = new AutoUserContext("m", "n"))
    {
      Assert.assertEquals(UserContextHolder.checkedGetUser(), "m");
      Assert.assertEquals(UserContextHolder.checkedGetAccessToken(), "n");
    }

    Assert.assertEquals(UserContextHolder.checkedGetUser(), "x");
    Assert.assertEquals(UserContextHolder.checkedGetAccessToken(), "y");

    UserContextHolder.clear();
  }

}