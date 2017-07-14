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
package io.mifos.core.api.util;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Myrle Krantz
 */
public class UserContextHolderTest {
  @Test
  public void testUserIdentifierCropping()
  {
    final String userIdentifier16 = RandomStringUtils.randomAlphanumeric(16);
    UserContextHolder.setAccessToken(userIdentifier16, "x");

    Assert.assertEquals(UserContextHolder.checkedGetUser(), userIdentifier16);

    final String userIdentifier32 = RandomStringUtils.randomAlphanumeric(32);
    UserContextHolder.setAccessToken(userIdentifier32, "x");

    Assert.assertEquals(UserContextHolder.checkedGetUser(), userIdentifier32);

    final String userIdentifier64 = userIdentifier32 + userIdentifier32;
    UserContextHolder.setAccessToken(userIdentifier64, "x");

    Assert.assertEquals(UserContextHolder.checkedGetUser(), userIdentifier32);
  }

  @Test(expected = IllegalStateException.class)
  public void testUnsetUserIdentifier()
  {
    UserContextHolder.clear();
    UserContextHolder.checkedGetUser();
  }

  @Test(expected = IllegalStateException.class)
  public void testUnsetAccessToken()
  {
    UserContextHolder.clear();
    UserContextHolder.checkedGetAccessToken();
  }

  @Test
  public void testSimpleUnSetAndGet()
  {
    UserContextHolder.clear();
    final Optional<UserContext> userContext = UserContextHolder.getUserContext();
    Assert.assertTrue(!userContext.isPresent());
  }

  @Test
  public void testSimpleSetAndGet()
  {
    final UserContext setUserContext = new UserContext("x", "y");
    UserContextHolder.clear();
    UserContextHolder.setUserContext(setUserContext);
    UserContextHolder.getUserContext().ifPresent(x -> Assert.assertEquals(setUserContext, x));
  }
}
