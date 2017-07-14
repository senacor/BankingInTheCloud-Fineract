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
package io.mifos.anubis.token;


import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Myrle Krantz, Markus Geiss
 */
public class TokenTypeTest {
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Test
  public void valueOfIssuer()
  {
    Assert.assertEquals(TokenType.valueOfIssuer("system").get(), TokenType.SYSTEM);
    Assert.assertEquals(TokenType.valueOfIssuer("tenant").get(), TokenType.TENANT);
    Assert.assertEquals(TokenType.valueOfIssuer("ra"), Optional.empty());
  }

  @Test
  public void getIssuer()
  {
    Assert.assertEquals(TokenType.SYSTEM.getIssuer(), "system");
    Assert.assertEquals(TokenType.TENANT.getIssuer(), "tenant");
  }
}
